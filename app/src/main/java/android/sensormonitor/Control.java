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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

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
        final ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recDataList);

        Button connectedStatusBtn = (Button)findViewById(R.id.connectedStatus);
        connectedStatusBtn.setText("Connected");
        connectedStatusBtn.setBackgroundColor(GREEN);

        final Button transmissionStatusBtn = (Button)findViewById(R.id.transmissionStatus);

        dataStatus = (Button)findViewById(R.id.dataStatus);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        dataStatus.setText("Initializing system...");

        mHandler = new Handler() {
            public int totalDataCounter;

            private boolean errorStatus = false;


            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    //System.out.println(readMessage);

                    System.out.println("TOTAL COUNTER " + totalDataCounter);
                    System.out.println("WEAR COUNTER " + wearCounter);

                    totalDataCounter += 1;
                    if (readMessage.contains("*")){
                        readMessage = readMessage.replace("*","");
                        wearCounter += 1;
                    }

                    // 10 data sets collected
                    if (totalDataCounter >= 6) {
                        // analyze data
                        if (wearCounter >= 3) {
                            // show error state
                            dataStatus.setText("Critical Error");
                            dataStatus.setBackgroundColor(RED);
                            v.vibrate(800);

                            errorStatus = true;

                        } else {
                            // show ok state
                            if (!errorStatus) {
                                dataStatus.setText("System OK");
                                dataStatus.setBackgroundColor(GREEN);

                            }
                        }
                        totalDataCounter = 0;
                        wearCounter = 0;
                    }

//                    recDataString.append(readMessage);                                      //keep appending to string until ~
//                    recDataList.add(0, readMessage);
                    //recDataList.add(readMessage);
//                    listAdapter.setNotifyOnChange(false);
//                    listAdapter.add(readMessage);
//                    listAdapter.notifyDataSetChanged();
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


