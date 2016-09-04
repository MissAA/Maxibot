package com.example.stjkagilonu.myapplication;


import android.telecom.Call;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by stj.kagilonu on 18.08.2016.
 */
public interface MInterface
{
    @GET("/channels/124871/feeds.json?results=1")
    void getFeed(Callback<Model> user);

    //&field2={moneyToSend}&field3={totalMoneyTL}&field4={totalMoneyBES}&field5={totalMoneyG}
    @GET("/update?api_key=J38US1KWBYLK66ZR&field1=TL")
    void setFields(@Query("field1") String lastSentType, @Query("field2") String lastSentAmount, @Query( "field3") String totalMoneyTL, @Query("field4") String totalMoneyBES, @Query("field5") String totalMoneyG, @Query("field6") String senderInfo, @Query("field7") String lastCallTotal, Callback<Integer> user);

}
