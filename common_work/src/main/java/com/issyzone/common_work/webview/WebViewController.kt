package com.issyzone.common_work.webview
import android.app.Application
import android.view.View
import com.issyzone.common_work.R


/**
 * author ：Peakmain
 * createTime：2023/04/06
 * mail:2726449200@qq.com
 * describe：
 */
class WebViewController {
    var P: WebViewParams? = null
        private set
    class WebViewParams(val application: Application) {
        var notCacheUrlArray: Array<String>? = null
        var mHandleUrlParamsCallback: HandleUrlParamsCallback<out WebViewEvent>? = null
        var mWebViewCount: Int = 3
        var userAgent: String = ""
        var mWebViewSetting: InitWebViewSetting = DefaultInitWebViewSetting()
        var mWebViewClientCallback: WebViewClientCallback = DefaultWebViewClientCallback()
        var mWebViewChromeClientCallback: WebViewChromeClientCallback =
            DefaultWebViewChromeClientCallback()
        //默认不显示Loading
        var mLoadingWebViewState: LoadingWebViewState = LoadingWebViewState.NotLoading
        var mLoadingViewConfig: LoadingViewConfig = ProgressLoadingConfigImpl()
        var mNoNetWorkView: View? = null
        var mNoNetWorkViewId: Int = R.layout.webview_no_network
        var mNetWorkViewBlock: ((View?, View?, String?) -> Unit)? = null
        var mEntities: Array<out Class<*>>? = null
        fun apply(controller: WebViewController, P: WebViewParams) {
            controller.P = P
        }
    }

}