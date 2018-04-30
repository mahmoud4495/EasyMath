package com.example.mahmoud.easymath;

import org.opencv.core.Mat;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;

public final class Model {

    private static final String[] classes = {"-", "(" ,")" , "+", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "div", "E", "G", "L", "N", "O", "S", "sqrt", "X", "Y"};
    private static final String INPUT_NAME = "I";
    private static final String OUTPUT_NAME = "predictions";

    public static String getInference (ArrayList<Mat> mats, TensorFlowInferenceInterface tensorFlowInferenceInterface) {

        String expression = "";

        for (int k = 0 ; k < mats.size() ; k++) {

            if (mats.get(k).size().width > (mats.get(k).size().height * 3)) {
                expression = expression + classes[0];
            } else {
                Mat resized = ImagePreprocessor.resizeImage(mats.get(k), 45, 45);
                Mat invertedImage = ImagePreprocessor.invertImage(resized);

                float[] ip = new float[2025];
                for (int i = 0 ; i < 45 ; i++) {
                    for (int j = 0 ; j < 45 ; j++) {
                        ip[45*i+j] = (float) invertedImage.get(i, j)[0];
                    }
                }

                float[] predictions = new float[25];

                tensorFlowInferenceInterface.feed(INPUT_NAME, ip, 1, 2025);
                tensorFlowInferenceInterface.run(new String[] {OUTPUT_NAME});
                tensorFlowInferenceInterface.fetch(OUTPUT_NAME, predictions);

                int indexMax = 0;
                for (int i = 0 ; i < predictions.length ; i++) {
                    if (predictions[i] > predictions[indexMax]) {
                        indexMax = i;
                    }
                }

                expression = expression + classes[indexMax];
            }
        }

        return expression;
    }

}
