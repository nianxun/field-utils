package com.field.utils.fieldutils.utils;


import org.bytedeco.javacv.CanvasFrame;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.concurrent.TimeUnit;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class DesktopCaptureUtils {
    public static void main(String[] args) {
        try {
            captureScreen();
        } catch (AWTException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void captureScreen() throws AWTException, InterruptedException {
        Rectangle rectangle = new Rectangle(100, 100, 1000, 800);
        //获取本地屏幕设备列表
        GraphicsDevice[] gs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        Robot robot = new Robot(gs[0]);
        // javacv提供的图像展现窗口
        CanvasFrame frame = new CanvasFrame("canvas");
        int width = 800;
        int height = 600;
        frame.setBounds((1000 - width) / 2, (800 - height) / 2, width,
                height);// 窗口居中
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        while (frame.isShowing()) {
            // 从当前屏幕中读取的像素图像，该图像不包括鼠标光标
            frame.showImage(robot.createScreenCapture(rectangle));
            TimeUnit.MILLISECONDS.sleep(50);
        }
        frame.dispose();
    }

}
