package com.issyzone.classicblulib.tools;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.issyzone.classicblulib.common.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class SocketConnection {
    private BluetoothSocket socket;

    public BluetoothSocket getSocket() {
        return socket;
    }

    private final BluetoothDevice device;
    private OutputStream outStream;
    private final ConnectionImpl connection;

    /**
     * UUID缓存
     */
    private final UUIDWrapper uuidWrapper;

    public List<byte[]> splitByteArray(byte[] input, byte[] delimiter) {
        List<byte[]> byteArrays = new ArrayList<>();

        int from = 0;
        int matchIndex = 0;

        for (int i = 0; i < input.length; i++) {
            if (input[i] == delimiter[matchIndex]) {
                // 如果当前字节匹配，则移到下一个匹配字节上
                matchIndex++;
                if (matchIndex == delimiter.length) { // 找到完全匹配的分隔符
                    // 从上一个分割点到当前找到分隔符之前的位置才是子数组
                    int to = i + 1 - delimiter.length;
                    byte[] subArray = new byte[to - from];
                    System.arraycopy(input, from, subArray, 0, subArray.length);
                    byteArrays.add(subArray);

                    // 更新下次搜索的起始点，并重置匹配索引
                    from = i + 1;
                    matchIndex = 0;
                }
            } else {
                matchIndex = 0; // 如果当前字节不匹配，立即重置匹配索引
            }
        }
        // 截取最后一个匹配后到数组结束的部分
        if (from < input.length) {
            byte[] subArray = new byte[input.length - from];
            System.arraycopy(input, from, subArray, 0, subArray.length);
            byteArrays.add(subArray);
        }

        return byteArrays;
    }

    private byte[] delimiter = new byte[]{(byte) 0x55, (byte) 0x0C, (byte) 0x80};

    public boolean startsWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    SocketConnection(ConnectionImpl connection, BTManager btManager, BluetoothDevice device, UUIDWrapper uuidWrapper, ConnectCallback callback) {
        this.device = device;
        this.connection = connection;
        this.uuidWrapper = uuidWrapper;
        BluetoothSocket tmp;
        try {
            connection.changeState(Connection.STATE_CONNECTING, false);
            tmp = device.createRfcommSocketToServiceRecord(uuidWrapper.getUuid());
        } catch (IOException e) {
            try {
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                tmp = (BluetoothSocket) method.invoke(device, 1);
            } catch (Throwable t) {
                onConnectFail(connection, callback, "Connect failed: Socket's create() method failed", e);
                return;
            }
        }
        socket = tmp;
        btManager.getExecutorService().execute(() -> {
            InputStream inputStream;
            OutputStream tmpOut;
            try {
                if (btManager.isDiscovering()) {
                    btManager.stopDiscovery();//停止搜索
                }
                synchronized (this) {
                    if (socket != null && !socket.isConnected()) {
                        socket.connect();
                        inputStream = socket.getInputStream();
                        tmpOut = socket.getOutputStream();
                    } else {
                        tmpOut = null;
                        inputStream = null;
                    }
                }

            } catch (IOException e) {
                if (!connection.isReleased()) {
                    onConnectFail(connection, callback, "Connect failed: " + e.getMessage(), e);
                }
                return;
            }
            connection.changeState(Connection.STATE_CONNECTED, true);
            if (callback != null) {
                callback.onSuccess(device);
            }
            connection.callback(MethodInfoGenerator.onConnectionStateChanged(device, uuidWrapper, Connection.STATE_CONNECTED));
            outStream = tmpOut;
            byte[] buffer = new byte[1024];
            int len;
            while (true) {
                try {
                    if (inputStream != null) {
                        len = inputStream.read(buffer);
                        byte[] data = Arrays.copyOf(buffer, len);
                        Log.i("SPP_Read>>>>", "Receive data =>> " + StringUtils.toHex(data));
                        // BTLogger.instance.d(BTManager.DEBUG_TAG, "Receive data =>> " + StringUtils.toHex(data));
                        connection.callback(MethodInfoGenerator.onRead(device, uuidWrapper, data));


                    }
                } catch (IOException e) {
                    if (!connection.isReleased()) {
                        connection.changeState(Connection.STATE_DISCONNECTED, false);
                    }
                    break;
                }
            }
            close();
        });
    }

    private void onConnectFail(ConnectionImpl connection, ConnectCallback callback, String errMsg, IOException e) {
        //connection.changeState(Connection.STATE_DISCONNECTED, true);
        connection.changeState(Connection.STATE_CONNECTFAILED, false);

        if (BTManager.isDebugMode) {
            Log.w(BTManager.DEBUG_TAG, errMsg);
        }
        close();
        if (callback != null) {
            callback.onFail(errMsg, e);
        }
        // connection.callback(MethodInfoGenerator.onConnectionStateChanged(device, uuidWrapper, Connection.STATE_DISCONNECTED));
        connection.callback(MethodInfoGenerator.onConnectionStateChanged(device, uuidWrapper, Connection.STATE_CONNECTFAILED));
    }

    void write(WriteData data) {
        if (outStream != null && !connection.isReleased()) {
            try {
                outStream.write(data.value);
                BTLogger.instance.d(BTManager.DEBUG_TAG, "Write success. tag = " + data.tag);
                if (data.callback == null) {
                    connection.callback(MethodInfoGenerator.onWrite(device, uuidWrapper, data.tag, data.value, true));
                } else {
                    data.callback.onWrite(device, data.tag, data.value, true);
                }
            } catch (IOException e) {
                onWriteFail("Write failed: " + e.getMessage(), data);
            }
        } else {
            onWriteFail("Write failed: OutputStream is null or connection is released", data);
        }
    }

    private void onWriteFail(String msg, WriteData data) {
        if (BTManager.isDebugMode) {
            Log.w(BTManager.DEBUG_TAG, msg);
        }
        if (data.callback == null) {
            connection.callback(MethodInfoGenerator.onWrite(device, uuidWrapper, data.tag, data.value, false));
        } else {
            data.callback.onWrite(device, data.tag, data.value, false);
        }
    }

    void close() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (Throwable e) {
                BTLogger.instance.e(BTManager.DEBUG_TAG, "Could not close the client socket: " + e.getMessage());
            }
        }
    }

    boolean isConnected() {
        return socket != null && socket.isConnected();
    }


    static class WriteData {
        String tag;
        byte[] value;
        WriteCallback callback;

        WriteData(String tag, byte[] value) {
            this.tag = tag;
            this.value = value;
        }
    }
}
