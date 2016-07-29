package com.wso2.catsage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SecondActivity extends AppCompatActivity {

    TextView textViewSecondCatAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        textViewSecondCatAge = (TextView) findViewById(R.id.textViewSecondCatAge);

        int catAge = getIntent().getExtras().getInt("catAge");

        textViewSecondCatAge.setText(String.valueOf(catAge));

    }
}
