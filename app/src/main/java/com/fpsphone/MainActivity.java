package com.fpsphone;

import android.app.Activity;
import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.fpsphone.MESSAGE";

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

    public void click_go_to_bluetooth(View view) {
        Intent intent_bluetooth_setup = new Intent(this, SetupBluetoothActivity.class);
        String tstMsg = "hi there";
        intent_bluetooth_setup.putExtra(EXTRA_MESSAGE, tstMsg);
        startActivity(intent_bluetooth_setup);
    }
}
