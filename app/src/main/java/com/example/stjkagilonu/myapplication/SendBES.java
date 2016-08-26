package com.example.stjkagilonu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SendBES extends MainActivity {
    private static final String TAG = "Kumbara-BES";
    private static final String TAGB = "Bluetooth";
    String REQUEST_ENABLE_BT;

    String currentTotalBESAmount, lastCallTimeInfo, lastCallAmountInfo, senderInfo;

    int base_int = 150; //Starting amount of the send-BES operation
    int operationAmount = 50; //Operations on the amount to be send will be made accordingly

    boolean deviceConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_bes);
        Intent intent = getIntent();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        //Shows the amount to be send to the kumbara
        final TextView amountToSend = (TextView)findViewById(R.id.amountDisplay);
        amountToSend.setText(base_int + " ₺");

        final ImageButton send,add,subtract;
        Log.i(TAG, "Current total BES amount: " + currentTotalBESAmount);
        update();



        //Add button increases the amount to be send by one per click.
        add = (ImageButton)findViewById(R.id.addition);
        add.setOnClickListener(new View.OnClickListener()
                               {
                                   public void onClick(View v)
                                   {
                                       amountToSend.setText(changeAmountToBeSent(amountToSend.getText().toString(), 1) + " ₺");
                                   }
                               }
        );

        //Decreases the amount to be sent by one per click.
        subtract = (ImageButton)findViewById(R.id.subtraction);
        subtract.setOnClickListener(new View.OnClickListener()
                                    {
                                        public void onClick(View v) {

                                            amountToSend.setText(changeAmountToBeSent(amountToSend.getText().toString(), -1) + " ₺");
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

                Log.i(TAG, "Current total before sending: " + currentTotalBESAmount);
                Log.i(TAG, "Amount to send before sending: " + amountToSend.getText().toString());

                int currentTotal = Integer.parseInt(formattedAmountToSend) + Integer.parseInt(currentTotalBESAmount);
                currentTotalBESAmount = Integer.toString(currentTotal);

                Log.i(TAG, "Current total after operation, before sending: " + currentTotalBESAmount);

                restInt.setFields("BES", formattedAmountToSend, currentTotalTLAmount , currentTotalBESAmount, totalSumG, senderInfo, new Callback<Integer>() {
                    @Override
                    public void success(Integer getJSON, Response response)
                    {
                        update();
                        Log.i(TAG, "Send amount: " + amountToSend.getText().toString());
                        amountToSend.setText(base_int + " ₺");
                        if(!deviceConnected) {
                            onClickStart();
                        }
                        onClickSend();
                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        String cause = error.getCause().toString();
                        String err = error.getMessage();
                        Log.e(TAG, "Thingspeak send request failed. " + cause);
                    }
                });
                AlertDialog.Builder transaction_success = new AlertDialog.Builder(SendBES.this);
                transaction_success.setMessage("Money is sent!");

            }
        });

    }

    private String changeAmountToBeSent(String _currentAmount, int operation)
    {
        //Current amount that is displayed
        String amountFormatter[] = _currentAmount.split(" ");
        int currentAmount = Integer.parseInt(amountFormatter[0]);
        Log.i(TAG, "currentAmount: " + currentAmount);

        if(currentAmount > base_int)
        {

            currentAmount += (operationAmount * operation);
            Log.i(TAG, "currentAmount after operation: " + Integer.toString(currentAmount));
        }
        else if(currentAmount == base_int && operation !=-1)
        {
            currentAmount += (operationAmount * operation);
            Log.i(TAG, "currentAmount after operation: " + Integer.toString(currentAmount));
        }

        //If the amount does not fit the boundaries, an error message is shown.
        else
        {
            AlertDialog.Builder errorMessage_invalidAmount = new AlertDialog.Builder(SendBES.this);
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
        return Integer.toString(currentAmount);
    }

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
                //currentTotalAmount.setText(feed.getField3() + " TL");
                Log.i(TAG, "Total TL sum: " + currentTotalTLAmount);

                lastCallAmountInfo = feed.getField2();
                //lastCallAmountInfo.setText(feed.getField2() + " TL");
                Log.i(TAG, "Last call amount info: " + lastCallAmountInfo);

                currentTotalBESAmount = feed.getField4();
                Log.i(TAG, "Total BES sum: " + currentTotalBESAmount);

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
                //lastCallTimeInfo.setText(dateToDisplay + "  " + timeToDisplay);
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

    public void onClickSend() {
        String string = lastCallType + " " + lastCallAmountInfo + " " + currentTotalTLAmount + " " +
                currentTotalBESAmount + " " + totalSumG + " " + senderInfo + " ";
        string.concat("\n");
        Log.i(TAGB, "info sent");
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



