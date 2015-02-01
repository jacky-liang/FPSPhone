package com.fpsphone;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.fpsphone.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void click_go_to_bluetooth(View view) {
        Intent intent_bluetooth_setup = new Intent(this, SetupBluetoothActivity.class);
        String tstMsg = "hi there";
        intent_bluetooth_setup.putExtra(EXTRA_MESSAGE, tstMsg);
        startActivity(intent_bluetooth_setup);
    }
}
