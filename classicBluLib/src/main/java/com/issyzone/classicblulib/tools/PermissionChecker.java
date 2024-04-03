package com.issyzone.classicblulib.tools;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.issyzone.classicblulib.common.AppHolder;


/**
 * 权限检查
 * 

 */
class PermissionChecker {
    static boolean hasPermission(@Nullable Context context, @NonNull String permission) {
        Activity activity = context instanceof Activity ? (Activity) context : AppHolder.getInstance().getTopActivity();
        context = context == null ? AppHolder.getInstance().getContext() : context;
        if (activity == null) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        }
    }
}
