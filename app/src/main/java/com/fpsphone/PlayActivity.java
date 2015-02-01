package com.fpsphone;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Pair;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.HashMap;

/**
 * Created by jacky on 1/30/2015.
 */
public class PlayActivity extends Activity implements SensorEventListener {
    private ConnectedThread mConnectedThread;
    private BluetoothSocket btSocket;
    private SensorManager aSensorManager;
    private Vibrator vib;

    private boolean trackingPaused = false;
    private boolean volume_down_is_down = false;
    private boolean volume_up_is_down = false;

	//to determine maximum and minimum rotation speed along the axis for reload/change weapon
	private LinkedList<Pair<Long, Float>> recentRotationSpeeds;

    private ImageView joystick;

    private final static float EPSILON = 0.001f;
    private final static long VIBRATE_PERIOD = 70; //In seconds
    private final static float ROT_TO_TRANS = 1.8f;
    private final static float ROT_TO_TRANS_FAST = 5.5f;
    private float CUR_ROT_TO_TRANS = ROT_TO_TRANS;

    private final static String START = "*";
    private final static String END = "&";
    private final static String PREFIX_KEY = "#";
    private final static String PREFIX_BTN = "!";
    private final static String PREFIX_MOVE = "~";

    private HashMap<Character, Boolean> pressed_keys = new HashMap<Character, Boolean>();
    private final String[] regions = {"D","WD","W","WA","A","AS","S","SD"};
    private int last_region = -1;

    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    Float origin_offset_x;
    Float origin_offset_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        pressed_keys.put('W',false);
        pressed_keys.put('A',false);
        pressed_keys.put('S',false);
        pressed_keys.put('D',false);
        pressed_keys.put(' ',false);

        //Element Creation
        btSocket = BluetoothApp.btSocket;
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        aSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor gyroscope = aSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        aSensorManager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_GAME);
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);

	    recentRotationSpeeds = new LinkedList<Pair<Long, Float>>();
		new GestureMonitor().start();

        joystick = (ImageView) findViewById(R.id.joystick);
        joystick.setAlpha(0f);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){ //user releases touch
            unpress_all_keys();
            origin_offset_x = null;
            origin_offset_y = null;
            joystick.setAlpha(0.5f);
        }
        else{   //When user touches
            if (origin_offset_x != null && origin_offset_y != null) {   //continuing to touch
                float x_corrected = event.getX() - origin_offset_x;
                float y_corrected = origin_offset_y - event.getY();
                float ref_angle = (float) Math.abs(Math.atan(y_corrected/x_corrected));
                if (x_corrected >= 0 && y_corrected >= 0) {  //Quadrant I
                    //Nothing happens ref_angle is the same
                } else if (x_corrected < 0 && y_corrected > 0) {    //Quadrant II
                    ref_angle = (float) Math.PI - ref_angle;
                } else if (x_corrected <= 0 && y_corrected <= 0) {    //Quadrant III
                    ref_angle = (float) Math.PI + ref_angle;
                } else if (x_corrected > 0 && y_corrected < 0) {    //Quadrant IV
                    ref_angle = 2* (float)Math.PI - ref_angle;
                }

                int region = (int) Math.floor((ref_angle+Math.PI/8)*4/Math.PI)%8;
                if(region != last_region){
                    //turn off last region
                    if(last_region != -1)
                        turnOff(last_region);
                    //turn on current region
                    turnOn(region);
                    last_region = region;
                }

                if (event.getPointerCount() > 1) {  //If Multitouch
                    if (event.getAction() == event.ACTION_POINTER_DOWN) {
                        toggleKey(" ");
                    }
                }
            }
            else {  //First time touching.
                origin_offset_x = new Float(event.getX());
                origin_offset_y = new Float(event.getY());
                Log.i("Test", origin_offset_x + ", " + origin_offset_y);
                joystick.setX(origin_offset_x-joystick.getWidth()/2);
                joystick.setY(origin_offset_y-joystick.getHeight());
                joystick.setAlpha(1.0f);
            }
        }

        if (event.getAction() == event.ACTION_POINTER_UP) {
            toggleKey(" ");
            pressed_keys.put(' ', true);
        }

        return true;
    }

    private void unpress_all_keys(){
        for(char c : "WASD ".toCharArray()){
            if(pressed_keys.get(c)){
                toggleKey(c+"");
                pressed_keys.put(c,false);
            }
        }
    }    
    
    private void turnOff(int region){
        String keys = regions[region];
        String temp = "";
        for(char c : keys.toCharArray()){
            if(pressed_keys.get(c)){
                toggleKey(c+"");
                pressed_keys.put(c,false);
                temp += ""+c;
            }
        }
        Log.i("Test", "Turning off "+temp);
    }

    private void turnOn(int region){
        String keys = regions[region];
        String temp = "";
        for(char c : keys.toCharArray()){
            if(!pressed_keys.get(c)){
                pressed_keys.put(c, true);
                toggleKey(c + "");
                temp += ""+c;
            }
        }
        Log.i("Test", "Turning on "+temp);
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
		        //remove rotation speeds from over 0.8 seconds ago
		        while (event.timestamp - recentRotationSpeeds.peek().first > 800000000)
			        recentRotationSpeeds.remove();
	        }

            if(!trackingPaused)
            {
                if(sigRotation(axisX) || sigRotation(axisZ))
                    moveMouse(axisX,axisZ);
            }else
            {
	            moveMouse(0, 0);
            }
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
                Log.i("Test", "Pressed Volume Up");
                if(!volume_up_is_down){
                    toggleBtn("L");
                    volume_up_is_down = true;
                }
                vib.vibrate(VIBRATE_PERIOD); //vibrate when you fire.
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.i("Test", "Pressed Volume Down");
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
                Log.i("Test", "Pressed Volume Up");
                if(volume_up_is_down){
                    toggleBtn("L");
                    volume_up_is_down = false;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.i("Test", "Pressed Volume Down");
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
        if (velocityHoriz > 0){
            velocityHoriz *= 1.3;
        }

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
				Pair<Long, Float> max = new Pair<Long, Float>(0L, 0f), min = new Pair<Long, Float>(0L, 0f);
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
					//a is the max speed when turning away from center, b when returning
					float a = max.second, b = -min.second; //right then left
					if(min.first < max.first) //left then right
					{
						a = -min.second;
						b = max.second;
					}
					if (a > 6 && b > 3 //allow turning back to be slower
							&& Math.abs(recentRotationSpeeds.peekLast().second) < 2 //make sure we're almost done turning back
							&& Math.abs(max.first - min.first) / 1000000000f * (max.second - min.second) >= 1.5) //enough magnitude of turn
					{
						System.out.println("reload! direction = " + Math.signum(min.first - max.first));
						//if max.first < min.first, the phone was turned right then left, vice versa
                        if(max.first < min.first)
                            toggleBtn("U");
                        else{
                            toggleKey("R");
                            toggleKey("R");
                        }
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
