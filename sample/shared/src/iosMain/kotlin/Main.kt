import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import tech.annexflow.sample.App

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
