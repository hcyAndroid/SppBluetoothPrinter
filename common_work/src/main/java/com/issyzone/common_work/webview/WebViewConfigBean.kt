package com.issyzone.common_work.webview

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable

data class WebViewConfigBean(
    var url: String? = null,
    var statusBarState: StatusBarState = StatusBarState.LightModeState,
    var statusBarColor: Int = Color.WHITE
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(StatusBarState::class.java.classLoader)
            ?: StatusBarState.LightModeState,
        parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeParcelable(statusBarState, flags)
        parcel.writeInt(statusBarColor)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WebViewConfigBean> {
        override fun createFromParcel(parcel: Parcel): WebViewConfigBean {
            return WebViewConfigBean(parcel)
        }

        override fun newArray(size: Int): Array<WebViewConfigBean?> {
            return arrayOfNulls(size)
        }
    }
}