package com.wso2.catsage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    EditText editTextAge;
    Button buttonCalculateAge;
    TextView textViewCatsAge;
    Button buttonSecondActivity;

    int catAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textViewCatsAge = (TextView) findViewById(R.id.textViewCatsAge);
        buttonCalculateAge = (Button) findViewById(R.id.buttonCalculateAge);
        editTextAge = (EditText) findViewById(R.id.editTextAge);
        buttonSecondActivity =(Button) findViewById(R.id.buttonSecond);

        buttonCalculateAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int age = Integer.valueOf(editTextAge.getText().toString());
                catAge = age * 7;
                textViewCatsAge.setText("Cat's Age : " + catAge);
            }
        });

        buttonSecondActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SecondActivity.class);
                intent.putExtra("catAge", catAge);
                startActivity(intent);
            }
        });


    }
}
