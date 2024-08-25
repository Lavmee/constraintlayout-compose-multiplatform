[![Maven Central](https://img.shields.io/maven-central/v/tech.annexflow.compose/constraintlayout-compose-multiplatform)](https://search.maven.org/search?q=g:tech.annexflow.compose)
![license](https://img.shields.io/github/license/Lavmee/constraintlayout-compose-multiplatform)

![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![badge-JVM](https://img.shields.io/badge/Platform-JVM-orange)
![badge-macOS](https://img.shields.io/badge/Platform-macOS-purple)

## Compose Multiplatform: ConstraintLayout

[ConstraintLayout](https://developer.android.com/jetpack/compose/layouts/constraintlayout) is a layout that allows you to place composables relative to other composables on the screen. It is an alternative to using multiple nested Row, Column, Box and other custom layout elements. ConstraintLayout is useful when implementing larger layouts with more complicated alignment requirements.

Consider using ConstraintLayout in the following scenarios:

- To avoid nesting multiple Columns and Rows for positioning elements on screen to improve readability of code.
- To position composables relative to other composables or to position composables based on guidelines, barriers or chains.
In the View system, ConstraintLayout was the recommended way to create large and complex layouts, as a flat view hierarchy was better for performance than nested views are. However, this is not a concern in Compose, which is able to efficiently handle deep layout hierarchies.

The `androidx.constraintlayout:constraintlayout-compose` library is available for Jetpack Compose, but it is not currently available for Compose Multiplatform. This library changes that, by providing the [ConstraintLayout](https://developer.android.com/reference/kotlin/androidx/constraintlayout/compose/package-summary) for many of the platforms supported by Compose Multiplatform.

| Platform      | Supported         |
|---------------|-------------------|
| Android       | ✅                |
| iOS           | ✅                |
| Desktop (JVM) | ✅                |
| Desktop (Mac) | ✅ (experimental) |
| Web           | ✅ (experimental) |

## Usage

Usage is very simple:

```kotlin
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun ConstraintLayoutContent() {
    ConstraintLayout {
        // Create references for the composables to constrain
        val (button, text) = createRefs()

        Button(
            onClick = { /* Do something */ },
            // Assign reference "button" to the Button composable
            // and constrain it to the top of the ConstraintLayout
            modifier = Modifier.constrainAs(button) {
                top.linkTo(parent.top, margin = 16.dp)
            }
        ) {
            Text("Button")
        }

        // Assign reference "text" to the Text composable
        // and constrain it to the bottom of the Button composable
        Text(
            "Text",
            Modifier.constrainAs(text) {
                top.linkTo(button.bottom, margin = 16.dp)
            }
        )
    }
}
```

You'll note that I have kept the package name the same as that in AndroidX. This is to enable easy migration for when support is added to Compose Multiplatform.

## Download

[![Maven Central](https://img.shields.io/maven-central/v/tech.annexflow.compose/constraintlayout-compose-multiplatform)](https://central.sonatype.com/namespace/tech.annexflow.compose)

```kotlin
val commonMain by getting {
    dependencies {
        implementation("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.4.0")
        /// Compose 1.7.0-alpha03
        implementation("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.5.0-alpha03")
    }
}
```

## Credits
Thanks to Chris Banes for the initial structure of the project.

## License

```
Copyright 2022 The Android Open Source Project
Portions 2023 Sergei Gagarin
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
