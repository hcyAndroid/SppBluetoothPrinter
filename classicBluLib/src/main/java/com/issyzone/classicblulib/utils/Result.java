package com.issyzone.classicblulib.utils;

class Result {
    int off;
    int len;
    int end;
    byte[] b;

    Result set(byte[] b, int off, int len) {
        this.b = b;
        this.off = off;
        this.len = len;
        this.end = off + len;
        return this;
    }
}
