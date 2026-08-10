# Vendored upstream parser

`src/oracle/java/androidx/constraintlayout/core/parser/` holds ten files copied verbatim from the
AOSP `androidx/constraintlayout` repository, commit `9abe35da953`, on 2026-08-10:

`CLArray`, `CLContainer`, `CLElement`, `CLKey`, `CLNumber`, `CLObject`, `CLParser`,
`CLParsingException`, `CLString`, `CLToken`.

They are the oracle this module compares the port against, so they are never edited — not the
headers, not the imports, not the formatting. Refreshing them means copying a newer upstream
revision wholesale and updating the commit recorded above. That is a deliberate act, because it
changes what "correct" means for every test here.

To verify they are still pristine against a local AOSP checkout:

```bash
diff -r src/oracle/java/androidx/constraintlayout/core/parser \
        <aosp>/constraintlayout/constraintlayout-core/src/main/java/androidx/constraintlayout/core/parser
```

## Why this module depends on `:compose-shaded`

The vendored files are in package `androidx.constraintlayout.core.parser` — the same package
`:compose` uses, since the port kept upstream's package names. The two could never share a
classpath. `:compose-shaded` publishes the identical sources relocated to
`tech.annexflow.constraintlayout`, so the oracle and the port coexist and can be compared in one
JVM. Any differential harness on the JVM has to go through the shaded flavour for this reason.

## Licence

Apache-2.0, the same licence this project carries. The upstream headers are unmodified.
