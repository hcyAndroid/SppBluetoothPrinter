package com.issyzone.blelibs.upacker;

public interface MsgCallback {
    void onMsgPrased(byte[] data, int len);

    void onMsgFailed();
}
