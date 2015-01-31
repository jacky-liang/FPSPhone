package com.fpsphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by jacky on 1/30/2015.
 */
public class PlayActivity extends Activity {
    private TextView playTestReceiveMsg;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = getIntent();
        String message = intent.getStringExtra(SetupBluetoothActivity.EXTRA_MESSAGE_BLUETOOTH);

        //display message:
        playTestReceiveMsg = (TextView) findViewById(R.id.playActivityReceiveMsg);
        playTestReceiveMsg.setText(message);
        
    }
}