package com.alexsykes.scoremonster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimerActivity extends AppCompatActivity {

    NumberPadFragment numberPadFragment;
    TextView numberLabel, timeLabel;
    String riderNumber;
    Button finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        numberPadFragment = new NumberPadFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.top, numberPadFragment).commit();
        numberLabel = findViewById(R.id.numberLabel);
        timeLabel = findViewById(R.id.timeLabel);
        finishButton = findViewById(R.id.finishButton);

        finishButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveFinishTime();
                return false;
            }
        });

    }

    private void saveFinishTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String finishTime  = dateFormat.format(new Date());
        timeLabel.setText(finishTime);


        numberLabel.setText("");
    }

    @Override
    protected void onStart() {

        super.onStart();
    }


    public void addDigit(View view) {
        // Get length of rider riderNumber
        numberLabel = findViewById(R.id.numberLabel);
        riderNumber = numberLabel.getText().toString();
        int len = riderNumber.length();

        // Get id from clicked button to get clicked digit
        int intID = view.getId();
        Button button = view.findViewById(intID);
        String digit = button.getText().toString();

        // Compare with backspace
        if (digit.equals("⌫")) {
            if (len > 0) {
                riderNumber = riderNumber.substring(0, len - 1);
            }
        } else if (digit.equals("C")) {
            riderNumber = "";
        } else {
            riderNumber = riderNumber + digit;
            if (len > 2)
                riderNumber = riderNumber.substring(1, 4);
        }

        if (riderNumber.equals("0")) {
            riderNumber = "";
        }
        numberLabel.setText(riderNumber);
    }
}
