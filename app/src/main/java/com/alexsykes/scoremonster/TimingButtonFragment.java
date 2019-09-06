package com.alexsykes.scoremonster;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TimingButtonFragment extends Fragment implements View.OnClickListener {
    TextView riderNumber;

    public TimingButtonFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timing_button, container, false);
    }

    public void setRiderNumber(String theNumber) {
        riderNumber.setText(theNumber);
    }

    public TextView getRiderNumber() {
        return riderNumber;
    }

    @Override
    public void onClick(View v) {

    }
}
