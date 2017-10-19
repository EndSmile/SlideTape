package com.ldy.slidetape;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ldy.slidetape.slidetape.SlideTapeGroup;
import com.ldy.slidetape.slidetape.TapeTextCalculator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SlideTapeGroup slideTapeGroup = (SlideTapeGroup) findViewById(R.id.slideTap);
        slideTapeGroup.setMaxProgress(130);
        slideTapeGroup.setTapeTextCalculator(new TapeTextCalculator() {
            @NonNull
            @Override
            public String calculateLongLineText(int progress) {
                return String.valueOf(progress + 100);
            }

            @NonNull
            @Override
            public String calculateProgressText(int progress) {
                return (progress + 100) + ".0厘米";
            }
        });
    }
}
