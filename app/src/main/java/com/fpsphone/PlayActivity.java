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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        playDebugStatus = (TextView) findViewById(R.id.playDebugStatus);
        playDebugStatus.setText(BluetoothApp.btSocket.getRemoteDevice().getName());
        
        btSocket = BluetoothApp.btSocket;
        
        playTestSend = (Button) findViewById(R.id.playTestSend);
        playTestSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();

                String msg = "*#W&";
                playDebugStatus.setText("Sending: " + msg + " over bt to " + btSocket.getRemoteDevice().getName());
                mConnectedThread.write(msg);
            }
        });
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