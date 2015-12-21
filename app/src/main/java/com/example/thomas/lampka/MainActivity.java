package com.example.thomas.lampka;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

import static java.lang.System.exit;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    final int REQUEST_ENABLE_BT=1;
    private Button button;
    private TextView upperText;
    private ListView listView;
    private SeekBar seekBar;
    private LinearLayout discoveringLayout;
   //private

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,1);
                if(extra==BluetoothAdapter.STATE_OFF)
                    bluetoothIsOff();
                if(extra==BluetoothAdapter.STATE_ON)
                    bluetoothIsOn();
                if(extra==BluetoothAdapter.STATE_TURNING_OFF)
                    bluetoothIsTurningOff();
                if(extra==BluetoothAdapter.STATE_TURNING_ON)
                    bluetoothIsTurningOn();

            }
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.i("action found","actionfound");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.v("BT dev founded",device.getName());
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                bluetoothIsOn();
            }

            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                discoveringStarted();

            }


        }
    };

    private void discoveringStarted() {
        upperText.setText("wyszukiwanie urządzeń Bluetooth....");
        button.setEnabled(false);
        //czyszczenie kontenerków sparowane urządzenia i urządzenia w pobliżu
        //wypełnianie tych kontenerków danymi i pokazanie ich

        TextView pairedDevicesTextView = (TextView)findViewById(R.id.textView2);
        pairedDevicesTextView.setVisibility(View.VISIBLE);
        ListView pairedDevicesListView = (ListView)findViewById(R.id.listView);
        pairedDevicesListView.setVisibility(View.VISIBLE);





        ArrayAdapter<BTItem> adapter = new ArrayAdapter<BTItem>(this,
                android.R.layout.simple_list_item_1,val);

        pairedDevicesListView.setAdapter(adapter);
        pairedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("on click listener","pos: "+  String.valueOf(position)+ " id: "+String.valueOf(id));
            }
        });
    }

    private void bluetoothIsOff() {
        upperText.setTextColor(Color.RED);
        upperText.setText("Bluetooth wyłączony");
        button.setText("włącz Bluetooth");
        button.setEnabled(true);
        seekBar.setEnabled(false);
        discoveringLayout.setVisibility(View.GONE);

    }

    private void bluetoothIsOn(){
        upperText.setTextColor(Color.BLUE);
        upperText.setText("Bluetooth włączony - brak połączenia z lampką");
        button.setText("wyszukaj lampkę");
        button.setEnabled(true);
        seekBar.setEnabled(false);
        discoveringLayout.setVisibility(View.VISIBLE);

    }

    private void bluetoothIsTurningOff(){
        upperText.setTextColor(Color.GRAY);
        button.setEnabled(false);
        upperText.setText("wyłączanie Bluetooth");

    }

    private void bluetoothIsTurningOn(){
        upperText.setTextColor(Color.BLUE);
        button.setEnabled(false);
        upperText.setText("włączanie Bluetooth");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button)findViewById(R.id.button);
        upperText = (TextView)findViewById(R.id.textView);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        discoveringLayout = (LinearLayout)findViewById(R.id.discoveringLayout);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));


        //bluetoothAdapter = null;
        //check if device has bluetooth
        if(bluetoothAdapter == null){
            upperText.setText("Twoje urządzenie prawdopodobnie nie obsługuje bluetooth");
            upperText.setTextColor(Color.RED);
            button.setEnabled(false);
            return;
        }

        //check if BT is enabled
        if(bluetoothAdapter.isEnabled())
            bluetoothIsOn();
        else
            bluetoothIsOff();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothAdapter.isEnabled())
                    requestBTTurnOn();
                else
                    discoverDevices();
            }
        });

    }

    private void requestBTTurnOn(){
        Log.i("UI event","requestBTTurnOn");
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
    }

    private void discoverDevices(){
        Log.i("UI event","discoverDevices");
        bluetoothAdapter.startDiscovery();


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.i("act result", "on activity result invoked");

    }
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    private class BTItem{
        private BluetoothDevice device;

        BTItem(BluetoothDevice device){
            this.device = device;
        }

        @Override
        public String toString() {
            return device.getName();
        }

        public BluetoothDevice getDevice(){
            return device;
        }
    }

}

