package com.fukaimei.bluetoothtest.task;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import com.fukaimei.bluetoothtest.util.BluetoothConnector;

public class BlueConnectTask extends AsyncTask<BluetoothDevice, Void, BluetoothSocket> {
    private final static String TAG = "BlueConnectTask";
    private String mAddress;

    public BlueConnectTask(String address) {
        mAddress = address;
    }

    @Override
    protected BluetoothSocket doInBackground(BluetoothDevice... params) {
        BluetoothConnector connector = new BluetoothConnector(params[0], true,
                BluetoothAdapter.getDefaultAdapter(), null);
        Log.d(TAG, "doInBackground");
        BluetoothSocket socket = null;
        //蓝牙连接需要完整的权限,有些机型弹出提示对话框"***想进行通信",这就不行,日志会报错:
        //read failed, socket might closed or timeout, read ret: -1
        try {
            socket = ((BluetoothConnector.NativeBluetoothSocket) connector.connect()).getUnderlyingSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return socket;
    }

    @Override
    protected void onPostExecute(BluetoothSocket socket) {
        mListener.onBlueConnect(mAddress, socket);
    }

    private BlueConnectListener mListener;

    public void setBlueConnectListener(BlueConnectListener listener) {
        mListener = listener;
    }

    public static interface BlueConnectListener {
        public abstract void onBlueConnect(String address, BluetoothSocket socket);
    }

}
