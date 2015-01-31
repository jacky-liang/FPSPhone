package com.fpsphone;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.view.View.OnClickListener;

/**
 * Created by jacky on 1/30/2015.
 */
public class PlayActivity extends Activity {
    private TextView playDebugStatus;
    private ConnectedThread mConnectedThread;
    private Button playTestSend;
    private BluetoothSocket btSocket;
    
    private final String START = "*";
    private final String END = "&";
    private final String PREFIX_KEY = "#";
    private final String PREFIX_BTN = "!";
    private final String PREFIX_MOVE = "~";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        playDebugStatus = (TextView) findViewById(R.id.playDebugStatus);
        playDebugStatus.setText(BluetoothApp.btSocket.getRemoteDevice().getName());
        
        btSocket = BluetoothApp.btSocket;
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
        
        playTestSend = (Button) findViewById(R.id.playTestSend);
        playTestSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playDebugStatus.setText("Sending: " + "W" + " over bt to " + btSocket.getRemoteDevice().getName());
                toggleBtn("U");
                toggleBtn("R");
            }
        });
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