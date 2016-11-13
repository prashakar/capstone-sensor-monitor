package android.sensormonitor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.YELLOW;

public class Control extends AppCompatActivity {
    private Handler mHandler;
    private final int handlerState = 0;
    private BluetoothSocket socket;
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private int wearCounter;
    private Button dataStatus;
    private Vibrator v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BluetoothDevice foundDevice = getIntent().getExtras().getParcelable("FOUND_DEVICE");
        try {
            socket = foundDevice.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);
        final StringBuilder recDataString = new StringBuilder();
        final ArrayList<String> recDataList = new ArrayList<>();
        final ListView dataList = (ListView)findViewById(R.id.dataList);
        final ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recDataList);
        dataList.setAdapter(listAdapter);

        Button connectedStatusBtn = (Button)findViewById(R.id.connectedStatus);
        connectedStatusBtn.setText("Connected");
        connectedStatusBtn.setBackgroundColor(GREEN);

        final Button transmissionStatusBtn = (Button)findViewById(R.id.transmissionStatus);

        dataStatus = (Button)findViewById(R.id.dataStatus);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
//
//        Thread t = new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    while (!isInterrupted()) {
//                        Thread.sleep(1000);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (wearCounter > 17){
//                                    dataStatus.setText("Warning");
//                                    dataStatus.setBackgroundColor(YELLOW);
//                                } else if (wearCounter > 30) {
//                                    dataStatus.setText("Error");
//                                    dataStatus.setBackgroundColor(RED);
//                                } else if (wearCounter == 0){
//
//                                } else {
//                                    dataStatus.setText("System OK" + wearCounter);
//                                    dataStatus.setBackgroundColor(GREEN);
//                                }
//
//                            }
//                        });
//                    }
//                } catch (InterruptedException e) {
//                }
//            }
//        };
//        t.start();

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    //System.out.println(readMessage);
                    if (readMessage.contains("*")){
                        readMessage = readMessage.replace("*","");
                        wearCounter += 1;
                    }

                    if (wearCounter == 50){
                        v.vibrate(100);
                    } else if (wearCounter == 70){
                        v.vibrate(100);
                    } else if (wearCounter == 90){
                        v.vibrate(100);
                    }
                    if (wearCounter > 30) {
                        dataStatus.setText("Critical Error " + wearCounter);
                        dataStatus.setBackgroundColor(RED);

                    } else if (wearCounter > 17){
                        dataStatus.setText("Warning");
                        dataStatus.setBackgroundColor(YELLOW);
                    } else if (wearCounter == 0){
                    } else {
                        dataStatus.setText("System OK");
                        dataStatus.setBackgroundColor(GREEN);
                    }
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    recDataList.add(0, readMessage);
                    //recDataList.add(readMessage);
//                    listAdapter.setNotifyOnChange(false);
//                    listAdapter.add(readMessage);
                    listAdapter.notifyDataSetChanged();
                    int endOfLineIndex = recDataString.indexOf("END");                    // determine the end-of-line

                    if (recDataString.toString().contains("~")) {
                        transmissionStatusBtn.setText("Data monitoring stopped");
                        transmissionStatusBtn.setBackgroundColor(GRAY);
                        dataStatus.setText("Critical wear detected!");
                        dataStatus.setTextSize(40);
                        dataStatus.setBackgroundColor(BLACK);
                        dataStatus.setTextColor(RED);
                        dataStatus.setElevation(30);
                        v.vibrate(800);


                    } else{
                        transmissionStatusBtn.setText("Data monitoring ongoing");
                        transmissionStatusBtn.setBackgroundColor(GREEN);
                    }


//                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
//                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
//                        txtString.setText("Data Received = " + dataInPrint);
//                        int dataLength = dataInPrint.length();                          //get length of data received
//                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));
//
//                        if (recDataString.charAt(0) == '#')                             //if it starts with # we know it is what we are looking for
//                        {
//                            String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
//                            String sensor1 = recDataString.substring(6, 10);            //same again...
//                            String sensor2 = recDataString.substring(11, 15);
//                            String sensor3 = recDataString.substring(16, 20);
//
//                            sensorView0.setText(" Sensor 0 Voltage = " + sensor0 + "V");    //update the textviews with sensor values
//                            sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "V");
//                            sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V");
//                            sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
//                        }
//                        recDataString.delete(0, recDataString.length());                    //clear all string data
//                        // strIncom =" ";
//                        dataInPrint = " ";
//                    }
                }
            }
        };
        ConnectedThread connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[128];  // buffer store for the stream
            int bytes; // bytes returned from read()
            StringBuilder completeMessage = new StringBuilder();
            //String completeMessage = "";

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    System.out.println(readMessage);
                    mHandler.obtainMessage(handlerState, bytes, -1, readMessage)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public void backDevices(View v){
        finish();
    }

}


