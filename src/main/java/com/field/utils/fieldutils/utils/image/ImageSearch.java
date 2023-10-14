package com.field.utils.fieldutils.utils.image;


import com.field.utils.fieldutils.utils.image.common.ImageFeature;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Field
 * @date 2023-10-11 22:17
 **/
public class ImageSearch {


    //基于颜色检索
    public void color(Mat mat) {
        double[] color = ImageFeature.calculateColor(mat);
        Map<Integer, Double> map = new TreeMap<>();//相似度

        //当前数据
        List<double[]> colors = new ArrayList<>();

        double d = 0;
        for (int i = 0; i < colors.size(); i++) {
            d = compareColor(color, colors.get(i));
            map.put(i, d);
        }
        ArrayList<Map.Entry<Integer, Double>> entryArrayList = new ArrayList<>(map.entrySet());
        entryArrayList.sort(Map.Entry.comparingByValue());

        //最匹配的图片
        Map.Entry<Integer, Double> integerDoubleEntry = entryArrayList.get(0);
    }

    //基于纹理检索
    public void texture(Mat mat) {
        double[] texture = ImageFeature.calculateTexture(mat);
        Map<Integer, Double> map = new TreeMap<>();//相似度

        //当前数据
        List<double[]> textures = new ArrayList<>();

        double d = 0;
        for (int i = 0; i < textures.size(); i++) {
            d = cosSimilar(texture, textures.get(i));
            map.put(i, d);
        }
        ArrayList<Map.Entry<Integer, Double>> entryArrayList = new ArrayList<>(map.entrySet());

        entryArrayList.sort((o1, o2) -> {
            return o2.getValue().compareTo(o1.getValue());//按value从大到小排序
        });
        //最匹配的图片
        Map.Entry<Integer, Double> integerDoubleEntry = entryArrayList.get(0);
    }

    //基于形状检索
    public void shape(BufferedImage bufImg) throws Exception {

//        File file = new File(filepath);
//        if (!file.exists()) {
//            throw new Exception("file doesn't find.");
//        }
//        ImageIO.read(file)
        double[] hu = ImageFeature.calculateShape(bufImg);

        Map<Integer, Double> map = new TreeMap<Integer, Double>();//相似度
        List<double[]> shapes = new ArrayList<>();

        double d = 0;
        for (int i = 0; i < shapes.size(); i++) {
            d = cosSimilar(hu, shapes.get(i));
            map.put(i, d);
        }
        ArrayList<Map.Entry<Integer, Double>> entryArrayList = new ArrayList<>(map.entrySet());
        entryArrayList.sort((o1, o2) -> {
            return o2.getValue().compareTo(o1.getValue());//按value从大到小排序
        });
        //最匹配的图片
        Map.Entry<Integer, Double> integerDoubleEntry = entryArrayList.get(0);
    }

    //综合
    public void commonSearch(String filepath, Double colorWeight, Double textureWeight, Double shapeWeight) throws Exception {
        if(colorWeight < 0 || colorWeight > 1.0 ||
                textureWeight < 0 || textureWeight > 1.0 ||
                shapeWeight < 0 || shapeWeight > 1.0){
            throw new Exception("weight is error!");
        }

        File file = new File(filepath);
        if (!file.exists()) {
            throw new Exception("file doesn't find.");
        }
        Mat mat = Imgcodecs.imread(filepath);
        double[] hu = ImageFeature.calculateShape(ImageIO.read(file));
        double[] color = ImageFeature.calculateColor(mat);
        double[] texture = ImageFeature.calculateTexture(mat);

        Map<Integer, Double> map = new TreeMap<Integer, Double>();//相似度


        List<double[]> colors = new ArrayList<>();
        List<double[]> textures = new ArrayList<>();
        List<double[]> shapes = new ArrayList<>();

        double colorScore, textureScore, shapeScore, score;
        for (int i = 0; i < shapes.size(); i++) {
            colorScore = compareColor(color, colors.get(i));
            textureScore = cosSimilar(texture, textures.get(i));
            shapeScore = cosSimilar(hu, shapes.get(i));
            score = colorScore * colorWeight + textureScore * textureWeight + shapeScore * shapeWeight;
            map.put(i, score);
        }
        ArrayList<Map.Entry<Integer, Double>> entryArrayList = new ArrayList<>(map.entrySet());
        entryArrayList.sort((o1, o2) -> {
            return o2.getValue().compareTo(o1.getValue());//按value从大到小排序
        });
        //最匹配的图片
        Map.Entry<Integer, Double> integerDoubleEntry = entryArrayList.get(0);

    }

    public static void findImageLocation(Mat image, Mat baseImage){


    }

    //比较特征值 欧氏距离
    public double compareColor(double[] a, double[] b) {
        double D, sum = 0;
        for (int i = 0; i < 9; i++) {
            sum = sum + Math.pow((a[i] - b[i]), 2);
        }
        D = Math.pow(sum, 0.5);
        return D;
    }

    //用余弦定理求匹配图片与数据库中图片的相似度
    public double cosSimilar(double[] a, double[] b) {
        double cosValue = 1, numerator = 0, denominator1 = 0, denominator2 = 0;
        for (int i = 0; i < b.length; i++) {
            numerator += a[i] * b[i];
            denominator1 += a[i] * a[i];
            denominator2 += b[i] * b[i];
        }
        denominator1 = Math.sqrt(denominator1);
        denominator2 = Math.sqrt(denominator2);
        cosValue = numerator / (denominator1 * denominator2);
        return cosValue;
    }


}
