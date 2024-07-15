package com.issyzone.common_work.webview
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel
import java.lang.ref.WeakReference
class WebViewModel : ViewModel() {
    fun initStatusBar(activity: Activity, webViewConfigBean: WebViewConfigBean?) {
        if (webViewConfigBean == null) return
        when (webViewConfigBean.statusBarState) {
            is StatusBarState.LightModeState -> {
                StatusBarUtils.setColor(
                    activity,
                    webViewConfigBean.statusBarColor,
                    0
                )
                StatusBarUtils.setLightMode(activity)
            }
            is StatusBarState.DartModeState -> {
                StatusBarUtils.setColor(
                    activity,
                    webViewConfigBean.statusBarColor,
                    0
                )
                StatusBarUtils.setDarkMode(activity)
            }
            is StatusBarState.StatusColorState -> {
                StatusBarUtils.setColor(
                    activity,
                    webViewConfigBean.statusBarColor,
                    0
                )
            }
            is StatusBarState.NoStatusModeState -> {
                StatusBarUtils.hideStatusBar(activity)
            }
        }
    }

    fun initTopContent(
        context: Context?,
        headContent: WeakReference<FrameLayout>?,
        h5UtilsParams: H5UtilsParams
    ) {
        val topContentView = h5UtilsParams.mHeadContentView
        val topContentViewBlock = h5UtilsParams.mHeadViewBlock
        val topContentViewId = h5UtilsParams.mHeadContentViewId
        val topContent = headContent?.get()
        if (topContentView == null && topContentViewId == 0 || topContent == null || context == null) {
            headContent?.get()?.visibility = View.GONE
            return
        }
        topContent.visibility = View.VISIBLE
        if (topContent.childCount > 0) {
            topContent.removeAllViews()
        }
        if (topContentViewId != 0) {
            val view = View.inflate(context, topContentViewId, topContent)
            topContentViewBlock?.invoke(view)
            return
        }
        if (topContentView != null) {
            topContent.addView(topContentView)
            topContentViewBlock?.invoke(topContentView)
            return
        }
    }
}