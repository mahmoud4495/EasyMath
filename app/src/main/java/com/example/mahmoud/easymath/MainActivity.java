package com.example.mahmoud.easymath;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private View view;
    private TextView expressionTextView;
    private TextView solutionTextView;
    private LinearLayout resultLinearLayout;
    private PaintView paintView;
    SeekBar seekBar;
    TensorFlowInferenceInterface tensorFlowInferenceInterface;
    static final String MODEL_NAME = "file:///android_asset/frozen_tfdroid.pb";

    static {
        System.loadLibrary("tensorflow_inference");
        if (OpenCVLoader.initDebug()) {
            Log.e("MainActivity", "OpenCV is Loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tensorFlowInferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_NAME);

        expressionTextView = (TextView) findViewById(R.id.expressionTextView);
        solutionTextView = (TextView) findViewById(R.id.solutionTextView);
        resultLinearLayout = (LinearLayout) findViewById(R.id.resultLinearLayout);
        view = findViewById(R.id.activity_main);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        paintView = (PaintView) findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_stroke) {
            seekBar.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    seekBar.setVisibility(View.GONE);
                }
            }, 5000);
        } else if(item.getItemId() == R.id.menu_undo) {
            if (!paintView.undo()) {
                Toast.makeText(this , "Screen is empty", Toast.LENGTH_SHORT).show();
            }
        } else if(item.getItemId() == R.id.menu_done) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                seekBar.setVisibility(View.GONE);
                resultLinearLayout.setVisibility(View.GONE);
                Bitmap bitmap = Screenshot.takeScreenshot(view);
                ArrayList<Mat> croppedSymbols = ImagePreprocessor.getCroppedSymbols(bitmap, false);
                String equation = Model.getInference(croppedSymbols, tensorFlowInferenceInterface);
                ArrayList<String> solutions = Solver.solve(equation);
                String result = "";
                if (solutions.size() != 0) {
                    for (int i = 0 ; i < solutions.size() ; i++) {
                        if (i == (solutions.size() - 1)) {
                            result = result + solutions.get(i);
                        } else {
                            result = result + solutions.get(i) + ", ";
                        }
                    }
                } else {
                    result = "Invalid Input";
                }

                int index = equation.indexOf("X2");
                if (index >= 0) {
                    equation = equation.substring(0, index) + "X^2" + equation.substring(index + 2);
                }
                index = equation.indexOf("X3");
                if (index >= 0) {
                    equation = equation.substring(0, index) + "X^3" + equation.substring(index + 2);
                }

                expressionTextView.setText(equation);
                solutionTextView.setText(result);
                resultLinearLayout.setVisibility(View.VISIBLE);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
                }
            }
        } else if (item.getItemId() == R.id.menu_clear) {
            resultLinearLayout.setVisibility(View.GONE);
            paintView.clear();
        }
        return super.onOptionsItemSelected(item);
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            paintView.stroke(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

}
