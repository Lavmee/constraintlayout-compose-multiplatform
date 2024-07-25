package androidx.constraintlayout.compose

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

/**
 * Wrapper to pass Class Verification from calling methods unavailable on older API.
 */
@RequiresApi(30)
private object Api30Impl {
    @JvmStatic
    fun isShowingLayoutBounds(view: View): Boolean {
        return view.isShowingLayoutBounds
    }
}

internal actual val isShowingLayoutBounds: Boolean
    @Composable get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        Api30Impl.isShowingLayoutBounds(LocalView.current)
