package com.fukaimei.bluetoothtest.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InputDialogFragment extends DialogFragment {
    private static final String TAG = "InputDialogFragment";
    private LinearLayout root;
    private String mSSID;
    private int mType;
    private String mMessage;
    private InputCallbacks mCallbacks;
    private TextView tv_message;
    private EditText et_input;

    public static InputDialogFragment newInstance(String ssid, int type, String message) {
        InputDialogFragment frag = new InputDialogFragment();
        Bundle args = new Bundle();
        args.putString("ssid", ssid);
        args.putInt("type", type);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSSID = getArguments().getString("ssid");
        mType = getArguments().getInt("type");
        mMessage = getArguments().getString("message");
        mCallbacks = (InputCallbacks) activity;
    }

    public interface InputCallbacks {
        public void onInput(String SSID, String password, int type);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LinearLayout.LayoutParams rootLayout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F);
        root = new LinearLayout(getActivity());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(rootLayout);

        tv_message = new TextView(getActivity());
        tv_message.setText(mMessage);
        root.addView(tv_message);
        et_input = new EditText(getActivity());
        root.addView(et_input);

        Builder popupBuilder = new Builder(getActivity());
        popupBuilder.setView(root);
        //popupBuilder.setMessage(mMessage);
        popupBuilder.setPositiveButton("确  定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "onClick ssid=" + mSSID + ",password=" + et_input.getText().toString() + ",type=" + mType);
                        mCallbacks.onInput(mSSID, et_input.getText().toString(), mType);
                    }
                });
        return popupBuilder.create();
    }

}
