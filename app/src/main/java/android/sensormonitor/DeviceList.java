package android.sensormonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class DeviceList extends AppCompatActivity {

    private BluetoothAdapter myBluetooth = null;
    private Button btnPaired;
    private ListView deviceList;
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothSocket socket;
    private Handler mHandler;
    private final int handlerState = 0;
    private BluetoothDevice foundDevice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

    }

    @Override
    protected void onResume(){
        super.onResume();

        btnPaired = (Button) findViewById(R.id.button);
        deviceList = (ListView) findViewById(R.id.listView);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            //Show a message that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!myBluetooth.isEnabled()) {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }
    }

    public void showDevices(View v) {
        pairedDevicesList(); //method that will be called
    }

    private void pairedDevicesList() {
        Set pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0) {
            for(Object bt : pairedDevices) {
                BluetoothDevice device = (BluetoothDevice) bt;
                if (device.getName().equals("Capstone")) {
                    foundDevice = (BluetoothDevice) bt;
                    Log.v("findDevices", "found intended server!");
                } else {
                    Log.v("findDevices", "not intended server");
                }
                list.add(device.getName() + "\n" + device.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }


        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked


    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int pos, long id) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            // Make an intent to start next activity.
            Log.v("connectingManager", "CONNECTING TO DEVICES " + address);
            Toast.makeText(getApplicationContext(), "CONNECTING TO DEVICE " + address, Toast.LENGTH_LONG).show();

            Intent i = new Intent(DeviceList.this, Control.class);
            //Change the activity.
            i.putExtra("FOUND_DEVICE", foundDevice);
            startActivity(i);
        }
    };


}

