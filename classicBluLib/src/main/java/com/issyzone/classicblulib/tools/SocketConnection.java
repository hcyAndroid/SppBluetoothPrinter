package com.issyzone.classicblulib.tools;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.issyzone.classicblulib.common.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;



class SocketConnection {
    private BluetoothSocket socket;
    private static final String TAG = "SPP_SOCKET";

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
           // readDataFromBluetooth(inputStream, connection, device, uuidWrapper);
   /*         byte[] buffer = new byte[1024];
            int len;
            while (true) {
                try {
                    if (inputStream != null) {
                        len = inputStream.read(buffer);
                        byte[] data = Arrays.copyOf(buffer, len);
                        Log.i("SyzClassicBluManager","SPP_Read>>>>Receive data =>> " + StringUtils.toHex(data));
                        // BTLogger.instance.d(BTManager.DEBUG_TAG, "Receive data =>> " + StringUtils.toHex(data));
                        connection.callback(MethodInfoGenerator.onRead(device, uuidWrapper, data));

                    }
                } catch (IOException e) {
                    Log.e("SyzClassicBluManager","SPP_Read=inputStream===null==${}"+connection.isReleased());
                    if (!connection.isReleased()) {
                        connection.changeState(Connection.STATE_DISCONNECTED, false);
                    }
                    break;
                }
            }
            close();*/
            byte[] buffer = new byte[2048];
            int len;
            ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
            while (true) {
                try {
                    if (inputStream != null) {
                        len = inputStream.read(buffer);
                        if (len > 0) {
                            packetBuffer.write(buffer, 0, len);
                            byte[] data = packetBuffer.toByteArray();
                            Log.i("SyzClassicBluManager", "SPP_Read>>>>Receive data =>> " + StringUtils.toHex(data));
                            connection.callback(MethodInfoGenerator.onRead(device, uuidWrapper, data));
                            packetBuffer.reset();
                        }
                    }
                } catch (IOException e) {
                    Log.e("SyzClassicBluManager", "SPP_Read=inputStream===null==${}" + connection.isReleased());
                    if (!connection.isReleased()) {
                        connection.changeState(Connection.STATE_DISCONNECTED, false);
                    }
                    break;
                }
            }
            close();
        });
    }


//    private void readDataFromBluetooth(InputStream inputStream, ConnectionImpl connection, BluetoothDevice device, UUIDWrapper uuidWrapper) {
//        final int BUFFER_SIZE = 2048;
//        byte[] buffer = new byte[BUFFER_SIZE];
//        int len;
//        // 创建一个缓存来存储不完整的数据包
//        ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
//        while (true) {
//            try {
//                if (inputStream != null) {
//                    len = inputStream.read(buffer);
//                    if (len > 0) {
//                        // 将数据写入缓存
//                        packetBuffer.write(buffer, 0, len);
//                        // 处理缓存中的数据包
//                        processPackets(packetBuffer, connection, device, uuidWrapper);
//                    }
//                }
//            } catch (IOException e) {
//                Log.e("SyzClassicBluManager", "SPP_Read=inputStream===null==${}" + connection.isReleased());
//                if (!connection.isReleased()) {
//                    connection.changeState(Connection.STATE_DISCONNECTED, false);
//                }
//                break;
//            }
//        }
//    }

    private void processPackets(ByteArrayOutputStream packetBuffer, ConnectionImpl connection, BluetoothDevice device, UUIDWrapper uuidWrapper) {
        byte[] data = packetBuffer.toByteArray();
        final int PACKET_LENGTH = 1024; // 每条数据包的最大长度

        while (data.length >= PACKET_LENGTH) {
            // 提取一个完整的数据包
            byte[] packet = Arrays.copyOfRange(data, 0, PACKET_LENGTH);
            // 处理数据包
            Log.i("SyzClassicBluManager", "SPP_Read>>>>Receive data =>> " + StringUtils.toHex(packet));
            connection.callback(MethodInfoGenerator.onRead(device, uuidWrapper, packet));
            // 从缓存中移除已处理的数据包
            data = Arrays.copyOfRange(data, PACKET_LENGTH, data.length);
            packetBuffer.reset();
            packetBuffer.write(data, 0, data.length);
        }
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
                Log.i("SyzClassicBluManager","SPP_WRITE>>>>WriteData data =>> " + StringUtils.toHex(data.value));

            } catch (IOException e) {
                Log.e("SyzClassicBluManager","SPP_WRITE>>>>WriteData data 异常=>> " + StringUtils.toHex(data.value)+"异常原因"+e.getMessage());
                onWriteFail("Write failed: " + e.getMessage(), data);
            }
        } else {
            Log.e("SyzClassicBluManager","SPP_WRITE>>>>WriteData data 异常=>>Write failed: OutputStream is null or connection is released");

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
