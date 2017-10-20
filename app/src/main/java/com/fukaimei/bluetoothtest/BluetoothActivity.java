package com.fukaimei.bluetoothtest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.fukaimei.bluetoothtest.adapter.BlueListAdapter;
import com.fukaimei.bluetoothtest.bean.BlueDevice;
import com.fukaimei.bluetoothtest.task.BlueAcceptTask;
import com.fukaimei.bluetoothtest.task.BlueConnectTask;
import com.fukaimei.bluetoothtest.task.BlueReceiveTask;
import com.fukaimei.bluetoothtest.util.BluetoothUtil;
import com.fukaimei.bluetoothtest.widget.InputDialogFragment;

public class BluetoothActivity extends AppCompatActivity implements
        OnClickListener, OnItemClickListener, OnCheckedChangeListener,
        BlueConnectTask.BlueConnectListener, InputDialogFragment.InputCallbacks, BlueAcceptTask.BlueAcceptListener {
    private static final String TAG = "BluetoothActivity";
    private CheckBox ck_bluetooth;
    private TextView tv_discovery;
    private ListView lv_bluetooth;
    private BluetoothAdapter mBluetooth;
    private ArrayList<BlueDevice> mDeviceList = new ArrayList<BlueDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        bluetoothPermissions();
        ck_bluetooth = (CheckBox) findViewById(R.id.ck_bluetooth);
        tv_discovery = (TextView) findViewById(R.id.tv_discovery);
        lv_bluetooth = (ListView) findViewById(R.id.lv_bluetooth);
        if (BluetoothUtil.getBlueToothStatus(this) == true) {
            ck_bluetooth.setChecked(true);
        }
        ck_bluetooth.setOnCheckedChangeListener(this);
        tv_discovery.setOnClickListener(this);
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (mBluetooth == null) {
            Toast.makeText(this, "本机未找到蓝牙功能", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 定义获取基于地理位置的动态权限
    private void bluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    /**
     * 重写onRequestPermissionsResult方法
     * 获取动态权限请求的结果,再开启蓝牙
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (BluetoothUtil.getBlueToothStatus(this) == true) {
                ck_bluetooth.setChecked(true);
            }
            ck_bluetooth.setOnCheckedChangeListener(this);
            tv_discovery.setOnClickListener(this);
            mBluetooth = BluetoothAdapter.getDefaultAdapter();
            if (mBluetooth == null) {
                Toast.makeText(this, "本机未找到蓝牙功能", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.ck_bluetooth) {
            if (isChecked == true) {
                beginDiscovery();
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent, 1);
                // 下面这行代码为服务端需要，客户端不需要
                mHandler.postDelayed(mAccept, 1000);
            } else {
                cancelDiscovery();
                BluetoothUtil.setBlueToothStatus(this, false);
                mDeviceList.clear();
                BlueListAdapter adapter = new BlueListAdapter(this, mDeviceList);
                lv_bluetooth.setAdapter(adapter);
            }
        }
    }

    private Runnable mAccept = new Runnable() {
        @Override
        public void run() {
            if (mBluetooth.getState() == BluetoothAdapter.STATE_ON) {
                BlueAcceptTask acceptTask = new BlueAcceptTask(true);
                acceptTask.setBlueAcceptListener(BluetoothActivity.this);
                acceptTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_discovery) {
            beginDiscovery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "允许本地蓝牙被附近的其它蓝牙设备发现", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "不允许蓝牙被附近的其它蓝牙设备发现", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            beginDiscovery();
            mHandler.postDelayed(this, 2000);
        }
    };

    private void beginDiscovery() {
        if (mBluetooth.isDiscovering() != true) {
            mDeviceList.clear();
            BlueListAdapter adapter = new BlueListAdapter(BluetoothActivity.this, mDeviceList);
            lv_bluetooth.setAdapter(adapter);
            tv_discovery.setText("正在搜索蓝牙设备");
            mBluetooth.startDiscovery();
        }
    }

    private void cancelDiscovery() {
        mHandler.removeCallbacks(mRefresh);
        tv_discovery.setText("取消搜索蓝牙设备");
        if (mBluetooth.isDiscovering() == true) {
            mBluetooth.cancelDiscovery();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.postDelayed(mRefresh, 50);
        blueReceiver = new BluetoothReceiver();
        //需要过滤多个动作，则调用IntentFilter对象的addAction添加新动作
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        foundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(blueReceiver, foundFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelDiscovery();
        unregisterReceiver(blueReceiver);
    }

    private BluetoothReceiver blueReceiver;

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action=" + action);
            // 获得已经搜索到的蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BlueDevice item = new BlueDevice(device.getName(), device.getAddress(), device.getBondState() - 10);
                mDeviceList.add(item);
                BlueListAdapter adapter = new BlueListAdapter(BluetoothActivity.this, mDeviceList);
                lv_bluetooth.setAdapter(adapter);
                lv_bluetooth.setOnItemClickListener(BluetoothActivity.this);
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mHandler.removeCallbacks(mRefresh);
                tv_discovery.setText("蓝牙设备搜索完成");
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    tv_discovery.setText("正在配对" + device.getName());
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    tv_discovery.setText("完成配对" + device.getName());
                    mHandler.postDelayed(mRefresh, 50);
                } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    tv_discovery.setText("取消配对" + device.getName());
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        cancelDiscovery();
        BlueDevice item = mDeviceList.get(position);
        BluetoothDevice device = mBluetooth.getRemoteDevice(item.address);
        try {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                Log.d(TAG, "开始配对");
                Boolean result = (Boolean) createBondMethod.invoke(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED &&
                    item.state != BlueListAdapter.CONNECTED) {
                tv_discovery.setText("开始连接");
                BlueConnectTask connectTask = new BlueConnectTask(item.address);
                connectTask.setBlueConnectListener(this);
                connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED &&
                    item.state == BlueListAdapter.CONNECTED) {
                tv_discovery.setText("正在发送消息");
                InputDialogFragment dialog = InputDialogFragment.newInstance(
                        "", 0, "请输入要发送的消息");
                String fragTag = getResources().getString(R.string.app_name);
                dialog.show(getFragmentManager(), fragTag);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tv_discovery.setText("配对异常：" + e.getMessage());
        }
    }

    //向对方发送消息
    @Override
    public void onInput(String title, String message, int type) {
        Log.d(TAG, "onInput message=" + message);
        Log.d(TAG, "mBlueSocket is " + (mBlueSocket == null ? "null" : "not null"));
        BluetoothUtil.writeOutputStream(mBlueSocket, message);
    }

    private BluetoothSocket mBlueSocket;

    //客户端主动连接
    @Override
    public void onBlueConnect(String address, BluetoothSocket socket) {
        mBlueSocket = socket;
        tv_discovery.setText("连接成功");
        refreshAddress(address);
    }

    //刷新已连接的状态
    private void refreshAddress(String address) {
        for (int i = 0; i < mDeviceList.size(); i++) {
            BlueDevice item = mDeviceList.get(i);
            if (item.address.equals(address) == true) {
                item.state = BlueListAdapter.CONNECTED;
                mDeviceList.set(i, item);
            }
        }
        BlueListAdapter adapter = new BlueListAdapter(this, mDeviceList);
        lv_bluetooth.setAdapter(adapter);
    }

    //服务端侦听到连接
    @Override
    public void onBlueAccept(BluetoothSocket socket) {
        Log.d(TAG, "onBlueAccept socket is " + (socket == null ? "null" : "not null"));
        if (socket != null) {
            mBlueSocket = socket;
            BluetoothDevice device = mBlueSocket.getRemoteDevice();
            refreshAddress(device.getAddress());
            BlueReceiveTask receive = new BlueReceiveTask(mBlueSocket, mHandler);
            receive.start();
        }
    }

    //收到对方发来的消息
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, "handleMessage readMessage=" + readMessage);
                AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothActivity.this);
                builder.setTitle("我收到消息啦").setMessage(readMessage).setPositiveButton("确定", null);
                builder.create().show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBlueSocket != null) {
            try {
                mBlueSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
