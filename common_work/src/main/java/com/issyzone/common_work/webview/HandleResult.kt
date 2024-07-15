package com.issyzone.common_work.webview

sealed class HandleResult {

    /**
     * 没有处理完成
     */
    object NotConsume : HandleResult()

    /**
     * 已经处理完成
     */
    object Consumed : HandleResult()

    /**
     * 正在处理中
     */
    object Consuming : HandleResult()
}