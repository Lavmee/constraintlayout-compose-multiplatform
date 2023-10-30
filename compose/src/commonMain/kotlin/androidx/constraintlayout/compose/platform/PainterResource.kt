package androidx.constraintlayout.compose.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
internal expect fun painterResource(): Painter
