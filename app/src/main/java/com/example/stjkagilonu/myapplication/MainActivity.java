package com.example.stjkagilonu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    //Log strings
    private static final String TAG = "Kumbara-TL";
    private static final String TAGB = "Bluetooth";
    //Server that stores the information
    public static final String url = "https://api.thingspeak.com";
    //
    private int operationCounter = 0;

    //Information regarding to Kumbara are stored in the following fields. The info will be taken from the server
    String currentTotalTLAmount, lastCallTimeInfo, lastCallAmountInfo, lastSenderInfo, senderInfo, totalSumBES, totalSumG, lastCallType;

    //Necessary elements for increasing/decreasing the amount. Addition/subtraction operations cycle through the array
    //TL
    int counter_tl = 0;
    int base_int_tl = 20; //Starting amount of the send-TL operation
    int max_int_tl = 200; //Maximum amount that can be send via TL transaction
    private int amountOrder_tl[] = {base_int_tl, 50, 100, max_int_tl};
    //BES
    int base_int_bes = 150; //Starting amount of the send-BES operation
    int bes_increaseAmount = 50; //Operations on the amount to be send will be made accordingly
    //Gold
    int counter_g;
    double base_int_g = 0.5;
    double max_int_g = 100;
    private double amountOrder_gold[] = {base_int_g, 1, 2.5, 5, 10, 20, 50, max_int_g};

    //Retrofit constants
    RestAdapter radapter = new RestAdapter.Builder().setEndpoint(url).build();
    final MInterface restInt = radapter.create(MInterface.class);
    //Checking if the send button clicked
    boolean sendEnabled = false;

    ImageView money;
   // AnimatorSet moveUp;
    Animation moveUp_text, moveUp;

    //Bluetooth connection elements
    protected final String DEVICE_NAME = "maxibot";
    protected final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    protected BluetoothDevice device;
    protected BluetoothSocket socket;
    protected OutputStream outputStream;
    protected InputStream inputStream;
    boolean deviceConnected = false;
    byte buffer[];
    boolean stopThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) //this method comes from the Activity superclass
    {

        super.onCreate(savedInstanceState); //call for the previous saved state
        //Creating layout
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    //This method checks if the device is connected to a network. It does not check if it really has an internet acces (if the connected
    //network has an internet access)
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

   /* @Override
    public void run()
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                hasInternetAccess = (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, "No network available!");
            hasInternetAccess = false;
        }
    }
*/

    @Override
    public void onStart() {
        super.onStart();


        money = (ImageView) findViewById(R.id.currentAmount_BG);

        moveUp = AnimationUtils.loadAnimation(this, R.anim.money_move_up);
        moveUp_text = AnimationUtils.loadAnimation(this, R.anim.money_text_move_up);


        //Displayed buttons
        Button send_tl, send_bes, send_g;

        final EditText sender = (EditText) findViewById(R.id.sender_name);
        sender.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    senderInfo = sender.getText().toString();
                    Log.i(TAG, "sender name: " + senderInfo);

                    return true;
                }
                return false;
            }
        });

        final TextView amountToSend = (TextView) findViewById(R.id.amountDisplay);
        Log.i(TAG, "Operation counter: " + operationCounter);
        amountToSend.setText(base_int_tl + " ₺");

        update(amountToSend);

        //Buttons indicate which currency to be send to the kumbara
        send_tl = (Button) findViewById(R.id.action_tl);
        send_tl.setOnClickListener(new View.OnClickListener() {
                                       public void onClick(View v) {
                                           operationCounter = 0;
                                           counter_tl = 0;
                                           counter_g = 0;
                                           amountToSend.setText(base_int_tl + " ₺");
                                           Log.i(TAG, "Operation counter: " + operationCounter);
                                       }
                                   }
        );
        send_bes = (Button) findViewById(R.id.action_bes);
        send_bes.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            operationCounter = 1;
                                            counter_tl = 0;
                                            counter_g = 0;
                                            amountToSend.setText(base_int_bes + " ₺");
                                            Log.i(TAG, "Operation counter: " + operationCounter);
                                        }
                                    }
        );
        send_g = (Button) findViewById(R.id.action_g);
        send_g.setOnClickListener(new View.OnClickListener() {
                                      public void onClick(View v) {
                                          operationCounter = 2;
                                          counter_tl = 0;
                                          counter_g = 0;
                                          amountToSend.setText(base_int_g + " GR");
                                          Log.i(TAG, "Operation counter: " + operationCounter);
                                      }
                                  }
        );

        if (!deviceConnected) {
            onClickStart();
        }

        final ImageView background_displayedAmount = (ImageView) findViewById(R.id.currentAmount_BG);
        if (background_displayedAmount != null) {
            background_displayedAmount.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
                public void onSwipeTop() //To send
                {
                    if(isNetworkAvailable()) {
                        sendEnabled = true;

                        Log.i(TAG, "Operation counter: " + operationCounter);
                        Log.i(TAG, "Sender name: " + senderInfo);

                        String totalAmountFormatter[] = amountToSend.getText().toString().split(" ");
                        String formattedAmountToSend = totalAmountFormatter[0]; //[0] would indicate the amount, [1] would indicate the type


                        if (currentTotalTLAmount.equals(null) || totalSumBES.equals(null) || totalSumG.equals(null)) {
                            currentTotalTLAmount = "0";
                            totalSumBES = "0";
                            totalSumG = "0";
                        }

                        Log.i(TAG, "Current total before sending: " + currentTotalTLAmount);
                        Log.i(TAG, "Amount to send before sending: " + amountToSend.getText().toString());

                        int currentTotal;
                        double currentTotal_g;

                        switch (operationCounter) {
                            case 0:
                                currentTotal = Integer.parseInt(formattedAmountToSend) + Integer.parseInt(currentTotalTLAmount);
                                currentTotalTLAmount = Integer.toString(currentTotal);
                                break;
                            case 1:
                                currentTotal = Integer.parseInt(formattedAmountToSend) + Integer.parseInt(totalSumBES);
                                totalSumBES = Integer.toString(currentTotal);
                                break;
                            case 2:
                                currentTotal_g = Double.parseDouble(formattedAmountToSend) + Double.parseDouble(totalSumG);
                                totalSumG = Double.toString(currentTotal_g);
                                break;
                        }

                        restInt.setFields(totalAmountFormatter[1], formattedAmountToSend, currentTotalTLAmount, totalSumBES, totalSumG, senderInfo, new Callback<Integer>() {
                            @Override
                            public void success(Integer getJSON, Response response) {
                                sendEnabled = true;
                                Toast.makeText(getApplicationContext(), "Para hesaba gönderildi", Toast.LENGTH_SHORT).show();

                                update(amountToSend);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                String err = error.getMessage();
                                Log.e(TAG, "Thingspeak send request failed. " + err);
                            }
                        });

                        Log.i(TAG, "updated stats: " + lastCallType + " " + lastCallAmountInfo + " " + lastCallTimeInfo);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "İnternet bağlantısı yok!", Toast.LENGTH_LONG).show();

                }

                public void onSwipeRight() //To increase the amount to be sent
                {
                    String totalAmountFormatter[] = amountToSend.getText().toString().split(" "); //to get the symbol

                    if (!amountToSend.getText().toString().equals(max_int_tl + " " + totalAmountFormatter[1]) && !amountToSend.getText().toString().equals(max_int_g + " GR")) {
                        counter_tl++;
                        counter_g++;
                        amountToSend.setText(changeAmountToBeSent(amountToSend.getText().toString(), counter_tl, counter_g, 1) + " " + totalAmountFormatter[1]);
                        Log.i(TAG, "Add button pressed");
                    } else {
                        counter_tl = amountOrder_tl.length - 1;
                        counter_g = amountOrder_gold.length - 1;
                    }
                }

                public void onSwipeLeft() //To increase the amount to be sent
                {
                    String totalAmountFormatter[] = amountToSend.getText().toString().split(" "); //to get the symbol

                    if (!amountToSend.getText().toString().equals(base_int_tl + " ₺") && !amountToSend.getText().toString().equals(base_int_g + " GR")) {
                        counter_tl--;
                        counter_g--;
                        amountToSend.setText(changeAmountToBeSent(amountToSend.getText().toString(), counter_tl, counter_g, 1) + " " + totalAmountFormatter[1]);
                        Log.i(TAG, "Subtract button pressed");
                    } else {
                        counter_tl = 0;
                        counter_g = 0;
                    }
                }
            });
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    @Override
    public void onPause() {
        super.onPause();
        if (deviceConnected)
            onClickStop(); //Stops the bluetooth connection
    }
    @Override
    public void onStop() {
        super.onStop();
        if (deviceConnected)
            onClickStop(); //Stops the bluetooth connection

    }


    //Changes the amount to be sent. It takes the current state of the amount and the desired addition amount as arguments and returns the changed amount as a String.
    private String changeAmountToBeSent(String _currentAmount, int counter_tl, int counter_g, int operation) {
        //Current amount that is displayed
        String amountFormatter[] = _currentAmount.split(" ");

        int currentAmount;
        double currentAmount_g; //Since Gould product types operates over doubles, it had to be distinguished

        //Switches between products. Each products addition/subtraction method is different
        //0: TL, 1: BES, 2: Gold
        switch (operationCounter) {
            case 0:
                currentAmount = Integer.parseInt(amountFormatter[0]);
                if (currentAmount >= base_int_tl && currentAmount <= max_int_tl) {
                    currentAmount = amountOrder_tl[counter_tl];
                    Log.i(TAG, "currentAmount after operation: " + Double.toString(currentAmount));
                }
                return Integer.toString(currentAmount);
            case 1:
                currentAmount = Integer.parseInt(amountFormatter[0]);
                if (currentAmount > base_int_bes) {

                    currentAmount += (bes_increaseAmount * operation);
                    Log.i(TAG, "currentAmount after operation: " + Double.toString(currentAmount));
                } else if (currentAmount == base_int_bes && operation != -1) {
                    currentAmount += (bes_increaseAmount * operation);
                    Log.i(TAG, "currentAmount after operation: " + Double.toString(currentAmount));
                }
                return Integer.toString(currentAmount);
            case 2:
                currentAmount_g = Double.parseDouble(amountFormatter[0]);
                if (currentAmount_g >= base_int_g && currentAmount_g <= max_int_g) {
                    currentAmount_g = amountOrder_gold[counter_g];
                    Log.i(TAG, "currentAmount after operation: " + Double.toString(currentAmount_g));
                }
                return Double.toString(currentAmount_g);
        }

        return " ";
    }

    public String formatDate(String date[]) {
        String month, day, year;
        year = date[0];
        month = date[1];
        day = date[2];

        return day + "/" + month + "/" + year;
    }

    public String formatTime(String time[]) {
        String hour, minute;
        int time_GMT3 = Integer.parseInt(time[0]) + 3;
        hour = Integer.toString(time_GMT3);
        minute = time[1];

        return hour + ":" + minute;
    }

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

    //Updates the Thinkspeak channel
    public void update(final TextView amountToSend) {


        final MInterface restInt = radapter.create(MInterface.class);
        final TextView _amountToSend = amountToSend;
        //Calls for the server and gets the info about appropriate sections
        restInt.getFeed(new Callback<Model>() {
            @Override
            public void success(Model model, Response response) {
                List<Feed> feeds = model.getFeeds();
                Feed feed = feeds.get(0);

                lastCallType = feed.getField1();
                Log.i(TAG, "Last call type: " + lastCallType);

                lastCallAmountInfo = feed.getField2();
                Log.i(TAG, "Last call amount info: " + lastCallAmountInfo);

                currentTotalTLAmount = feed.getField3();
                Log.i(TAG, "Current total amount: " + currentTotalTLAmount);

                totalSumBES = feed.getField4();
                Log.i(TAG, "Total BES sum: " + totalSumBES);

                totalSumG = feed.getField5();
                Log.i(TAG, "Total Gold sum: " + totalSumG);

                lastSenderInfo = (String) feed.getField6();
                Log.i(TAG, "Total sender name: " + lastSenderInfo);

                //Formatting last call time
                String lastCallTime = feed.getCreatedAt();
                String date_timeComponents[] = lastCallTime.split("T");
                String date, dateComponents[], time, timeComponents[];
                //Formatting date
                date = date_timeComponents[0];
                dateComponents = date.split("-");
                String dateToDisplay = formatDate(dateComponents);
                //Formatting time
                time = date_timeComponents[1];
                timeComponents = time.split(":");
                String timeToDisplay = formatTime(timeComponents);
                //
                lastCallTimeInfo = dateToDisplay + " " + timeToDisplay;
                Log.i(TAG, "Last call time: " + lastCallTimeInfo);
                Log.i(TAG, "Updated");

                if (sendEnabled) {
                    money.startAnimation(moveUp);
                    amountToSend.startAnimation(moveUp_text);

                    if (deviceConnected) {
                        onClickSend();
                        Toast.makeText(getApplicationContext(), "Para kumbaraya gönderildi", Toast.LENGTH_SHORT).show();
                        Log.i(TAGB, "info sending sueccessful");

                    }
                    switch (operationCounter) {
                        case 0:
                            counter_tl = 0;
                            Log.i(TAG, "Send amount: " + _amountToSend.getText().toString());
                            _amountToSend.setText(base_int_tl + " ₺");
                            sendEnabled = false;
                            break;

                        case 1:
                            Log.i(TAG, "Send amount: " + _amountToSend.getText().toString());
                            _amountToSend.setText(base_int_bes + " ₺");
                            sendEnabled = false;
                            break;

                        case 2:
                            counter_g = 0;
                            Log.i(TAG, "Send amount: " + _amountToSend.getText().toString());
                            _amountToSend.setText(base_int_g + " GR");
                            sendEnabled = false;
                            break;
                    }

                }


                Log.i(TAG, "updated stats enhanced: " + lastCallType + " " + lastCallAmountInfo + " " + lastCallTimeInfo);

            }

            @Override
            public void failure(RetrofitError error) {
                String cause = error.getCause().toString();
                String err = error.getMessage();
                Log.e(TAG, "Thingspeak get request failed. " + cause);
            }
        });
    }

    //---Bluetooth related methods---
    //Bluetooth initiation method
    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getName().equals(DEVICE_NAME)) {
                    Log.i(TAGB, "matching paired devices");
                    device = iterator;
                    Log.i(TAGB, "device: " + device.getName());
                    found = true;
                    return found;
                }
            }
        }
        Toast.makeText(getApplicationContext(), "Cihaz bulunamadı", Toast.LENGTH_LONG).show();
        return found;
    }

    //Bluetooth connection establishment
    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {

            e.printStackTrace();
            connected = false;
        }
        if (connected) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }

    public void onClickStart() {
        if (BTinit()) {
            if (BTconnect()) {
                deviceConnected = true;
                beginListenForData();
            }
        }
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            handler.post(new Runnable() {
                                public void run() {

                                }
                            });
                        }
                    } catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public void onClickSend() {
        String string = operationCounter + " " + lastCallType + " " + lastCallAmountInfo + " " + currentTotalTLAmount + " " +
                totalSumBES + " " + totalSumG + " " + senderInfo + " ";

        Log.i(TAGB, "info sent: " + string);
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickStop() {
        try {
            stopThread = true;
            outputStream.close();
            inputStream.close();
            socket.close();
            deviceConnected = false;
            Log.i(TAGB, "connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}

