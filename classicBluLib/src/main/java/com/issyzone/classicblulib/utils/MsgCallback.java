package com.issyzone.classicblulib.utils;

public interface MsgCallback {
    void onMsgPrased(byte[] data, int len);

    void onMsgFailed();
}
