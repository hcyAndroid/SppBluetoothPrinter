package com.issyzone.common_work.webview

@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandlerMethod(
    val path: String
)