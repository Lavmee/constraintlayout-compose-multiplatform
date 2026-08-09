#!/usr/bin/env bash
# Verifies a staged release before anything is uploaded, and reports what it weighs.
#
# Four assertions, in order of how badly they fail if skipped:
#
#  1. Each flavour's root .module must reference all seven targets. A Kotlin/Native target the
#     build host cannot build is dropped rather than failing the build, so a publish from a host
#     without Xcode produces root modules that resolve fine and offer no iOS and no macOS. Nothing
#     else catches this: the build is green, the upload succeeds, and consumers find out.
#  2. Eight module directories per flavour must be present — the seven targets plus the root.
#  3. Every publishable artifact must carry a detached signature. Gradle requires signing for a
#     release version, so a tag with no key configured fails on its own — but not for a -SNAPSHOT,
#     where the sign tasks are SKIPPED and the staging build succeeds with zero .asc files. That is
#     the workflow_dispatch rehearsal, the run whose entire purpose is to prove the secrets work
#     before a tag depends on them, so it is the one run that must not be allowed to pass while
#     signing nothing.
#  4. The staged version must be the version being released, when the caller says what that is.
#     `-PconstraintlayoutVersion` reaching the modules is assumed everywhere and checked nowhere:
#     if it ever stopped arriving the version would stay at build.gradle.kts's -SNAPSHOT default,
#     publishAndReleaseToMavenCentral would route it to the snapshot repository, perform no Portal
#     release, and exit 0 — a tag reporting a successful release of nothing.
#
# The size line exists because Maven Central tracks release size as a three-month average per
# organisation and begins rate limiting on 2026-10-01. Three flavours of a seven-target library is
# a large release by that measure, so the number is worth seeing on every run rather than
# discovering later.
set -euo pipefail

staging="${1:?usage: check-staged-release.sh <staging-repo-dir> [expected-version]}/tech/annexflow/compose"

# Optional, and empty on the workflow_dispatch path, which stages whatever the build's default
# version is and is not releasing anything. Absent means both version assertions are skipped.
version="${2:-}"

[ -d "$staging" ] || { echo "No staged release at $staging" >&2; exit 1; }

flavours="constraintlayout-compose-multiplatform
constraintlayout-compose-multiplatform-shaded
constraintlayout-compose-multiplatform-shaded-compose"

targets="android jvm js wasm-js macosarm64 iosarm64 iossimulatorarm64"

status=0

if [ -n "$version" ]; then
    # A release must never stage a snapshot: the publish plugin silently reroutes a -SNAPSHOT
    # version to the snapshot repository and performs no Portal release, so this is the difference
    # between publishing and appearing to publish.
    case "$version" in
        *-SNAPSHOT)
            echo "Refusing to stage a release of snapshot version '$version'." >&2
            echo "publishAndReleaseToMavenCentral would route this to the snapshot repository," >&2
            echo "release nothing at the Portal, and exit 0." >&2
            status=1
            ;;
    esac
fi

for flavour in $flavours; do
    if [ ! -d "$staging/$flavour" ]; then
        echo "Flavour '$flavour' was not staged at all: $staging/$flavour" >&2
        status=1
        continue
    fi

    # Gradle lays the staged files out as <group>/<artifact>/<version>/, so the directory existing
    # is proof that the version the caller asked for is the version that was actually written.
    if [ -n "$version" ] && [ ! -d "$staging/$flavour/$version" ]; then
        echo "Nothing staged for version '$version' at $staging/$flavour/$version." >&2
        echo "Staged versions:" >&2
        find "$staging/$flavour" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; >&2
        status=1
    fi

    # Exactly one, not the first of however many. A -SNAPSHOT publish writes uniquely timestamped
    # filenames, so a staging directory reused across runs accumulates several root modules and
    # `head -1` picks between them arbitrarily — the target assertion below then passes or fails on
    # which one `find` happened to walk into first. Refusing to guess is more honest than guessing.
    root_modules="$(find "$staging/$flavour" -name "$flavour-*.module" 2>/dev/null | sort || true)"
    if [ -n "$root_modules" ]; then
        root_count="$(printf '%s\n' "$root_modules" | wc -l | tr -d ' ')"
    else
        root_count=0
    fi
    if [ "$root_count" -ne 1 ]; then
        echo "Expected exactly one root .module under $staging/$flavour, found $root_count:" >&2
        [ "$root_count" -eq 0 ] || printf '%s\n' "$root_modules" >&2
        echo "Delete the staging directory and stage again." >&2
        status=1
        continue
    fi

    for target in $targets; do
        if ! grep -q "\"$flavour-$target\"" "$root_modules"; then
            echo "Root module does not reference $flavour-$target: $root_modules" >&2
            status=1
        fi
    done
done

# Eight directories per flavour — the seven targets plus the root — and nothing else under the
# group. Counted across the whole staging repository rather than per flavour because the three
# flavours' artifact ids are prefixes of one another, so no per-flavour glob can separate them.
expected_modules=$(( 8 * $(printf '%s\n' "$flavours" | wc -l | tr -d ' ') ))
modules="$(find "$staging" -mindepth 1 -maxdepth 1 -type d | wc -l | tr -d ' ')"
if [ "$modules" -ne "$expected_modules" ]; then
    echo "Expected $expected_modules module directories (seven targets plus a root, per flavour), found $modules:" >&2
    find "$staging" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; | sort >&2
    status=1
fi

# Everything Maven Central treats as a publishable artifact needs a sibling detached signature.
# Every unsigned file is reported rather than just the first, because the cause is almost always
# "no signing key at all" and seeing one name invites fixing one file.
unsigned=0
while IFS= read -r artifact; do
    if [ ! -f "$artifact.asc" ]; then
        echo "Unsigned: $artifact" >&2
        unsigned=$((unsigned + 1))
    fi
done < <(
    find "$staging" -type f \
        \( -name '*.jar' -o -name '*.klib' -o -name '*.aar' -o -name '*.pom' -o -name '*.module' \) |
        sort
)
if [ "$unsigned" -ne 0 ]; then
    echo "$unsigned staged artifacts have no detached .asc signature." >&2
    echo "On a snapshot the signing tasks skip rather than fail, so this is what an unset or" >&2
    echo "misnamed ORG_GRADLE_PROJECT_signingInMemoryKey looks like from here." >&2
    status=1
fi

[ "$status" -eq 0 ] || exit "$status"

# Counted without sha256/sha512, because this staging repository is plain `maven-publish` output
# and the deployment is not. The publish plugin's `Checksum.DEFAULT` is `[MD5, SHA1]`, so those two
# extensions are all that reaches Maven Central and the rest never leaves the runner. Since the
# only reason to print this line is Central's release-size budget, counting files it will never see
# would defeat it.
shipped="$(find "$staging" -type f ! -name '*.sha256' ! -name '*.sha512' | wc -l | tr -d ' ')"
bytes="$(find "$staging" -type f ! -name '*.sha256' ! -name '*.sha512' -exec du -k {} + | awk '{s += $1} END {printf "%.1f", s / 1024}')"

echo "Staged release: ${bytes}M, $shipped files, $modules modules."
