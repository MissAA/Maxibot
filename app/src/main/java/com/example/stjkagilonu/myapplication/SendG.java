package com.example.stjkagilonu.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SendG extends MainActivity {

    private static final String TAG = "Kumbara-G";
    private static final String TAGB = "Bluetooth";

    String currentTotalGoldAmount, lastCallTimeInfo, lastCallAmountInfo, senderInfo;

    int counter;
    double base_int = 0.5;
    double max_int = 100;
    private double amountOrder[] = {base_int, 1, 2.5, 5, 10, 20, 50, max_int};

    boolean deviceConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_g);
        Intent intent = getIntent();//Creating layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Shows the amount to be send to the kumbara
        final TextView amountToSend = (TextView)findViewById(R.id.amountDisplay);
        amountToSend.setText(base_int + " GR");

        final ImageButton send,add,subtract;
        Log.i(TAG, "Current total Gold amount: " + currentTotalGoldAmount);
        counter = 0;
        update();

        //Add button increases the amount to be send by one per click.
        add = (ImageButton)findViewById(R.id.addition);
        add.setOnClickListener(new View.OnClickListener()
                               {
                                   public void onClick(View v)
                                   {
                                       if(!amountToSend.getText().toString().equals(max_int + " GR"))
                                       {
                                           counter++;

                                           amountToSend.setText(changeAmountToBeSent(amountToSend.getText().toString(), counter) + " GR");
                                           Log.i(TAG, "Add button pressed");
                                       }
                                       else
                                           counter = amountOrder.length-1;
                                   }
                               }
        );

        //Decreases the amount to be sent by one per click.
        subtract = (ImageButton)findViewById(R.id.subtraction);
        subtract.setOnClickListener(new View.OnClickListener()
                                    {
                                        public void onClick(View v)
                                        {
                                            if(!amountToSend.getText().toString().equals(base_int + " GR"))
                                            {
                                                counter--;
                                                amountToSend.setText(changeAmountToBeSent(amountToSend.getText().toString(), counter) + " GR");
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

                Log.i(TAG, "Current total before sending: " + currentTotalGoldAmount);
                Log.i(TAG, "Amount to send before sending: " + amountToSend.getText().toString());

                double currentTotal = Double.parseDouble(formattedAmountToSend) + Double.parseDouble(currentTotalGoldAmount);
                currentTotalGoldAmount = Double.toString(currentTotal);

                Log.i(TAG, "Current total after operation, before sending: " + currentTotalGoldAmount);

                restInt.setFields("GR", formattedAmountToSend, currentTotalTLAmount , totalSumBES, currentTotalGoldAmount, senderInfo, new Callback<Integer>() {
                    @Override
                    public void success(Integer getJSON, Response response)
                    {

                    }

                    @Override
                    public void failure(RetrofitError error)
                    {
                        String cause = error.getCause().toString();
                        String err = error.getMessage();
                        Log.e(TAG, "Thingspeak send request failed. " + cause);
                    }
                });
                AlertDialog.Builder transaction_success = new AlertDialog.Builder(SendG.this);
                transaction_success.setMessage("Money is sent!");

                update();
                Log.i(TAG, "Send amount: " + amountToSend.getText().toString());
                amountToSend.setText(base_int + " ₺");
                if(!deviceConnected) {
                    onClickStart();
                }
                onClickSend();

                counter = 0;

            }
        });

    }

    private String changeAmountToBeSent(String _currentAmount, int counter)
    {
        Log.i(TAG, "counter before operation: " + counter);
        //Current amount that is displayed
        String amountFormatter[] = _currentAmount.split(" ");
        double currentAmount = Double.parseDouble(amountFormatter[0]);
        Log.i(TAG, "currentAmount: " + currentAmount);

        if(currentAmount >= base_int && currentAmount <= max_int)
        {
            currentAmount = amountOrder[counter];
            Log.i(TAG, "currentAmount after operation: " + Double.toString(currentAmount));
        }

        //If the amount does not fit the boundaries, an error message is shown.
        else
        {
            AlertDialog.Builder errorMessage_invalidAmount = new AlertDialog.Builder(SendG.this);
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
            return Double.toString(currentAmount);
        }
        Log.i(TAG, "counter after operation: " + counter);
        return Double.toString(currentAmount);
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
                Log.i(TAG, "Total TL sum: " + currentTotalTLAmount);

                lastCallAmountInfo = feed.getField2();
                Log.i(TAG, "Last call amount info: " + lastCallAmountInfo);

                totalSumBES = feed.getField4();
                Log.i(TAG, "Total BES sum: " + totalSumBES);

                currentTotalGoldAmount = feed.getField5();
                Log.i(TAG, "Total Gold sum: " + currentTotalGoldAmount);

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
                totalSumBES + " " + currentTotalGoldAmount + " " + senderInfo + " ";
        string.concat("\n");
        try {
            outputStream.write(string.getBytes());
            Log.i(TAGB, "info sent: " + string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



