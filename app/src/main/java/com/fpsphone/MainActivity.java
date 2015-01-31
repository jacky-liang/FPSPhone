package com.fpsphone;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends Activity implements SensorEventListener{

    private TextView debugStatus;
    private Button btn_w;
    private Button btn_a;
    private Button btn_p;
    private Button btn_d;
    private Button btn_s;

    private SensorManager aSensorManager;
    private Sensor gyroscope;
    private TextView debugGyroX;
    private TextView debugGyroY;
    private TextView debugGyroZ;

    private final float EPSILON = 0.01f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debugStatus = (TextView) findViewById(R.id.debugStatus);

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
        btn_a = (Button) findViewById(R.id.buttonA);
        btn_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugStatus.setText("Pressed A");
            }
        });
        btn_p = (Button) findViewById(R.id.buttonPause);
        btn_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugStatus.setText("Pressed Pause");
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                debugStatus.setText("Pressed Volume Up");
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                debugStatus.setText("Pressed Volume Down");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onAccuracyChanged(Sensor s, int x){
        
        
    }


}
