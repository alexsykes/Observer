package com.alexsykes.observer.data;

import android.provider.BaseColumns;
import androidx.appcompat.app.AppCompatActivity;

public class FinishTimeContract extends AppCompatActivity {
    public FinishTimeContract(){}

    public static final class FinishTimeEntry implements BaseColumns {

        public static final String TABLE_NAME = "finishTimes";

        public static final String _ID = BaseColumns._ID;
        public final static String COLUMN_FINISHTIME_RIDER = "rider";
        public final static String COLUMN_FINISHTIME_FINISHTIME = "finishtime";
        public final static String COLUMN_FINISHTIME_STARTTIME = "starttime";
        public final static String COLUMN_FINISHTIME_ELAPSED_TIME = "elapsedtime";
        public final static String COLUMN_FINISHTIME_SYNC = "sync";
        public final static String COLUMN_FINISHTIME_HUMANREADABLE = "finishtime_humanreadable";
        public final static String COLUMN_FINISHTIME_TRIALID = "trialid";
    }
}