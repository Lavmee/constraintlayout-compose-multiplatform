# How To Contribute

First of all, I'd like to express my appreciation to you for contributing to this project. 
Below is the guidance for how to report issues, propose new features, and submit contributions via Pull Requests (PRs).

## Before you start, file an issue
If you have a question, think you've discovered an issue, would like to propose a new feature, etc., then find/file an issue **BEFORE** starting work to fix/implement it.

### Search existing issues first

Before filing a new issue, search existing open and closed issues first: It is likely someone else has found the problem you're seeing, and someone may be working on or have already contributed a fix!

If no existing item describes your issue/feature, great - please file a new issue.

## Contributing fixes / features

For those able & willing to help fix issues and/or implement features ...

### Development environment

Make sure you have
 - JDK 17  
 - A Mac if you're developing Compose for iOS/macOS

### Project layout

`:compose` is the only module with sources — edit them there. The two shaded flavours,
`:compose-shaded` and `:compose-shaded-compose`, own no `src/` at all: their sources are generated
from `:compose` at build time by rewriting package names, so a change to `:compose` reaches every
published flavour on its own. The generation and the shared Kotlin Multiplatform configuration live
in the `build-logic` convention plugin; what makes each flavour different is the `constraintlayout.*`
properties in its own `gradle.properties`.

To inspect what a flavour will actually compile:

```shell
./gradlew :compose-shaded:relocateCommonMain
```

### Code guidelines
To check the code style, run `./gradlew spotlessCheck` and fix the errors before you submit any PR.  
