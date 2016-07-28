package com.wso2.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    EditText editTextInput;
    Button buttonSayHello;
    TextView textViewHello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calculator calculator = new Calculator();

        editTextInput = (EditText) findViewById(R.id.editTextInput);
        buttonSayHello = (Button) findViewById(R.id.buttonSayHello);
        textViewHello = (TextView) findViewById(R.id.textViewHello);

        buttonSayHello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editTextInput.getText().toString();
                textViewHello.setText("Hi " + input + "!");
            }
        });

    }
}
