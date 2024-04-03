package com.issyzone.classicblulib.tools;


import com.issyzone.classicblulib.common.AbstractLogger;


class BTLogger extends AbstractLogger {
    static final BTLogger instance = new BTLogger();

    @Override
    protected boolean accept(int priority, String tag, String msg) {
        return BTManager.isDebugMode;
    }
}