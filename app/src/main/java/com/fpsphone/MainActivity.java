package com.fpsphone;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.fpsphone.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView instructions = (TextView) findViewById(R.id.textView2);
        instructions.setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    public void click_go_to_bluetooth(View view) {
        Intent intent_bluetooth_setup = new Intent(this, SetupBluetoothActivity.class);
        startActivity(intent_bluetooth_setup);
    }
}
