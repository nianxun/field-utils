package com.field.utils.fieldutils.utils.image.common;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.xfeatures2d.SURF;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Field
 * @date 2023-10-12 21:53
 **/
public class ImageRecognition {

    public static void matchImage(Mat templateImage, Mat originalImage) {
        MatOfKeyPoint templateKeyPoints = new MatOfKeyPoint();
        //指定特征点算法SURF
        org.opencv.xfeatures2d.SURF surf = SURF.create();
        //获取模板图的特征点
        surf.detect(templateImage, templateKeyPoints);
        //提取模板图的特征点
        MatOfKeyPoint templateDescriptors = new MatOfKeyPoint();
        System.out.println("提取模板图的特征点");
        surf.compute(templateImage, templateKeyPoints, templateDescriptors);

        //显示模板图的特征点图片
        org.opencv.core.Mat outputImage = new org.opencv.core.Mat(templateImage.rows(), templateImage.cols(), Imgcodecs.IMREAD_COLOR);
        System.out.println("在图片上显示提取的特征点");
        Features2d.drawKeypoints(templateImage, templateKeyPoints, outputImage, new org.opencv.core.Scalar(255, 0, 0), 0);

        //获取原图的特征点
        MatOfKeyPoint originalKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint originalDescriptors = new MatOfKeyPoint();
        surf.detect(originalImage, originalKeyPoints);
        System.out.println("提取原图的特征点");
        surf.compute(originalImage, originalKeyPoints, originalDescriptors);

        List<MatOfDMatch> matches = new LinkedList<>();
        org.opencv.features2d.DescriptorMatcher descriptorMatcher = org.opencv.features2d.DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        System.out.println("寻找最佳匹配");
        /*
          knnMatch方法的作用就是在给定特征描述集合中寻找最佳匹配
          使用KNN-matching算法，令K=2，则每个match得到两个最接近的descriptor，然后计算最接近距离和次接近距离之间的比值，当比值大于既定值时，才作为最终match。
         */
        descriptorMatcher.knnMatch(templateDescriptors, originalDescriptors, matches, 2);

        System.out.println("计算匹配结果");
        LinkedList<org.opencv.core.DMatch> goodMatchesList = new LinkedList<>();
        float nndrRatio = 0.7f;//这里设置既定值为0.7，该值可自行调整

        //对匹配结果进行筛选，依据distance进行筛选
        matches.forEach(match -> {
            org.opencv.core.DMatch[] dmatcharray = match.toArray();
            org.opencv.core.DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];

            if (m1.distance <= m2.distance * nndrRatio) {
                goodMatchesList.addLast(m1);
            }
        });

        //当匹配后的特征点大于等于 100 个，则认为模板图在原图中，该值可以自行调整
        if (goodMatchesList.size() >= 100) {
            System.out.println("模板图在原图匹配成功！");

            List<KeyPoint> templateKeyPointList = templateKeyPoints.toList();
            List<KeyPoint> originalKeyPointList = originalKeyPoints.toList();
            LinkedList<Point> objectPoints = new LinkedList<>();
            LinkedList<Point> scenePoints = new LinkedList<>();
            goodMatchesList.forEach(goodMatch -> {
                objectPoints.addLast(templateKeyPointList.get(goodMatch.queryIdx).pt);
                scenePoints.addLast(originalKeyPointList.get(goodMatch.trainIdx).pt);
            });
            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
            objMatOfPoint2f.fromList(objectPoints);
            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
            scnMatOfPoint2f.fromList(scenePoints);
            //使用 findHomography 寻找匹配上的关键点的变换
            org.opencv.core.Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

            /*
              透视变换(Perspective Transformation)是将图片投影到一个新的视平面(Viewing Plane)，也称作投影映射(Projective Mapping)。
             */
            org.opencv.core.Mat templateCorners = new org.opencv.core.Mat(4, 1, CvType.CV_32FC2);
            org.opencv.core.Mat templateTransformResult = new org.opencv.core.Mat(4, 1, CvType.CV_32FC2);
            templateCorners.put(0, 0, 0, 0);
            templateCorners.put(1, 0, templateImage.cols(), 0);
            templateCorners.put(2, 0, templateImage.cols(), templateImage.rows());
            templateCorners.put(3, 0, 0, templateImage.rows());
            //使用 perspectiveTransform 将模板图进行透视变以矫正图象得到标准图片
            Core.perspectiveTransform(templateCorners, templateTransformResult, homography);

            //矩形四个顶点
            double[] pointA = templateTransformResult.get(0, 0);
            double[] pointB = templateTransformResult.get(1, 0);
            double[] pointC = templateTransformResult.get(2, 0);
            double[] pointD = templateTransformResult.get(3, 0);

            //指定取得数组子集的范围
            int rowStart = Math.min((int) pointA[0], (int) pointC[0]);
            int rowEnd = Math.max((int) pointA[0], (int) pointC[0]);
            int colStart = Math.min((int) pointD[0], (int) pointB[0]);
            int colEnd = Math.max((int) pointD[0], (int) pointB[0]);
            Mat subMat = originalImage.submat(rowStart, rowEnd, colStart, colEnd);
            Imgcodecs.imwrite("D:\\work\\t1.jpg", subMat);

            //将匹配的图像用用四条线框出来
            Imgproc.line(originalImage, new Point(pointA), new Point(pointB), new org.opencv.core.Scalar(0, 255, 0), 4);//上 A->B
            Imgproc.line(originalImage, new Point(pointB), new Point(pointC), new org.opencv.core.Scalar(0, 255, 0), 4);//右 B->C
            Imgproc.line(originalImage, new Point(pointC), new Point(pointD), new org.opencv.core.Scalar(0, 255, 0), 4);//下 C->D
            Imgproc.line(originalImage, new Point(pointD), new Point(pointA), new org.opencv.core.Scalar(0, 255, 0), 4);//左 D->A

            MatOfDMatch goodMatches = new MatOfDMatch();
            goodMatches.fromList(goodMatchesList);
            org.opencv.core.Mat matchOutput = new org.opencv.core.Mat(originalImage.rows() * 2, originalImage.cols() * 2, Imgcodecs.IMREAD_COLOR);
            Features2d.drawMatches(templateImage, templateKeyPoints, originalImage, originalKeyPoints, goodMatches, matchOutput, new org.opencv.core.Scalar(0, 255, 0), new Scalar(255, 0, 0), new MatOfByte(), 2);

            Imgcodecs.imwrite("D:\\work\\t2.jpg", matchOutput);
            Imgcodecs.imwrite("D:\\work\\t3.jpg", originalImage);
        } else {
            System.out.println("模板图不在原图中！");
        }
        Imgcodecs.imwrite("D:\\work\\t4.jpg", outputImage);
    }

}
