package com.fukaimei.bluetoothtest.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

public class BluetoothUtil {
    private final static String TAG = "BluetoothUtil";

    // 获取蓝牙的开关状态
    public static boolean getBlueToothStatus(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean enabled = false;
        switch (bluetoothAdapter.getState()) {
            case BluetoothAdapter.STATE_ON:
            case BluetoothAdapter.STATE_TURNING_ON:
                enabled = true;
                break;
            case BluetoothAdapter.STATE_OFF:
            case BluetoothAdapter.STATE_TURNING_OFF:
            default:
                enabled = false;
                break;
        }
        return enabled;
    }

    // 打开或关闭蓝牙
    public static void setBlueToothStatus(Context context, boolean enabled) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (enabled == true) {
            bluetoothAdapter.enable();
        } else {
            bluetoothAdapter.disable();
        }
    }

    public static String readInputStream(InputStream inStream) {
        String result = "";
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] data = outStream.toByteArray();
            outStream.close();
            inStream.close();
            result = new String(data, "utf8");
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    public static void writeOutputStream(BluetoothSocket socket, String message) {
        Log.d(TAG, "begin writeOutputStream message=" + message);
        try {
            OutputStream outStream = socket.getOutputStream();
            outStream.write(message.getBytes());
            //outStream.flush();
            //outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end writeOutputStream");
    }

}
