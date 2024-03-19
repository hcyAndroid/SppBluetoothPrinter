package com.issyzone.blelibs.callback;

import com.issyzone.blelibs.data.BleDevice;

public interface BleScanPresenterImp {

    void onScanStarted(boolean success);

    void onScanning(BleDevice bleDevice);

}
