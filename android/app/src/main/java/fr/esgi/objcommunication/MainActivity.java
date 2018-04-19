package fr.esgi.objcommunication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements ColorPickerDialogListener {
    private static final String TAG = "MainActivity";

    private static final int DIALOG_ID_DETECTED = 42;
    private static final int DIALOG_ID_UNDETECTED = 43;

    @BindView(R.id.main_act_edit_address)
    EditText editAddress;

    @BindView(R.id.main_act_btn_connect)
    Button btnConnect;

    @BindView(R.id.main_act_text_status_bt)
    TextView bluetoothStatus;


    @BindView(R.id.main_act_linear_root)
    View linearRoot;

    @BindView(R.id.main_act_text_current_distance)
    TextView currentDistance;

    @BindView(R.id.main_act_frame_current_color)
    FrameLayout currentColor;

    @BindView(R.id.main_act_text_current_door)
    TextView currentDoor;


    @BindView(R.id.main_act_seek_settings_distance_trigger)
    SeekBar seekDistanceTrigger;

    @BindView(R.id.main_act_text_settings_distance_trigger)
    TextView textDistanceTrigger;


    @BindView(R.id.main_act_frame_color_detected)
    FrameLayout frameColorDetected;

    @BindView(R.id.main_act_frame_color_undetected)
    FrameLayout frameColorUndetected;


    final int RECEIVE_MESSAGE = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder stringBuilder = null;


    private @ColorInt int colorDetected;
    private @ColorInt int colorUndetected;
    private boolean doorOpened;

    private int btState = 0;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectedThread connectedThread;
    private Handler h;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        h = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == RECEIVE_MESSAGE) {
                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String(readBuf, 0, msg.arg1);
                    for (char c : strIncom.toCharArray()) {
                        if (c == '@') {
                            stringBuilder = new StringBuilder();
                        } else if (c == '$') {
                            if (stringBuilder != null) {
                                onMessageReceived(stringBuilder.toString());
                            }
                        } else {
                            if (stringBuilder != null) {
                                stringBuilder.append(c);
                            }
                        }
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        seekDistanceTrigger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                connectedThread.write("DT#" + seekBar.getProgress());
            }
        });
    }

    @OnClick(R.id.main_act_btn_connect)
    void onClickConnect() {
        if (btSocket != null && btSocket.isConnected()) {
            disconnect();
            return;
        }
        connect();
    }

    private void connect() {
        BluetoothDevice device = btAdapter.getRemoteDevice(editAddress.getText().toString());
        btSocket = null;
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
            btSocket = (BluetoothSocket) m.invoke(device, MY_UUID);
            Log.d(TAG, "createInsecureRfcomm");
        } catch (Exception e) {
            Log.e(TAG, "createInsecureRfcomm", e);
            Toast.makeText(getApplicationContext(), "Unable to create Insecure RFComm Connection", Toast.LENGTH_LONG).show();
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "createRfcomm");
            } catch (Exception e2) {
                Log.e(TAG, "createRfcomm", e);
                Toast.makeText(getApplicationContext(), "Unable to create RFComm Connection", Toast.LENGTH_LONG).show();
                return;
            }
        }
        btAdapter.cancelDiscovery();

        bluetoothStatus.setText(R.string.main_act_bt_status_connecting);
        try {
            btSocket.connect();
            bluetoothStatus.setText(R.string.main_act_bt_status_connected);
            btnConnect.setText(R.string.main_act_btn_disconnect);
            linearRoot.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Log.e(TAG, "connect", e);
            Toast.makeText(getApplicationContext(), "Unable to connect -> Disconnecting", Toast.LENGTH_LONG).show();
            disconnect();
        }

        connectedThread = new ConnectedThread(btSocket);
        connectedThread.start();

        connectedThread.write("INIT");
    }

    private void disconnect() {
        if (btSocket != null && btSocket.isConnected()) {
            try {
                btSocket.close();
                bluetoothStatus.setText(R.string.main_act_bt_status_disconnected);
            } catch (Exception e) {
                Log.e(TAG, "disconnect", e);
                Toast.makeText(getApplicationContext(), "Unable to disconnect", Toast.LENGTH_LONG).show();
            }
        }
        linearRoot.setVisibility(View.GONE);
        btnConnect.setText(R.string.main_act_btn_connect);
    }


    @OnClick(R.id.main_act_frame_color_detected)
    void setColorDetected() {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(DIALOG_ID_DETECTED)
                .setColor(colorDetected)
                .setShowAlphaSlider(true)
                .show(this);
    }

    @OnClick(R.id.main_act_frame_color_undetected)
    void setColorUndetected() {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(DIALOG_ID_UNDETECTED)
                .setColor(colorUndetected)
                .setShowAlphaSlider(true)
                .show(this);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        String hex = Integer.toHexString(color);
        @ColorInt int colorInt = Color.parseColor("#" + hex);
        switch (dialogId) {
            case DIALOG_ID_DETECTED:
                connectedThread.write("CD#" + hex.substring(2));
                break;

            case DIALOG_ID_UNDETECTED:
                connectedThread.write("CU#" + hex.substring(2));
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    private void onMessageReceived(String s) {
        Log.d(TAG, "onMessageReceived    " + s);
        String[] strings = s.split("#");
        if (strings.length == 2) {
            String cmd = strings[0];
            String arg = strings[1];
            if (cmd.equalsIgnoreCase("DT")) {
                onCommandDistanceTriggerReceived(arg);
            } else if (cmd.equalsIgnoreCase("CD")) {
                onCommandColorDetectedReceived(arg);
            } else if (cmd.equalsIgnoreCase("CU")) {
                onCommandColorUndetectedReceived(arg);
            } else if (cmd.equalsIgnoreCase("DOOR")) {
                onCommandDoorReceived(arg);
            } else if (cmd.equalsIgnoreCase("D")) {
                onCommandDistanceReceived(arg);
            }
        }
    }

    private void onCommandDistanceTriggerReceived(String arg) {
        try {
            int i = Integer.valueOf(arg);
            seekDistanceTrigger.setProgress(i);
            textDistanceTrigger.setText(String.valueOf(i) + " cm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onCommandDoorReceived(String arg) {
        boolean closed = arg.equalsIgnoreCase("0");
        doorOpened = !closed;
        invalidateCurrentStat();
    }

    private void onCommandColorDetectedReceived(String arg) {
        colorDetected = Color.parseColor("#" + arg);
        frameColorDetected.setBackgroundColor(colorDetected);
        invalidateCurrentStat();
    }

    private void onCommandColorUndetectedReceived(String arg) {
        colorUndetected = Color.parseColor("#" + arg);
        frameColorUndetected.setBackgroundColor(colorUndetected);
        invalidateCurrentStat();
    }

    private void onCommandDistanceReceived(String arg) {
        currentDistance.setText(arg);
    }

    private void invalidateCurrentStat() {
        currentDoor.setText(doorOpened ? R.string.main_act_options_door_opened : R.string.main_act_options_door_closed);
        currentColor.setBackgroundColor(doorOpened ? colorDetected : colorUndetected);
    }

    @Override
    public void onPause() {
        super.onPause();
        disconnect();
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    h.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "Data to send: " + message);
            message = "@" + message + "@";
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
}