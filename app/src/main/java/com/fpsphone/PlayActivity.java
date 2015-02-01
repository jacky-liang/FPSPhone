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
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
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
    private Button btn_p;
    private Button btn_d;
    private Button btn_s;
    private boolean trackingPaused = false;
    private boolean volume_down_is_down = false;
    private boolean volume_up_is_down = false;
    
    private Button btn_g;

    private SensorManager aSensorManager;
	//to determine maximum and minimum rotation speed along the axis for reload/change weapon
	private LinkedList<Pair<Long, Float>> recentRotationSpeeds;

    private final float EPSILON = 0.01f;
    private final long VIBRATE_PERIOD = 500; //In seconds
    private final float ROT_TO_TRANS = 1.6f;
    private final float ROT_TO_TRANS_FAST = 6f;
    private float CUR_ROT_TO_TRANS = ROT_TO_TRANS;
    
    private final String START = "*";
    private final String END = "&";
    private final String PREFIX_KEY = "#";
    private final String PREFIX_BTN = "!";
    private final String PREFIX_MOVE = "~";

    private Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        debugStatus = (TextView) findViewById(R.id.debugStatus);

        btSocket = BluetoothApp.btSocket;
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        aSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor gyroscope = aSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        aSensorManager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_GAME);

	    recentRotationSpeeds = new LinkedList<Pair<Long, Float>>();
		new GestureMonitor().start();

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

	    vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

	        synchronized(recentRotationSpeeds)
	        {
		        recentRotationSpeeds.add(new Pair<Long, Float>(event.timestamp, axisY));
		        //remove rotation speeds from over a second ago
		        while (event.timestamp - recentRotationSpeeds.peek().first > 1000000000)
			        recentRotationSpeeds.remove();
	        }

            if(!trackingPaused)
            {
                if(sigRotation(axisX) || sigRotation(axisZ))
                    moveMouse(axisX,axisZ);
            }
            else{
                moveMouse(0,0);
            }
        }
    }
    
    public void onAccuracyChanged(Sensor s, int x){
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
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
        }
        return super.onKeyUp(keyCode, event);
    }
    
    
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

	private class GestureMonitor extends Thread
	{
		public void run()
		{
			while(true)
			{
				Pair<Long, Float> max = new Pair<Long, Float>(0L, 0f), min = new Pair<Long, Float>(0L, 99999f);
				synchronized (recentRotationSpeeds)
				{
					for (Pair<Long, Float> rotY : recentRotationSpeeds)
					{
						if (rotY.second > max.second)
							max = rotY;
						if (rotY.second < min.second)
							min = rotY;
					}
					//detect reload
					if (max.second > 5 && min.second < -5 && Math.abs(recentRotationSpeeds.peekLast().second) < 2
							&& Math.abs(max.first - min.first) * (max.second + min.second) >= 1.5)
					{
						System.out.println("reload! direction = " + Math.signum(min.first - max.first));
						//if max.first < min.first, the phone was turned right then left, vice versa
						//reset so we don't get multiple reload notifications
						recentRotationSpeeds.clear();
					}
				}
				try
				{
					Thread.sleep(5);
				}catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
