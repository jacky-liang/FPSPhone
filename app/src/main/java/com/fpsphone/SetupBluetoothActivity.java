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
import java.util.Set;
import java.util.UUID;
import android.view.View.OnClickListener;

/**
 * Created by jacky on 1/30/2015.
 */
public class SetupBluetoothActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private TextView bluetoothDebugStatus;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private Button confirmBtn;
    private BluetoothSocket btSocket = null;
    private BluetoothAdapter btAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView listViewDevices;
    private ArrayAdapter<String> BTArrayAdapter;
    private boolean connected = false;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_bluetooth);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        
        //display message:
        bluetoothDebugStatus = (TextView) findViewById(R.id.bluetoothDebugStatus);
        bluetoothDebugStatus.setText(message);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            //setenabled enables buttons
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            confirmBtn.setEnabled(false);
            bluetoothDebugStatus.setText("Status: not supported");

            //toast is an on screen notification
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            //Blutooth

            bluetoothDebugStatus.setText("Bluetooth Status: "+ Integer.toString(btAdapter.getState()));

            listViewDevices = (ListView) findViewById(R.id.list_view_devices);
            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            listViewDevices.setAdapter(BTArrayAdapter);
            listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String name_and_address =((TextView) view).getText().toString();
                    String clicked_address = name_and_address.substring(name_and_address.indexOf("\n")+1);
                    BluetoothDevice device = btAdapter.getRemoteDevice(clicked_address);
                    Toast.makeText(getApplicationContext(), clicked_address, Toast.LENGTH_SHORT).show();
                    
                    //has not connected to a device yet
                    connect(device);
                }
            });

            confirmBtn = (Button) findViewById(R.id.confirm);
            confirmBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //finish_setup_to_play();
                    finish_setup_to_play();

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
            offBtn = (Button)findViewById(R.id.turnOff);
            offBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    off(v);
                }
            });
            listBtn = (Button)findViewById(R.id.paired);
            listBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    list(v);
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
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT){
            if(btAdapter.isEnabled()) {
                bluetoothDebugStatus.setText("Status: Enabled");
            } else {
                bluetoothDebugStatus.setText("Status: Disabled");
            }
        }
    }

    public void list(View view){
        // get paired devices
        pairedDevices = btAdapter.getBondedDevices();

        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

        Toast.makeText(getApplicationContext(),"Show Paired Devices",
                Toast.LENGTH_SHORT).show();
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void connect(BluetoothDevice device){
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btAdapter.cancelDiscovery();
            btSocket.connect();
            connected = true;
            bluetoothDebugStatus.setText("connected with " + device.getAddress());
        } catch (IOException e) {
            try
            {
                btSocket.close();
                bluetoothDebugStatus.setText("btsocket connect failure: "+device.getName());
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
    }
    
    public void find(View view) {
        bluetoothDebugStatus.setText("finding devices");

        if (btAdapter.isDiscovering()) {
            bluetoothDebugStatus.setText("cancelling search");
            // the button is pressed when it discovers, so cancel the discovery
            btAdapter.cancelDiscovery();
            bluetoothDebugStatus.setText("canceled search");
        }
        else {
            BTArrayAdapter.clear();
            String closed_msg = "";
            if(connected){
                try{
                    String name = btSocket.getRemoteDevice().getName();
                    btSocket.close();
                    connected = false;
                    closed_msg = "closing " + name +" .";
                } catch (IOException e){
                    bluetoothDebugStatus.setText("closing btsocked failed with "+btSocket.getRemoteDevice().getName());
                }
            }

            btAdapter.startDiscovery();
            bluetoothDebugStatus.setText(closed_msg+"searching...");

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void off(View view){
        btAdapter.disable();
        bluetoothDebugStatus.setText("Status: Disconnected");

        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
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