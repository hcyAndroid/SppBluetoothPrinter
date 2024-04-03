package com.issyzone.classicblulib.tools;

import androidx.annotation.NonNull;

import java.util.UUID;


public class UUIDWrapper {

    //00002902-0000-1000-8000-00805f9b34fb
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //00001101-0000-1000-8000-00805F9B34FB
    private UUIDWrapper() {
    }

    @NonNull
    private UUID uuid = SPP_UUID;

    @NonNull
    public UUID getUuid() {
        return uuid;
    }

    /**
     * 使用默认的UUID 默认使用{@link #SPP_UUID}连接
     *
     * @return 默认UUID包装器
     */
    public static UUIDWrapper useDefault() {
        return new UUIDWrapper();
    }

    /**
     * 使用自定义UUID连接
     *
     * @param uuid 自定义UUID
     * @return UUID包装器
     */
    public static UUIDWrapper useCustom(@NonNull UUID uuid) {
        UUIDWrapper wrapper = new UUIDWrapper();
        wrapper.uuid = uuid;
        return wrapper;
    }
}