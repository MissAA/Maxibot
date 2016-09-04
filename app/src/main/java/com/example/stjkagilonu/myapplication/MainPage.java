package com.example.stjkagilonu.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_layout);

        Button maxibot_action = (Button)findViewById(R.id.maxibot_action);
        maxibot_action.setOnClickListener(new View.OnClickListener() {
                                              public void onClick(View v) {
                                                  maxibotActivity();
                                              }
                                          }
        );
    }

    public void maxibotActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
