package com.alexsykes.scoremonster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LayoutActivity extends AppCompatActivity {

    ScorePadFragment scorePadFragment;
    TouchFragment numberPadFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);


        scorePadFragment = new ScorePadFragment();
        numberPadFragment = new TouchFragment();


        getSupportFragmentManager().beginTransaction().add(R.id.top, numberPadFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.bottom, scorePadFragment).commit();
    }
}
