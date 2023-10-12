package com.field.utils.fieldutils.utils.image.common;
//������ֵ�����ж�ֵ��

import java.awt.image.BufferedImage;
import java.io.IOException;

//��ֵ����������ֵ��
public class IterationThreshold {

    private BufferedImage bufferedImage = null;
    private int T2 = 0;

    //��ȡ��ֵ
    public BufferedImage getIterationThreshold(BufferedImage bufImg, int Width, int Height) {
        int t1 = 0;
        int t0 = 1;
        do {
            t1 = T2;
            int n1;
            int g2;
            int n2;
            int g1 = g2 = n1 = n2 = 0;
            for (int i = 0; i < Width; i++)
                for (int j = 0; j < Height; j++) {
                    int pixel = (bufImg.getRGB(i, j) & 0xff);
                    if (pixel <= t1) {
                        g1 += pixel;
                        n1++;
                    }//����
                    else {
                        g2 += pixel;
                        n2++;
                    }
                }
            T2 = (g1 / n1 + g2 / n2) / 2;//�µ���ֵ
        } while (Math.abs(T2 - t1) > t0);//������ǰ�����������ʱ��ֹͣ����
        this.getBinarization(bufImg, Width, Height, T2);
        return bufferedImage;
    }

    //��ֵ��
    public void getBinarization(BufferedImage bufImg, int Width, int Height, int T) {
        bufferedImage = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);
        int alpha = (bufImg.getRGB(0, 0) >> 24) & 0xff;
        int rgb;
        for (int i = 0; i < Width; i++) {
            for (int j = 0; j < Height; j++) {
                int pixel = (bufImg.getRGB(i, j) & 0xff);
                if (pixel <= T) {
                    rgb = (alpha << 24);
                } else {
                    rgb = (alpha << 24) | (255 << 16) | (255 << 8) | 255;
                }
                bufferedImage.setRGB(i, j, rgb);
            }
        }
    }
}
