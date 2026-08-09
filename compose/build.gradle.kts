// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

// The only module with sources. The shaded flavours in `:compose-shaded` and
// `:compose-shaded-compose` generate theirs from this one — see `build-logic`.
plugins {
    id("tech.annexflow.constraintlayout-library")
    id("tech.annexflow.constraintlayout-publish")
}
