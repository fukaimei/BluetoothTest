package com.fukaimei.bluetoothtest.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fukaimei.bluetoothtest.R;
import com.fukaimei.bluetoothtest.bean.BlueDevice;

public class BlueListAdapter extends BaseAdapter {
    private static final String TAG = "BlueListAdapter";
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<BlueDevice> mBlueList;
    private String[] mStateArray = {"未绑定", "绑定中", "已绑定", "已连接"};
    public static int CONNECTED = 3;

    public BlueListAdapter(Context context, ArrayList<BlueDevice> blue_list) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mBlueList = blue_list;
    }

    @Override
    public int getCount() {
        return mBlueList.size();
    }

    @Override
    public Object getItem(int position) {
        return mBlueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_bluetooth, null);
            holder.tv_blue_name = (TextView) convertView.findViewById(R.id.tv_blue_name);
            holder.tv_blue_address = (TextView) convertView.findViewById(R.id.tv_blue_address);
            holder.tv_blue_state = (TextView) convertView.findViewById(R.id.tv_blue_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final BlueDevice device = mBlueList.get(position);
        holder.tv_blue_name.setText(device.name);
        holder.tv_blue_address.setText(device.address);
        holder.tv_blue_state.setText(mStateArray[device.state]);
        return convertView;
    }

    public final class ViewHolder {
        public TextView tv_blue_name;
        public TextView tv_blue_address;
        public TextView tv_blue_state;
    }

}
