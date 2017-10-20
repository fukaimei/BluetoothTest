package com.fukaimei.bluetoothtest.task;

import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import com.fukaimei.bluetoothtest.util.BluetoothConnector;

public class BlueAcceptTask extends AsyncTask<Void, Void, BluetoothSocket> {
    private final static String TAG = "BlueAcceptTask";
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    private BluetoothAdapter mAdapter;
    private BluetoothServerSocket mmServerSocket;

    public BlueAcceptTask(boolean secure) {
        Log.d(TAG, "BlueAcceptTask");
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (secure) {
                mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(
                        NAME_SECURE, BluetoothConnector.uuid);
            } else {
                mmServerSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, BluetoothConnector.uuid);
            }
        } catch (Exception e) {
            Log.d(TAG, "BlueAcceptTask First failed");
            e.printStackTrace();
            Method listenMethod = null;
            try {
                listenMethod = mAdapter.getClass().getMethod(
                        "listenUsingRfcommOn", new Class[]{int.class});
                mmServerSocket = (BluetoothServerSocket) listenMethod.invoke(
                        mAdapter, new Object[]{29});
            } catch (Exception e1) {
                Log.d(TAG, "BlueAcceptTask Second failed");
                e1.printStackTrace();
            }
        }
    }

    @Override
    protected BluetoothSocket doInBackground(Void... params) {
        Log.d(TAG, "doInBackground");
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            if (socket != null) {
                break;
            }
        }
        return socket;
    }

    @Override
    protected void onPostExecute(BluetoothSocket socket) {
        mListener.onBlueAccept(socket);
    }

    private BlueAcceptListener mListener;

    public void setBlueAcceptListener(BlueAcceptListener listener) {
        mListener = listener;
    }

    public static interface BlueAcceptListener {
        public abstract void onBlueAccept(BluetoothSocket socket);
    }

}
