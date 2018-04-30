package com.example.mahmoud.easymath;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public final class ImagePreprocessor {

    public static Mat convertBitmapToMat (Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        return mat;
    }

    public static Mat convertToGrayScale (Mat image) {
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGB2GRAY);
        return gray;
    }

    public static Mat blurImage (Mat image) {
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(image, blurred, new Size(3,3), 0);
        return blurred;
    }

    public static Mat invertImage (Mat image) {
        Mat inverted = new Mat();
        Core.bitwise_not(image, inverted);
        return inverted;
    }

    public static Mat resizeImage (Mat image, int width, int height) {
        Mat resized = new Mat();
        Imgproc.resize(image, resized, new Size(width, height));
        return resized;
    }

    public static Mat rotateImage (Mat image) {
        Mat rotated  = new Mat();
        List<Point> points = new ArrayList<>();
        for (int i = 0 ; i < image.rows(); i++) {
            for (int j = 0 ; j < image.cols(); j++) {
                double pixel = image.get(i,j)[0];
                if (pixel == 255.0) {
                    Point p = new Point(i,j);
                    points.add(p);
                }
            }
        }
        MatOfPoint2f matOfPoints = new MatOfPoint2f();
        matOfPoints.fromList(points);
        RotatedRect rotatedRect = Imgproc.minAreaRect(matOfPoints);

        double theta = rotatedRect.angle;
        if(theta < -45) {
            theta += 90;
        }

        Mat box = new Mat();
        Imgproc.boxPoints(rotatedRect, box);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(rotatedRect.center, -theta, 1);

        Imgproc.warpAffine(image, rotated, rotationMatrix, image.size(), Imgproc.INTER_CUBIC);

        return rotated;
    }

    public static Mat thresholdImage (Mat image) {
        Mat thresholded = new Mat();
        Imgproc.adaptiveThreshold(image, thresholded, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 10);
        return thresholded;
    }

    public static List<MatOfPoint> getContours (Mat image) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat image32S = new Mat();
        image.convertTo(image32S, CvType.CV_8UC1);
        Imgproc.findContours(image32S, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS, new Point(0,0));
        return contours;
    }

    public static List<MatOfPoint2f> getContoursPoly (List<MatOfPoint> contours) {
        List<MatOfPoint2f> contoursPoly = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Point[] pts = contour.toArray();
            MatOfPoint2f contour2f = new MatOfPoint2f(pts);
            MatOfPoint2f contourPoly = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, contourPoly, 3, true);
            contoursPoly.add(contourPoly);
        }
        return contoursPoly;
    }

    public static List<Rect> getRects (List<MatOfPoint2f> contoursPoly) {
        List<Rect> rects = new ArrayList<>();

        for (int i = 0 ; i < contoursPoly.size() ; i++) {
            Point[] pts1 = contoursPoly.get(i).toArray();
            MatOfPoint contour1 = new MatOfPoint(pts1);
            Rect rect1 = Imgproc.boundingRect(contour1);
            int w1 = rect1.width;
            int h1 = rect1.height;
            int x1 = rect1.x;
            int y1 = rect1.y;
            if (w1*h1 < 100)
                continue;

            boolean inside = false;
            for (int j = 0 ; j < contoursPoly.size() ; j++) {
                if (i == j)
                    continue;
                Point[] pts2 = contoursPoly.get(j).toArray();
                MatOfPoint contour2 = new MatOfPoint(pts2);
                Rect rect2 = Imgproc.boundingRect(contour2);
                int w2 = rect2.width;
                int h2 = rect2.height;
                int x2 = rect2.x;
                int y2 = rect2.y;
                if (w2*h2 < 100 || h2*w2 < h1*w1)
                    continue;
                if (x1 > x2 && x1+w1 < x2+w2 && y1 > y2 && y1+h1 < y2+h2)
                    inside = true;
            }
            if (inside)
                continue;
            rects.add(rect1);
        }

        for (int i = 0 ; i < rects.size() ; i++) {
            for (int j = 0 ; j < rects.size() ; j++) {
                if (i != j && rects.get(j).x > rects.get(i).x) {
                    Rect temp = rects.get(i);
                    rects.set(i, rects.get(j));
                    rects.set(j, temp);
                }
            }
        }

        return rects;
    }

    public static ArrayList<Mat> getCroppedMats (Mat image, List<Rect> rects) {
        ArrayList<Mat> Mats = new ArrayList<>();
        for (int i = 0 ; i < rects.size() ; i++) {
            Mat sympol = new Mat(image, rects.get(i));
            Mats.add(sympol);
        }
        return Mats;
    }

    public static ArrayList<Mat> getCroppedSymbols (Bitmap bitmap, boolean isRotated) {
        Mat image = convertBitmapToMat(bitmap);
        Mat gray = convertToGrayScale(image);
        Mat blurred = blurImage(gray);
        Mat thresholded = thresholdImage(blurred);
        Mat inverted = invertImage(thresholded);
        Mat image2 = new Mat();
        if (isRotated) {
            image2 = rotateImage(inverted);
        } else {
            inverted.copyTo(image2);
        }
        List<MatOfPoint> contours = getContours(image2);
        List<MatOfPoint2f> contoursPoly = getContoursPoly(contours);
        List<Rect> rects = getRects(contoursPoly);
        ArrayList<Mat> croppedSymbols = getCroppedMats(image2, rects);
        return croppedSymbols;
    }

}
