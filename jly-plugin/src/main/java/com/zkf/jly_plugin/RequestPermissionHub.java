package com.zkf.jly_plugin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;



/**
 * 权限请求
 */
public class RequestPermissionHub extends Fragment {

    public interface Callback {
        void onResult(boolean isGranted);
    }

    private final static int PERMISSION_REQUEST_CODE = 4896;
    private final static int APP_SETTING_REQUEST_CODE = 4851;


    /**
     * 是有拥有对应的权限
     */
    public static boolean hasPermission(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermission(FragmentManager fragmentManager, Callback callback, boolean promptToSet,   String... permissions) {
        RequestPermissionHub requestPermissionHub = new RequestPermissionHub();
        requestPermissionHub.permissions = permissions;
        requestPermissionHub.callback = callback;
        requestPermissionHub.promptToSet = promptToSet;
        fragmentManager.beginTransaction()
                .add(requestPermissionHub, RequestPermissionHub.class.getName())
                .commitAllowingStateLoss();
    }

    /*
     * 请求相机权限
     */
    public static void requestCameraPermission(FragmentManager fragmentManager, Callback callback) {
        RequestPermissionHub requestPermissionHub = new RequestPermissionHub();
        requestPermissionHub.permissions = new String[]{Manifest.permission.CAMERA};
        requestPermissionHub.callback = callback;
        requestPermissionHub.promptToSet = true;
        fragmentManager.beginTransaction()
                .add(requestPermissionHub, RequestPermissionHub.class.getName())
                .commitAllowingStateLoss();
    }

    /**
     * 请求相册权限
     */
    public static void requestAlbumPermission(FragmentManager fragmentManager, Callback callback) {
        RequestPermissionHub requestPermissionHub = new RequestPermissionHub();
        requestPermissionHub.permissions = new String[]{getAlbumPermission()};
        requestPermissionHub.callback = callback;
        requestPermissionHub.promptToSet = true;
        fragmentManager.beginTransaction()
                .add(requestPermissionHub, RequestPermissionHub.class.getName())
                .commitAllowingStateLoss();
    }


    /**
     * 请求精确位置权限
     */
    public static void requestLocationPermission(FragmentManager fragmentManager, Callback callback) {
        RequestPermissionHub requestPermissionHub = new RequestPermissionHub();
        requestPermissionHub.permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissionHub.callback = callback;
        requestPermissionHub.promptToSet = true;
        fragmentManager.beginTransaction()
                .add(requestPermissionHub, RequestPermissionHub.class.getName())
                .commitAllowingStateLoss();
    }





    public static String getAlbumPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    /**
     * 请求通知权限
     */
    public static void requestNotificationPermission(FragmentManager fragmentManager, Callback callback, boolean promptToSet) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(fragmentManager, callback, promptToSet,  Manifest.permission.POST_NOTIFICATIONS);
        } else {
            if (callback != null) {
                callback.onResult(true);
            }
        }
    }

    /**
     * 请求蓝牙权限
     */
    public static void requestBLEPermission(FragmentManager fragmentManager, Callback callback) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
            requestPermission(fragmentManager, callback, true,   permissions);
        } else {
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            requestPermission(fragmentManager, callback, true,   permissions);
        }
    }

    private String[] permissions;

    private Callback callback;

    /**
     * 是否提示去设置
     */
    private boolean promptToSet = true;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasPermission(requireContext(), permissions)) {
            handleCallback(true);
            detach();
            return;
        }



        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    handleCallback(false);
                    if (promptToSet) {
                        showOpenAppSettingDialog(permissions[i]);
                    }
                    return;
                }
            }
            handleCallback(true);
            detach();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void handleCallback(boolean b) {
        if (callback != null) {
            callback.onResult(b);
        }
    }

    private void showOpenAppSettingDialog(String permission) {
        String messagge = "";
        switch (permission) {
            case Manifest.permission.POST_NOTIFICATIONS:
                messagge = "您未开启通知权限，请在系统设置中开启";
                break;
            case Manifest.permission.CAMERA:
                messagge ="您未开启相机权限，请在系统设置中开启";
                break;
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                messagge = "您未开启位置权限，请在系统设置中开启";
                break;
            case Manifest.permission.BLUETOOTH_SCAN:
            case Manifest.permission.BLUETOOTH_CONNECT:
                messagge = "您未开启蓝牙权限，请在系统设置中开启";
                break;
        }


        new AlertDialog.Builder(getContext())
                .setTitle("温馨提示")
                .setMessage(messagge)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openAppSetting();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        detach();
                    }
                })
                .show();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        detach();
    }

    private void openAppSetting() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivityForResult(intent, APP_SETTING_REQUEST_CODE);
    }

    private void detach() {
        getParentFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
    }

}
