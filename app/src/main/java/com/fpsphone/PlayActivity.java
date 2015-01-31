package com.fpsphone;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.view.View.OnClickListener;

/**
 * Created by jacky on 1/30/2015.
 */
public class PlayActivity extends Activity implements SensorEventListener {
    private ConnectedThread mConnectedThread;
    private BluetoothSocket btSocket;

    private TextView debugStatus;
    private Button btn_w;
    private Button btn_a;
    //private Button btn_p;
    private Button btn_d;
    private Button btn_s;
    private Button btn_g;

    private Display display;
    private int stageWidth;
    private int stageHeight;
    private Point size;

    private ImageView joystick;

    private SensorManager aSensorManager;
    private Sensor gyroscope;
    private TextView debugGyroX;
    private TextView debugGyroY;
    private TextView debugGyroZ;

    private final float EPSILON = 0.01f;
    private final long vibrateLength = 1; //In seconds
    
    private final String START = "*";
    private final String END = "&";
    private final String PREFIX_KEY = "#";
    private final String PREFIX_BTN = "!";
    private final String PREFIX_MOVE = "~";

    private Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        debugStatus = (TextView) findViewById(R.id.debugStatus);


        btSocket = BluetoothApp.btSocket;
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        stageWidth = size.x;
        stageHeight = size.y;

        aSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscope = aSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        aSensorManager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_NORMAL);

        debugGyroX = (TextView) findViewById(R.id.debugGyroX);
        debugGyroY = (TextView) findViewById(R.id.debugGyroY);
        debugGyroZ = (TextView) findViewById(R.id.debugGyroZ);

        debugGyroX.setText("X");
        debugGyroY.setText("Y");
        debugGyroZ.setText("Z");

        btn_w = (Button) findViewById(R.id.buttonW);
        btn_w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugStatus.setText("Pressed W");
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
        btn_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugStatus.setText("Pressed A");
            }
        });

        joystick = (ImageView) findViewById(R.id.joystick);
        joystick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float touchX = event.getX();
                    float touchY = event.getY();
                    debugStatus.setText("Touch Coords: " + String.valueOf(touchX) + "x" + String.valueOf(touchY));

                    float originX = (float)(stageWidth / 2);
                    float originY = (float)(stageHeight / 2);

                    float delta = (float) Math.abs(Math.atan((event.getY() - originY) / (event.getX() - originX)));
                    if (touchX >= 0 && touchY >= 0) {   //Quadrant I
                        if (delta > 45) {
                            debugStatus.setText("Move W");
                        }
                        else {
                            debugStatus.setText("Move D");
                        }
                        //Nothing happens delta is the same
                    } else if (touchX < 0 && touchY > 0) {    //Quadrant II
                        delta = 180 - delta;
                        if (delta < 135) {
                            debugStatus.setText("Move W");
                        }
                        else {
                            debugStatus.setText("Move A");
                        }
                    } else if (touchX <= 0 && touchY <= 0) {    //Quadrant III
                        delta = 180 + delta;
                        if (delta < 225) {
                            debugStatus.setText("Move A");
                        }
                        else {
                            debugStatus.setText("Move S");
                        }
                    } else if (touchX > 0 && touchY < 0) {    //Quadrant IV
                        delta = 360 - delta;
                        if (delta < 315) {
                            debugStatus.setText("Move S");
                        }
                        else {
                            debugStatus.setText("Move D");
                        }
                    }
                }
                return true;
            }
        });

        /*btn_p = (Button) findViewById(R.id.buttonPause);
        btn_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugStatus.setText("Pressed Pause");
            }
        });*/
        btn_d = (Button) findViewById(R.id.buttonD);
        btn_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugStatus.setText("Pressed D");
            }
        });
        btn_s = (Button) findViewById(R.id.buttonS);
        btn_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugStatus.setText("Pressed S");
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

            if(axisX > EPSILON)
                debugGyroX.setText(Float.toString(axisX));
            else
                debugGyroX.setText("0");
            if(axisY > EPSILON)
                debugGyroY.setText(Float.toString(axisY));
            else
                debugGyroY.setText("0");
            if(axisZ > EPSILON)
                debugGyroZ.setText(Float.toString(axisZ));
            else
                debugGyroZ.setText("0");
        }

    }
    
    public void onAccuracyChanged(Sensor s, int x){
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:        //Fire
                debugStatus.setText("Pressed Volume Up");
                vib.vibrate(vibrateLength); //vibrate when you fire.
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:      //ADS
                debugStatus.setText("Pressed Volume Down");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void toggleKey(String key){
        String msg = bt_encapsulate(PREFIX_KEY + key);
        mConnectedThread.write(msg);
    }
    
    private void toggleBtn(String btn){
        String msg = bt_encapsulate(PREFIX_BTN + btn);
        mConnectedThread.write(msg);
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
