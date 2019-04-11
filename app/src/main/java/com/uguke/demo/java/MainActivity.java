package com.uguke.demo.java;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.uguke.java.util.ValueUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ValueUtils.print(new int[] {0, 1});
    }
}
