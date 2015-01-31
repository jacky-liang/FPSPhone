package com.fpsphone;

import android.app.Activity;
import android.os.Bundle;
//import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView debugStatus;
    private Button btn_w;
    private Button btn_a;
    private Button btn_p;
    private Button btn_d;
    private Button btn_s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debugStatus = (TextView) findViewById(R.id.debugStatus);

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



}
