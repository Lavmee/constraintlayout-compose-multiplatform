# Vendored upstream core

`src/oracle/java/androidx/constraintlayout/core/` holds the whole of `constraintlayout-core` — 138
files, copied verbatim from the AOSP `androidx/constraintlayout` repository, commit `9abe35da953`,
on 2026-08-10.

They are the oracle this module compares the port against, so they are never edited — not the
headers, not the imports, not the formatting. Refreshing them means copying a newer upstream
revision wholesale and updating the commit recorded above. That is a deliberate act, because it
changes what "correct" means for every test here.

To verify they are still pristine against a local AOSP checkout:

```bash
diff -r src/oracle/java/androidx/constraintlayout/core \
        <aosp>/constraintlayout/constraintlayout-core/src/main/java/androidx/constraintlayout/core
```

## Why the whole module rather than a subset

The parser needed ten files. The solver's dependency closure is most of `core`, and a computed
subset would be fragile: six class names are duplicated across `core` packages — `Barrier`, `Chain`,
`Guideline`, `GuidelineReference`, `Helper`, `Transition` — so any name-based closure is ambiguous,
and upstream refactors would move the boundary on every refresh.

`constraintlayout-core` is self-contained: it depends only on `java.*`, `org.jspecify` and
`androidx.annotation`, the latter two annotation-only. Copying it whole costs nothing in complexity
and means no future comparison needs further vendoring.

## Why this module depends on `:compose-shaded`

The vendored files are in package `androidx.constraintlayout.core.*` — the same packages `:compose`
uses, since the port kept upstream's names. The two could never share a classpath.
`:compose-shaded` publishes the identical sources relocated to `tech.annexflow.constraintlayout`,
so the oracle and the port coexist and can be compared in one JVM.

## Licence

Apache-2.0, the same licence this project carries. The upstream headers are unmodified.
