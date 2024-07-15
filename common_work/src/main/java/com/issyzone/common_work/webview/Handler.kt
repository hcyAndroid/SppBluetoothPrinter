package com.issyzone.common_work.webview

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Handler(
    /**
     * 申明Scheme
     * @return String
     */
    val scheme: String,
    /**
     * 申明Authority
     * @return String
     */
    val authority: Array<String>
)