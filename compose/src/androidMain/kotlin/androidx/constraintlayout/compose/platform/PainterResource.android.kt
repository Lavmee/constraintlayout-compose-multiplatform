package androidx.constraintlayout.compose.platform

import androidx.compose.runtime.Composable

@Composable
internal actual fun painterResource() = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
