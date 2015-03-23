package com.fpsphone;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import android.view.View.OnClickListener;

/**
 * Created by jacky on 1/30/2015.
 */
public class SetupBluetoothActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private TextView bluetoothDebugStatus;
    private Button onBtn;
    private Button findBtn;
    private Button confirmBtn;
    private BluetoothSocket btSocket = null;
    private BluetoothAdapter btAdapter;
    private ListView listViewDevices;
    private ArrayAdapter<String> BTArrayAdapter;
    private boolean connected = false;
    private String connectedName;
    private HashMap<String, String> searchedDevices;
    
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private void updateBluetoothStatus() {
        if(btAdapter.isEnabled()) {
            bluetoothDebugStatus.setText("Bluetooth enabled!");
        } else {
            bluetoothDebugStatus.setText("Please enable bluetooth!");
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_bluetooth);

        bluetoothDebugStatus = (TextView) findViewById(R.id.bluetoothDebugStatus);
        
        searchedDevices = new HashMap<String, String>();

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            //setenabled enables buttons
            onBtn.setEnabled(false);
            findBtn.setEnabled(false);
            confirmBtn.setEnabled(false);
            //toast is an on screen notification
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            //Blutooth
            updateBluetoothStatus();

            listViewDevices = (ListView) findViewById(R.id.list_view_devices);
            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            listViewDevices.setAdapter(BTArrayAdapter);
            listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String clicked_name =((TextView) view).getText().toString();
                    String clicked_address = searchedDevices.get(clicked_name);
                    System.out.println(clicked_name);
                    System.out.println(clicked_address);
                    BluetoothDevice device = btAdapter.getRemoteDevice(clicked_address);
                    findBtn.setText(getString(R.string.btn_bluetooth_find));

                    //has not connected to a device yet
                    connect(device);
                }
            });

            confirmBtn = (Button) findViewById(R.id.confirm);
            confirmBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!connected){
                        bluetoothDebugStatus.setText("Not connected to a device yet!");
                    }
                    else{
                        finish_setup_to_play();
                    }
                }
            });
            
            onBtn = (Button) findViewById(R.id.turnOn);
            onBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    on(v);
                }
            });
            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    find(v);
                }
            });
        }
    }

    public void on(View view){
        if (!btAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
            updateBluetoothStatus();
        } else {
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT){
            updateBluetoothStatus();
        }
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName());
                BTArrayAdapter.notifyDataSetChanged();
                
                searchedDevices.put(device.getName(),device.getAddress());
            }
        }
    };

    private void establishConnection(BluetoothDevice device) {
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        try {
            btAdapter.cancelDiscovery();
            btSocket.connect();
            connected = true;
            connectedName = btSocket.getRemoteDevice().getName();
            bluetoothDebugStatus.setText("Connected to " + connectedName);
        } catch (IOException e) {
            try
            {
                btSocket.close();
                bluetoothDebugStatus.setText("Couldn't Connect "+device.getName());
            } catch (IOException e2)
            {}
        }
    }
    
    public void connect(BluetoothDevice device){
        if (btSocket != null && btSocket.isConnected()) {
            if (btSocket.getRemoteDevice().equals(device)) {
                Toast.makeText(getBaseContext(), "Already Connected to "+connectedName, Toast.LENGTH_LONG).show();
            } else {
                try {
                    btSocket.close();    
                } catch (IOException e) {
                    bluetoothDebugStatus.setText("Can't close socket");
                }
                establishConnection(device);
            }
        } else {
            establishConnection(device);
        }
    }
    
    public void find(View view) {
        bluetoothDebugStatus.setText("finding devices");

        if (btAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            btAdapter.cancelDiscovery();
            bluetoothDebugStatus.setText("canceled search.");
            findBtn.setText(getString(R.string.btn_bluetooth_find));
        } else {
            BTArrayAdapter.clear();
            findBtn.setText(getString(R.string.btn_bluetooth_cancel));
            if(connected){
                try{
                    btSocket.close();
                    connected = false;
                } catch (IOException e){
                    bluetoothDebugStatus.setText("closing btsocked failed with "+btSocket.getRemoteDevice().getName());
                }
            }

            btAdapter.startDiscovery();
            bluetoothDebugStatus.setText("searching...");

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(bReceiver);    
        } catch (IllegalArgumentException error) {
            
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public void finish_setup_to_play(){
        Intent intent_play = new Intent(this, PlayActivity.class);
        BluetoothApp.btSocket = btSocket;
        startActivity(intent_play);
    }
}