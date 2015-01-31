package com.fpsphone;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jacky on 1/30/2015.
 */
public class PlayActivity extends Activity implements SensorEventListener {
    private ConnectedThread mConnectedThread;
    private BluetoothSocket btSocket;
    private SensorManager aSensorManager;
    private Sensor gyroscope;
    private Vibrator vib;
    
    private Button btn_w;
    private Button btn_a;
    private Button btn_p;
    private Button btn_d;
    private Button btn_s;
    private Button btn_g;

    private boolean trackingPaused = false;
    private boolean volume_down_is_down = false;
    private boolean volume_up_is_down = false;

    private TextView debugStatus;
    private TextView debugGyroX;
    private TextView debugGyroY;
    private TextView debugGyroZ;

    private final float EPSILON = 0.01f;
    private final long VIBRATE_PERIOD = 70; //In seconds
    private final float ROT_TO_TRANS = 1.6f;
    private final float ROT_TO_TRANS_FAST = 6f;
    private float CUR_ROT_TO_TRANS = ROT_TO_TRANS;
    
    private final String START = "*";
    private final String END = "&";
    private final String PREFIX_KEY = "#";
    private final String PREFIX_BTN = "!";
    private final String PREFIX_MOVE = "~";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        
        //Element Creation
        debugStatus = (TextView) findViewById(R.id.debugStatus);

        btSocket = BluetoothApp.btSocket;
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        aSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscope = aSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        aSensorManager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_GAME);
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        debugGyroX = (TextView) findViewById(R.id.debugGyroX);
        debugGyroY = (TextView) findViewById(R.id.debugGyroY);
        debugGyroZ = (TextView) findViewById(R.id.debugGyroZ);

        debugGyroX.setText("X");
        debugGyroY.setText("Y");
        debugGyroZ.setText("Z");

        //Button event handlers
        btn_w = (Button) findViewById(R.id.buttonW);
        btn_w.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        debugStatus.setText("W Down");
                        toggleKey("W");
                        break;
                    case MotionEvent.ACTION_UP:
                        debugStatus.setText("W Up");
                        toggleKey("W");
                        break;
                }
                return true;
            }
        });
        btn_g = (Button) findViewById(R.id.buttonG);
        btn_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugStatus.setText("Pressed G");
            }
        });
        btn_a = (Button) findViewById(R.id.buttonA);
        btn_a.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        debugStatus.setText("A Down");
                        toggleKey("A");
                        break;
                    case MotionEvent.ACTION_UP:
                        debugStatus.setText("A Up");
                        toggleKey("A");
                        break;
                }
                return true;
            }
        });
        btn_p = (Button) findViewById(R.id.buttonPause);
        btn_p.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        debugStatus.setText("P Down");
                        trackingPaused = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        debugStatus.setText("P Up");
                        trackingPaused = false;
                        break;
                }
                return true;
            }
        });
        btn_d = (Button) findViewById(R.id.buttonD);
        btn_d.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        debugStatus.setText("D Down");
                        toggleKey("D");
                        break;
                    case MotionEvent.ACTION_UP:
                        debugStatus.setText("D Up");
                        toggleKey("D");
                        break;
                }
                return true;
            }
        });
        btn_s = (Button) findViewById(R.id.buttonS);
        btn_s.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        debugStatus.setText("S Down");
                        toggleKey("S");
                        break;
                    case MotionEvent.ACTION_UP:
                        debugStatus.setText("S Up");
                        toggleKey("S");
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            //Debug sensor
            if(axisX > EPSILON){
                debugGyroX.setText(Float.toString(axisX));
            }
            else{
                debugGyroX.setText("0");
            }
            if(axisY > EPSILON){
                debugGyroY.setText(Float.toString(axisY));
            }
            else{
                debugGyroY.setText("0");
            }
            if(axisZ > EPSILON){
                debugGyroZ.setText(Float.toString(axisZ));
            }
            else{
                debugGyroZ.setText("0");
            }
            
            //For tracking mouse
            if(!trackingPaused && (sigRotation(axisX) || sigRotation(axisZ)))
                moveMouse(axisX,axisZ);
            else
                moveMouse(0,0);
        }

    }
    
    public void onAccuracyChanged(Sensor s, int x){
    }

    //For Volume Buttons
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                debugStatus.setText("Pressed Volume Up");
                if(!volume_up_is_down){
                    toggleBtn("L");
                    volume_up_is_down = true;
                }
                vib.vibrate(VIBRATE_PERIOD); //vibrate when you fire.
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                debugStatus.setText("Pressed Volume Down");
                if(!volume_down_is_down){
                    CUR_ROT_TO_TRANS = ROT_TO_TRANS_FAST;
                    volume_down_is_down = true;
                }
                break;
            default:
                result = super.onKeyDown(keyCode, event);
                break;
        }
        return result;
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        boolean result = true;
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                debugStatus.setText("Pressed Volume Up");
                if(volume_up_is_down){
                    toggleBtn("L");
                    volume_up_is_down = false;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                debugStatus.setText("Pressed Volume Down");
                if(volume_down_is_down){
                    CUR_ROT_TO_TRANS  = ROT_TO_TRANS;
                    volume_down_is_down = false;
                }
                break;
            default:
                result = super.onKeyUp(keyCode, event);
                break;
        }
        return result;
    }
    
    //Bluetooth Helper Function
    private void toggleKey(String key){
        String msg = bt_encapsulate(PREFIX_KEY + key);
        mConnectedThread.write(msg);
    }
    
    private void toggleBtn(String btn){
        String msg = bt_encapsulate(PREFIX_BTN + btn);
        mConnectedThread.write(msg);
    }
    
    private void moveMouse(float axisX, float axisZ){
        float velocityHoriz = CUR_ROT_TO_TRANS * axisX * -1;
        float velocityVerti = CUR_ROT_TO_TRANS * axisZ * -1;
        String msg = bt_encapsulate(PREFIX_MOVE + Float.toString(velocityHoriz) + "|" + Float.toString(velocityVerti));
        mConnectedThread.write(msg);
    }
    
    private boolean sigRotation(float val){
        return Math.abs(val) > EPSILON;
    }

    private String bt_encapsulate(String s){
        return START + s + END;
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}    
