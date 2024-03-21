package com.issyzone.blelibs.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BitmapUtils {
    public static byte[] print(Bitmap paramBitmap, int widthBitmap, int heightBitmap) {
        int i = 0, j = 0;
        int k;
        if (widthBitmap % 8 == 0) {
            k = widthBitmap / 8;
        } else {
            k = widthBitmap / 8 + 1;
        }
        int m;
        byte[] arrayOfByte;
        int n;
        for (arrayOfByte = new byte[m = heightBitmap * k], n = 0; n < m; ) {
            arrayOfByte[n] = 0;
            n++;
        }
        while (i < heightBitmap) {
            int[] arrayOfInt= new int[widthBitmap];
            paramBitmap.getPixels(arrayOfInt, 0, widthBitmap, 0, i, widthBitmap, 1);
            int b1;
            for (n = 0, b1 = 0; b1 < widthBitmap; ) {
                int i1 = arrayOfInt[b1];
                if (++n > 8) {
                    n = 1;
                    j++;
                }
                if (i1 != -1) {
                    int i2 = 1 << 8 - n;
                    i1 = Color.green(i1);
                    int i3 = Color.blue(i1);
                    if ((Color.red(i1) + i1 + i3) / 3 < 128){
                        arrayOfByte[j] = (byte)(arrayOfByte[j] | i2);
                    }
                }
                b1++;
            }
            j = k * ++i;
        }
        return arrayOfByte;
    }
}
