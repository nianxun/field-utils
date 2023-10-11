package com.field.utils.fieldutils.utils;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * @author Field
 * @date 2023-10-11 17:55
 **/
public class ImageSimilarityCalculator {

    public static double calculateSimilarity(String imagePath1, String imagePath2) {
        // 读取图片
        org.bytedeco.opencv.opencv_core.Mat image1 = opencv_imgcodecs.imread(imagePath1, Imgcodecs.IMREAD_COLOR);
        org.bytedeco.opencv.opencv_core.Mat image2 = opencv_imgcodecs.imread(imagePath2, Imgcodecs.IMREAD_COLOR);
        // 转换为HSV颜色空间
        org.bytedeco.opencv.opencv_core.Mat hsvImage1 = new org.bytedeco.opencv.opencv_core.Mat();
        org.bytedeco.opencv.opencv_core.Mat hsvImage2 = new org.bytedeco.opencv.opencv_core.Mat();
        opencv_imgproc.cvtColor(image1, hsvImage1, Imgproc.COLOR_BGR2HSV);
        opencv_imgproc.cvtColor(image2, hsvImage2, Imgproc.COLOR_BGR2HSV);

        // 划分颜色空间
        int[] channels = {0, 1};
        int[] histSize = {50, 60};
        float[] hueRange = {0, 180};
        float[] saturationRange = {0, 256};
        float[][] ranges = {hueRange, saturationRange};

        // 计算直方图
        org.bytedeco.opencv.opencv_core.Mat histImage1 = new org.bytedeco.opencv.opencv_core.Mat();
        org.bytedeco.opencv.opencv_core.Mat histImage2 = new org.bytedeco.opencv.opencv_core.Mat();
        opencv_imgproc.calcHist(
                hsvImage1,
                1,
                channels,
                new org.bytedeco.opencv.opencv_core.Mat(),
                histImage1,
                1,
                histSize,
                saturationRange
        );
        opencv_imgproc.calcHist(
                hsvImage2,
                1,
                channels,
                new org.bytedeco.opencv.opencv_core.Mat(),
                histImage2,
                1,
                histSize,
                saturationRange
        );
        // 归一化直方图
        opencv_core.normalize(histImage1, histImage1, 0, 1, Core.NORM_MINMAX, -1, new org.bytedeco.opencv.opencv_core.Mat());
        opencv_core.normalize(histImage2, histImage2, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        // 计算直方图相似度
        opencv_imgproc.contourArea(histImage1);
        return opencv_imgproc.compareHist(histImage1, histImage2, Imgproc.CV_COMP_CORREL);
    }

}
