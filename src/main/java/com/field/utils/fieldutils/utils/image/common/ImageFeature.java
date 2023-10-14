package com.field.utils.fieldutils.utils.image.common;


import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Field
 * @date 2023-10-12 10:01
 **/
public class ImageFeature {

    /**
     * 计算颜色特征值
     *
     * @param mat 图片信息
     * @return 特征
     */
    public static double[] calculateColor(Mat mat) {
        //直方图均衡化 图片增强
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2YCrCb);

        List<Mat> list = new ArrayList<>();
        Core.split(mat, list);
        Imgproc.equalizeHist(list.get(0), list.get(0));
        Core.normalize(list.get(0), list.get(0), 0, 255, Core.NORM_MINMAX);
        Core.merge(list, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_YCrCb2BGR);

        //RGB到HSV
        double [] y = new double[9];
        double [] B=new double[mat.rows()* mat.cols()];
        double [] G=new double[mat.rows()* mat.cols()];
        double [] R=new double[mat.rows()* mat.cols()];
        for(int j = 0; j< mat.rows(); j++){
            for(int k = 0; k< mat.cols(); k++){
                double [] data= mat.get(j, k);
                B[j* mat.cols()+k]=data[0];
                G[j* mat.cols()+k]=data[1];
                R[j* mat.cols()+k]=data[2];
            }
        }

        y[0] = mean(B);
        y[1] = std(B, mean(B));
        y[2] = skew(B, mean(B));
        y[3] = mean(G);
        y[4] = std(G, mean(G));
        y[5] = skew(G, mean(G));
        y[6] = mean(R);
        y[7] = std(R, mean(R));
        y[8] = skew(R, mean(R));
        //归一化
        double[] temp = new double[9];
        temp[0] = y[0];
        temp[1] = y[1];
        temp[2] = y[2];
        temp[3] = y[3];
        temp[4] = y[4];
        temp[5] = y[5];
        temp[6] = y[6];
        temp[7] = y[7];
        temp[8] = y[8];
        Arrays.sort(temp);
        double mm = temp[8] - temp[0];
        double min = temp[0];
        for (int i = 0; i < 9; i++) {
            y[i] = (y[i] - min) / mm;
        }
        return y;
    }

    /**
     * 计算图像形状特征值
     *
     * @param bufImg 图像
     */
    public static double[] calculateShape(BufferedImage bufImg) {
        int height = bufImg.getHeight();
        int width = bufImg.getWidth();

        //图像处理
        //图像灰度化
        BufferedImage bufImgGray = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            //注意行列顺序
            for (int j = 0; j < height; j++) {
                int ARGB = bufImg.getRGB(i, j);
                int A = (ARGB >> 24) & 0Xff;
                int R = 30 * (((ARGB >> 16) & 0xff));
                //灰度化公式y=0.30*R+0.59*G+0.11*B
                int G = 59 * (((ARGB >> 8) & 0xff));
                int B = 11 * ((ARGB & 0xff));
                int gray = (R + G + B) / 100;
                int g = (A << 24) | (gray << 16) | (gray << 8) | gray;
                bufImgGray.setRGB(i, j, g);

            }
        }
        bufImg = bufImgGray;

        //图象平滑-中值滤波
        BufferedImage filter = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] pixel = new int[9];
        int t;
        int alpha = (bufImg.getRGB(0, 0) >> 24) & 0xff;
        for (int i = 1; i < width - 1; i++)
            for (int j = 1; j < height - 1; j++) {
                t = 0;
                //找中心及周围的八个点
                int p0 = bufImg.getRGB(i - 1, j - 1) & 0xff;
                pixel[t++] = p0;
                int p1 = bufImg.getRGB(i - 1, j) & 0xff;
                pixel[t++] = p1;
                int p2 = bufImg.getRGB(i - 1, j + 1) & 0xff;
                pixel[t++] = p2;
                int p3 = bufImg.getRGB(i, j - 1) & 0xff;
                pixel[t++] = p3;
                int p4 = bufImg.getRGB(i, j) & 0xff;
                pixel[t++] = p4;
                int p5 = bufImg.getRGB(i, j + 1) & 0xff;
                pixel[t++] = p5;
                int p6 = bufImg.getRGB(i + 1, j - 1) & 0xff;
                pixel[t++] = p6;
                int p7 = bufImg.getRGB(i + 1, j) & 0xff;
                pixel[t++] = p7;
                int p8 = bufImg.getRGB(i + 1, j + 1) & 0xff;
                pixel[t++] = p8;
                int mid = bubbleSort(pixel);
                int rgb = (alpha << 24) | (mid << 16) | (mid << 8) | mid;
                filter.setRGB(i, j, rgb);
            }
        bufImg = filter;
        //图像锐化_Sobel
        BufferedImage bufImg_sharpen = new Sharpen().getSharpenSobel(bufImg, width, height);
        //二值化：迭代阈值法(iteration_threshold)
        bufImg = new IterationThreshold().getIterationThreshold(bufImg_sharpen, width, height);

        //计算矩心
        Centroid centroid = getCentroid(bufImg, width, height);
        //计算中心矩
        double[][] centralMoment = getCentralMoment(bufImg, width, height, centroid);
        //中心矩归一化
        double[][] normalization = getNormalization(centralMoment);
        //计算不变矩和离心率
        return getMomentInvariants(normalization);
    }

    public static double[] calculateTexture(Mat mat) {
        Mat gray = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);//灰度图象
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);//RGB->GRAY
        int height = gray.height();
        int width = gray.width();
        int[][] grayLow = new int[height][width];//降低等级后的灰度图像
        double[][][] glcm = new double[4][8][8];//四个灰度共生矩阵
        int gi, gj;//像素对
        int sum0 = 0, sum45 = 0, sum90 = 0, sum135 = 0;//不同角度灰度共生矩阵中像素点个数
        double[] asm = new double[4];//纹理一致性
        double[] contrast = new double[4];//纹理对比度
        double[] entropy = new double[4];//纹理熵
        double[] correlation = new double[4];//纹理相关性
        double[] ux = new double[4];//相关性的μ
        double[] uy = new double[4];
        double[] ax = new double[4];//相关性的σ
        double[] ay = new double[4];

        //降低灰度等级，分成8个区间
        for(int i=0;i<height;i++) {
            for(int j=0;j<width;j++) {
                grayLow[i][j] = (int) (gray.get(i,j)[0]/32);
            }
        }
        //按四个方向遍历图片，记录灰度值对出现的次数
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //0度
                if (i + 2 >= 0 && i + 2 < height) {
                    gi = grayLow[i][j];
                    gj = grayLow[i + 2][j];
                    glcm[0][gi][gj] += 1;
                    sum0++;
                }
                //45度
                if (i + 2 >= 0 && i + 2 < height && j + 2 >= 0 && j + 2 < width) {
                    gi = grayLow[i][j];
                    gj = grayLow[i + 2][j + 2];
                    glcm[1][gi][gj] += 1;
                    sum45++;
                }
                //90度
                if (j + 2 >= 0 && j + 2 < width) {
                    gi = grayLow[i][j];
                    gj = grayLow[i][j + 2];
                    glcm[2][gi][gj] += 1;
                    sum90++;
                }
                //135度
                if (i - 2 >= 0 && j + 2 >= 0 && j + 2 < width) {
                    gi = grayLow[i][j];
                    gj = grayLow[i - 2][j + 2];
                    glcm[3][gi][gj] += 1;
                    sum135++;
                }
            }
        }
        //求灰度共生矩阵，每个单元为像素对出现的概率
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                glcm[0][i][j] = glcm[0][i][j] / sum0;
                glcm[1][i][j] = glcm[1][i][j] / sum45;
                glcm[2][i][j] = glcm[2][i][j] / sum90;
                glcm[3][i][j] = glcm[3][i][j] / sum135;
            }
        }
        //计算纹理特征
        for (int index = 0; index < 4; index++) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    //纹理一致性
                    asm[index] += Math.pow(glcm[index][i][j], 2);
                    //纹理对比度
                    contrast[index] += Math.pow(i - j, 2) * glcm[index][i][j];
                    //纹理熵
                    if (glcm[index][i][j] != 0) {
                        entropy[index] -= glcm[index][i][j] * Math.log(glcm[index][i][j]);
                    }
                    //相关性的u
                    ux[index] += i * glcm[index][i][j];
                    uy[index] += j * glcm[index][i][j];
                }
            }
        }
        //相关性的σ
        for (int index = 0; index < 4; index++) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    ax[index] += Math.pow(i - ux[index], 2) * glcm[index][i][j];
                    ay[index] += Math.pow(j - uy[index], 2) * glcm[index][i][j];
                    correlation[index] += i * j * glcm[index][i][j];
                }
            }
            //纹理相关性
            if (ax[index] != 0 && ay[index] != 0) {
                correlation[index] = (correlation[index] - ux[index] * uy[index]) / ax[index] / ay[index];
            } else {
                correlation[index] = 8;
            }
        }
        double[] y = new double[8];
        //期望
        y[0] = (asm[0] + asm[1] + asm[2] + asm[3]) / 4;
        y[1] = (contrast[0] + contrast[1] + contrast[2] + contrast[3]) / 4;
        y[2] = (correlation[0] + correlation[1] + correlation[2] + correlation[3]) / 4;
        y[3] = (entropy[0] + entropy[1] + entropy[2] + entropy[3]) / 4;
        //标准差
        y[4] = Math.sqrt(Math.pow(asm[0] - y[0], 2) + Math.pow(asm[1] - y[0], 2) + Math.pow(asm[2] - y[0], 2) + Math.pow(asm[3] - y[0], 2));
        y[5] = Math.sqrt(Math.pow(contrast[0] - y[0], 2) + Math.pow(contrast[1] - y[0], 2) + Math.pow(contrast[2] - y[0], 2) + Math.pow(contrast[3] - y[0], 2));
        ;
        y[6] = Math.sqrt(Math.pow(correlation[0] - y[0], 2) + Math.pow(correlation[1] - y[0], 2) + Math.pow(correlation[2] - y[0], 2) + Math.pow(correlation[3] - y[0], 2));
        ;
        y[7] = Math.sqrt(Math.pow(entropy[0] - y[0], 2) + Math.pow(entropy[1] - y[0], 2) + Math.pow(entropy[2] - y[0], 2) + Math.pow(entropy[3] - y[0], 2));
        ;
        return y;
    }


    /**
     * 一阶矩均值
     */
    private static double mean(double[] data) {
        double sum = 0;
        for (double datum : data) {
            sum += datum;
        }
        return sum / data.length;
    }

    /**
     * 二阶矩方差
     */
    private static double std(double[] data, double mean) {
        double sum = 0;
        for (double datum : data) {
            sum += Math.pow((datum - mean), 2);
        }
        return Math.pow((sum / data.length), 0.5);
    }

    /**
     * 三阶矩斜度
     */
    private static double skew(double[] data, double mean) {
        double sum = 0;
        for (double datum : data) {
            sum += Math.pow((datum - mean), 3);
        }
        return Math.cbrt(sum / data.length);
    }

    /**
     * 排序
     */
    private static int bubbleSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            for (int j = 0; j < a.length - i - 1; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
        return a[a.length / 2];
    }

    /**
     * 计算矩心
     */
    private static Centroid getCentroid(BufferedImage bufImg,
                                        int width,
                                        int height) {
        double M00 = 0.0;
        double M10 = 0.0;
        double M01 = 0.0;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                /*二值图0和255转化为0和1**/
                int pixel = (bufImg.getRGB(i, j) & 0xff) & 1;
                /*目标物体**/
                if (pixel == 1) {
                    M10 += i;
                    M01 += j;
                    M00 += 1;
                }
            }
        Centroid centroid = new Centroid();
        centroid.setX(M10 / M00);
        centroid.setY(M01 / M00);
        return centroid;
    }

    @Getter
    @Setter
    static class Centroid {
        private double x;
        private double y;
    }

    /**
     * 计算中心矩
     */
    private static double[][] getCentralMoment(BufferedImage bufImg,
                                               int width,
                                               int height,
                                               Centroid centroid) {
        double[][] m = new double[4][4];
        for (int p = 0; p < 4; p++) {
            for (int k = 0; k < 4; k++) {
                m[p][k] = 0.0;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        /*二值图0和255转化为0和1**/
                        int pixel = (bufImg.getRGB(i, j) & 0xff) & 1;
                        if (pixel == 1)
                            m[p][k] += Math.pow(i - centroid.getX(), p * 1.0) * Math.pow(j - centroid.getY(), k * 1.0);
                    }
                }
            }
        }
        return m;
    }

    /**
     * 中心矩归一化
     */
    private static double[][] getNormalization(double[][] m) {
        double[][] u = new double[4][4];
        for (int p = 0; p < 4; p++) {
            for (int k = 0; k < 4; k++) {
                u[p][k] = m[p][k] / (Math.pow(m[0][0], (((double) (p + k)) / 2.0 + 1)));
            }
        }
        return u;
    }

    /**
     * 计算不变矩和离心率
     */
    private static double[] getMomentInvariants(double[][] u) {
        double[] e = new double[8];
        //离心率
        e[0] = (Math.pow(u[2][0] - u[0][2], 2.0) + 4 * Math.pow(u[1][1], 2.0)) / Math.pow(u[2][0] + u[0][2], 2.0);

        //不变矩
        e[1] = u[2][0] + u[0][2];
        e[2] = Math.pow(u[2][0] - u[0][2], 2.0) + 4 * Math.pow(u[1][1], 2.0);
        e[3] = Math.pow(u[3][0] - 3 * u[1][2], 2.0) + Math.pow(u[0][3] - 3 * u[2][1], 2.0);
        e[4] = Math.pow(u[3][0] + u[1][2], 2.0) + Math.pow(u[0][3] + u[2][1], 2.0);
        e[5] = (u[3][0] - 3 * u[1][2]) * (u[3][0] + u[1][2]) * (Math.pow(u[3][0] + u[1][2], 2.0)
                - 3 * Math.pow(u[2][1] + u[0][3], 2.0)) + (u[0][3] - 3 * u[2][1]) * (u[0][3] + u[2][1])
                * (Math.pow(u[0][3] + u[2][1], 2.0) - 3 * Math.pow(u[1][2] + u[3][0], 2.0));
        e[6] = (u[2][0] - u[0][2]) * (Math.pow(u[3][0] + u[1][2], 2.0) - Math.pow(u[2][1] + u[0][3], 2.0))
                + 4 * u[1][1] * (u[3][0] + u[1][2]) * (u[0][3] + u[2][1]);
        e[7] = (3 * u[2][1] - u[0][3]) * (u[3][0] + u[1][2]) * (Math.pow(u[3][0] + u[1][2], 2.0) -
                3 * Math.pow(u[0][3] + u[2][1], 2.0)) + (3 * u[1][2] - u[3][0]) * (u[2][1] + u[0][3])
                * (3 * Math.pow(u[3][0] + u[1][2], 2.0) - Math.pow(u[0][3] + u[2][1], 2.0));
        return e;
    }
}
