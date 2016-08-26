package com.example.stjkagilonu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "Kumbara-TL";
    private static final String TAGB = "Bluetooth";
    //Server that stores the information
    public static final String url = "https://api.thingspeak.com";

    //Information regarding to Kumbara are stored in the following fields. The info will be taken from the server
    String currentTotalTLAmount, lastCallTimeInfo, lastCallAmountInfo, senderInfo, totalSumBES, totalSumG, lastCallType;

    //Necessary elements for increasing/decreasing the amount. Addition/subtraction operations cycle through the array
    int counter = 0;
    int base_int = 20; //Starting amount of the send-TL operation
    int max_int = 200; //Maximum amount that can be send via TL transaction
    private int amountOrder[] = {20, 50, 100, 200};

    //Bluetooth connection elements
    protected final String DEVICE_NAME="maxibot";
    protected final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    protected BluetoothDevice device;
    protected BluetoothSocket socket;
    protected OutputStream outputStream;
    protected InputStream inputStream;
    boolean deviceConnected=false;
    byte buffer[];
    boolean stopThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) //this method comes from the Activity superclass
    {

        super.onCreate(savedInstanceState); //call for the previous saved state
        Log.i(TAG, "The main activity is created.");

        //Creating layout
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Displayed buttons
        final ImageButton send,add,subtract;
        Button send_tl, send_bes, send_g;

        //Shows the amount to be send to the kumbara
        final TextView amountToSend = (TextView)findViewById(R.id.amountDisplay);
        amountToSend.setText(base_int + " ₺");
        //Holds the sender information and shows it on screen
        final EditText senderName = (EditText)findViewById(R.id.sender_name);
        senderInfo = senderName.getText().toString();
        //Updates the current status of the variables that depend on the information taken from the server
        update();

        //Buttons indicate which currency to be send to the kumbara
        send_tl = (Button)findViewById(R.id.action_tl);
        send_tl.setOnClickListener(new View.OnClickListener()
                                   {
                                       public void onClick(View v)
                                       {
                                           mainActivity(v);
                                       }
                                   }
        );
        send_bes = (Button)findViewById(R.id.action_bes);
        send_bes.setOnClickListener(new View.OnClickListener()
                                    {
                                        public void onClick(View v)
                                        {
                                            besActivity(v);
                                        }
                                    }
        );
        send_g = (Button)findViewById(R.id.action_g);
        send_g.setOnClickListener(new View.OnClickListener()
                                  {
                                      public void onClick(View v)
                                      {
                                         goldActivity(v);
                                      }
                                  }
        );

        Log.i(TAG, "Amount to send: " + amountToSend.getText().toString());

        //Add button increases the amount to be send by one per click.
        add = (ImageButton)findViewById(R.id.addition);
        add.setOnClickListener(new View.OnClickListener()
                               {
                                   public void onClick(View v)
                                   {
                                       Log.i(TAG, "Counter (beggining of add): " + counter);
                                       if(!amountToSend.getText().toString().equals(max_int + " ₺"))
                                       {
                                           counter++;

                                           amountToSend.setText(changeAmountToBeSent(amountToSend.getText().toString(), counter) + " ₺");
                                           Log.i(TAG, "Add button pressed");
                                       }
                                       else
                                           counter = 3;
                                   }
                               }
        );

        //Decreases the amount to be sent by one per click.
        subtract = (ImageButton)findViewById(R.id.subtraction);
        subtract.setOnClickListener(new View.OnClickListener()
                                    {
                                        public void onClick(View v)
                                        {
                                            if(!amountToSend.getText().toString().equals(base_int + " ₺"))
                                            {
                                                counter--;
                                                amountToSend.setText(changeAmountToBeSent(amountToSend.getText().toString(), counter) + " ₺");
                                                Log.i(TAG, "Subtract button pressed");
                                                Log.i(TAG, "Counter (end of sub): " + counter);
                                            }
                                            else
                                                counter = 0;
                                        }
                                    }
        );

        RestAdapter radapter=new RestAdapter.Builder().setEndpoint(url).build();
        final MInterface restInt = radapter.create(MInterface.class);

        //Send button: when pressed sends the specified amount to the specified Kumbara
        send = (ImageButton)findViewById(R.id.send);
        send.setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View v)
            {

                String totalAmountFormatter[] = amountToSend.getText().toString().split(" ");
                String formattedAmountToSend = totalAmountFormatter[0];

                Log.i(TAG, "Current total before sending: " + currentTotalTLAmount);
                Log.i(TAG, "Amount to send before sending: " + amountToSend.getText().toString());

                int currentTotal = Integer.parseInt(formattedAmountToSend) + Integer.parseInt(currentTotalTLAmount);
                currentTotalTLAmount = Integer.toString(currentTotal);

                restInt.setFields("TL", formattedAmountToSend, currentTotalTLAmount , totalSumBES, totalSumG, senderInfo, new Callback<Integer>() {
                    @Override
                    public void success(Integer getJSON, Response response)
                    {
                        update();
                        Log.i(TAG, "updated stats: " + lastCallType + " " + lastCallAmountInfo  + " " + lastCallTimeInfo);

                        if(!deviceConnected) {
                            onClickStart();
                        }
                        onClickSend();
                        Log.i(TAGB, "info sending sueccessful");
                        counter = 0;
                    }
                    @Override
                    public void failure(RetrofitError error)
                    {
                        String cause = error.getCause().toString();
                        String err = error.getMessage();
                        Log.e(TAG, "Thingspeak send request failed. " + cause);
                    }
                });



                Log.i(TAG, "Send amount: " + amountToSend.getText().toString());
                amountToSend.setText(base_int + " ₺");

                Log.i(TAG, "updated stats enhanced: " + lastCallType + " " + lastCallAmountInfo  + " " + lastCallTimeInfo);
                AlertDialog.Builder transaction_success = new AlertDialog.Builder(MainActivity.this);
                transaction_success.setMessage("Money is sent!");

            }
        });

    }

    @Override
    public void onPause()
    {
        if(deviceConnected)
            onClickStop(); //Stops the bluetooth connection
        super.onPause();
    }

    //--Button methods to change activity--
    //Starts the MainActivity, sending currency change to TL
    public void mainActivity(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        if(deviceConnected)
            onClickStop();
        startActivity(intent);

    }
    //Starts the SendBES activity, sending currency change to BES
    public void besActivity(View view)
    {
        Intent intent = new Intent(this, SendBES.class);
        if(deviceConnected)
            onClickStop();
        startActivity(intent);

    }
    //Starts the SendG activity, sending currency change to Gold
    public void goldActivity(View view)
    {
        Intent intent = new Intent(this, SendG.class);
        if(deviceConnected)
         onClickStop();
        startActivity(intent);

    }
    //-----

    //Changes the amount to be sent. It takes the current state of the amount and the desired addition amount as arguments and returns the changed amount as a String.
    private String changeAmountToBeSent(String _currentAmount, int counter)
    {
        Log.i(TAG, "counter before operation: " + counter);
        //Current amount that is displayed
        String amountFormatter[] = _currentAmount.split(" ");
        int currentAmount = Integer.parseInt(amountFormatter[0]);
        Log.i(TAG, "currentAmount: " + currentAmount);
        if(currentAmount >= base_int && currentAmount <= max_int)
        {
            currentAmount = amountOrder[counter];
            Log.i(TAG, "currentAmount after operation: " + Integer.toString(currentAmount));
        }

        //If the amount does not fit the boundaries, an error message is shown.
        else
        {
            AlertDialog.Builder errorMessage_invalidAmount = new AlertDialog.Builder(MainActivity.this);
            errorMessage_invalidAmount.setMessage("Invalid amount");
            errorMessage_invalidAmount.setCancelable(false);
            errorMessage_invalidAmount.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }
            );
            return Integer.toString(currentAmount);
        }
        Log.i(TAG, "counter after operation: " + counter);
        return Integer.toString(currentAmount);
    }

    public String formatDate(String date[])
    {
        String month, day, year;
        year = date[0];
        month = date[1];
        day = date[2];

        return day + "/" + month + "/" + year;
    }

    public String formatTime(String time[])
    {
        String hour, minute;
        int time_GMT3 = Integer.parseInt(time[0]) + 3;
        hour = Integer.toString(time_GMT3);
        minute = time[1];

        return hour + ":" + minute;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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
    public void update()
    {
        RestAdapter radapter=new RestAdapter.Builder().setEndpoint(url).build();
        final MInterface restInt = radapter.create(MInterface.class);
        //Calls for the server and gets the info about appropriate sections
        restInt.getUser(new Callback<Model>()
        {
            @Override
            public void success(Model model, Response response)
            {
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

                senderInfo = (String) feed.getField6();
                Log.i(TAG, "Total sender name: " + senderInfo);

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
            }
            @Override
            public void failure(RetrofitError error) {
                String cause = error.getCause().toString();
                String err = error.getMessage();
                Log.e(TAG, "Thingspeak get request failed. " + cause);
            }
        });
    }



    //Bluetooth initiation method
    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getName().equals(DEVICE_NAME))
                {
                    Log.i(TAGB, "matching paired devices");
                    device=iterator;
                    Log.i(TAGB, "device: " + device.getName());
                    found=true;
                    return found;
                }
            }
        }
        return found;
    }

    //Bluetooth connection establishment
    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {

            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }

    public void onClickStart() {
        if(BTinit())
        {
            if(BTconnect())
            {
                deviceConnected=true;
                beginListenForData();
            }
        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {

                                }
                            });
                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }
    public void onClickSend() {
        String string = lastCallType + " " + lastCallAmountInfo + " " + currentTotalTLAmount + " " +
                        totalSumBES + " " + totalSumG + " " + senderInfo + " ";

        string.concat("\n");
        Log.i(TAGB, "info sent: " + string);
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickStop(){
        try {
            stopThread = true;
            outputStream.close();
            inputStream.close();
            socket.close();
            deviceConnected=false;
            Log.i(TAGB, "connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

