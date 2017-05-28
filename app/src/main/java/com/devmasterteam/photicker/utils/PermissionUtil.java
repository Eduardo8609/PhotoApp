package com.devmasterteam.photicker.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.devmasterteam.photicker.R;

public final class PermissionUtil {

    public static final int CAMERA_PERMISSION = 0;

    /**
     * Verifica se é necessário requisitar a permissão ao usuário de acordo com a versão do Android
     */
    private static boolean needToAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static boolean hasCameraPermission(Context context) {
        if (needToAskPermission())
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        return true;
    }

    /**
     * Faz a requisição ao usuário para acessar a câmera
     */
    public static void asksCameraPermission(final Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            // Mostra uma dialog com a explicação e um botão para permitir a requisição
            new AlertDialog.Builder(activity)
                    .setMessage(activity.getString(R.string.permission_camera_explanation))
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PermissionUtil.CAMERA_PERMISSION);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionUtil.CAMERA_PERMISSION);
        }
    }
}
