package com.issyzone.classicblulib.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FmArrayUtils {
    // 55 0C 80 B1 08 06 10 C8 01 1A 05 08
   // private  byte[] delimiter = new byte[]{(byte)0x55, (byte)0x0C, (byte)0x80,(byte)0xB1};
    public static boolean startsWith(byte[] array) {
        byte[] prefix = {
                (byte)0x55, (byte)0x0C, (byte)0x80, (byte)0xB1, (byte)0x08, (byte)0x06,
                (byte)0x10, (byte)0xC8, (byte)0x01, (byte)0x1A, (byte)0x05, (byte)0x08
        };
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
    public static List<byte[]> splitByteArrayBySequence(byte[] input) {
        byte[] sequence = {
                (byte)0x55, (byte)0x0C, (byte)0x80, (byte)0xB1, (byte)0x08, (byte)0x06,
                (byte)0x10, (byte)0xC8, (byte)0x01, (byte)0x1A, (byte)0x05, (byte)0x08
        };
        List<byte[]> result = new LinkedList<>();
        int sequenceStart = -1;

        for (int i = 0; i <= input.length - sequence.length; i++) {
            // Check if the next part of the array matches the sequence.
            boolean match = true;
            for (int j = 0; j < sequence.length; j++) {
                if (input[i + j] != sequence[j]) {
                    match = false;
                    break;
                }
            }

            // If it matches, we've found a split point.
            if (match) {
                if (sequenceStart != -1) {
                    result.add(Arrays.copyOfRange(input, sequenceStart, i));
                }
                sequenceStart = i;
                i += sequence.length - 1; // Skip the rest of the sequence.
            }
        }

        // Add the last segment if there is one.
        if (sequenceStart != -1 && sequenceStart < input.length) {
            result.add(Arrays.copyOfRange(input, sequenceStart, input.length));
        }

        return result;
    }
    public static List<byte[]> splitByteArray(byte[] input) {
        byte[] delimiter = {
                (byte)0x55, (byte)0x0C, (byte)0x80, (byte)0xB1, (byte)0x08, (byte)0x06,
                (byte)0x10, (byte)0xC8, (byte)0x01, (byte)0x1A, (byte)0x05, (byte)0x08
        };
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


}
