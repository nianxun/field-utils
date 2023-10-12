package com.field.utils.fieldutils.utils.image.common;

import java.awt.image.BufferedImage;

public class Sharpen {

    private final int[][] OPERATOR_SOBEL_X = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    private final int[][] OPERATOR_SOBEL_Y = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

    //sobel算子对图像进行锐化
    public BufferedImage getSharpenSobel(BufferedImage bufImg, int Width, int Height) {
        BufferedImage sharpen = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);
        int alpha = (bufImg.getRGB(0, 0) >> 24) & 0xff;

        for (int i = 1; i < Width - 1; i++) {
            for (int j = 1; j < Height - 1; j++) {
                //找中心点及周围的8个点
                int p0 = bufImg.getRGB(i - 1, j - 1) & 0xff;
                int p1 = bufImg.getRGB(i - 1, j) & 0xff;
                int p2 = bufImg.getRGB(i - 1, j + 1) & 0xff;
                int p3 = bufImg.getRGB(i, j - 1) & 0xff;
                int p4 = bufImg.getRGB(i, j) & 0xff;
                int p5 = bufImg.getRGB(i, j + 1) & 0xff;
                int p6 = bufImg.getRGB(i + 1, j - 1) & 0xff;
                int p7 = bufImg.getRGB(i + 1, j) & 0xff;
                int p8 = bufImg.getRGB(i + 1, j + 1) & 0xff;
                //卷积计算
                int sharpen_x = p0 * OPERATOR_SOBEL_X[0][0] + p1 * OPERATOR_SOBEL_X[1][0] + p2 * OPERATOR_SOBEL_X[2][0]
                        + p6 * OPERATOR_SOBEL_X[0][2] + p7 * OPERATOR_SOBEL_X[1][2] + p8 * OPERATOR_SOBEL_X[2][2];
                int sharpen_y = p0 * OPERATOR_SOBEL_Y[0][0] + p3 * OPERATOR_SOBEL_Y[0][1] + p6 * OPERATOR_SOBEL_Y[0][2]
                        + p2 * OPERATOR_SOBEL_Y[2][0] + p5 * OPERATOR_SOBEL_Y[2][1] + p8 * OPERATOR_SOBEL_Y[2][2];
                int sharpen_xy = (int) Math.sqrt((Math.pow(sharpen_x, 2.0) + Math.pow(sharpen_y, 2.0)));
                int rgb = (alpha << 24) | (sharpen_xy << 16) | (sharpen_xy << 8) | sharpen_xy;
                sharpen.setRGB(i, j, rgb);
            }
        }
        return sharpen;
    }
}
