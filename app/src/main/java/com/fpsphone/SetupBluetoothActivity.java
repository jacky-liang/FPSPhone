package com.fpsphone;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by jacky on 1/30/2015.
 */
public class SetupBluetoothActivity extends Activity {
    public final static String EXTRA_MESSAGE_BLUETOOTH = "com.fpsphone.BLUETOOTH";
    
    private TextView bluetoothTestReceiveMsg;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_bluetooth);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        
        //display message:
        bluetoothTestReceiveMsg = (TextView) findViewById(R.id.bluetoothActivityReceiveMsg);
        bluetoothTestReceiveMsg.setText(message);

    }
    
    public void finish_setup_to_play(View view){
        Intent intent_play = new Intent(this, PlayActivity.class);
        String tstMsg = "GOT BLUETOOTH";
        intent_play.putExtra(EXTRA_MESSAGE_BLUETOOTH, tstMsg);
        startActivity(intent_play);
        
    }
}