package com.easean2.vpaint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class BitmapOperations {
    private Paint paint;

    public BitmapOperations() {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
    }

    /**
     * 按比例缩放图片
     * baseBitmap: 需要缩放的原图
     * float x:横向缩放比例
     * float y:纵向缩放比例
     */
    public Bitmap bitmapScale(Bitmap baseBitmap, float x, float y) {
        // 因为要将图片放大，所以要根据放大的尺寸重新创建Bitmap
        Bitmap afterBitmap = Bitmap.createBitmap(
                (int) (baseBitmap.getWidth() * x),
                (int) (baseBitmap.getHeight() * y), baseBitmap.getConfig());
        Canvas canvas = new Canvas(afterBitmap);
        // 初始化Matrix对象
        Matrix matrix = new Matrix();
        // 根据传入的参数设置缩放比例
        matrix.setScale(x, y);
        // 根据缩放比例，把图片draw到Canvas上
        canvas.drawBitmap(baseBitmap, matrix, this.paint);
        return afterBitmap;
    }

    /**
     * 按给定尺寸缩放图片
     * baseBitmap: 需要缩放的原图
     * new_width:缩放后的宽度
     * new_height:缩放后的高度
     */
    public Bitmap bitmapScale(Bitmap baseBitmap, int new_width, int new_height) {
        // 因为要将图片放大，所以要根据放大的尺寸重新创建Bitmap
        Bitmap afterBitmap = Bitmap.createBitmap(
                new_width, new_height, baseBitmap.getConfig());
        Canvas canvas = new Canvas(afterBitmap);
        // 初始化Matrix对象
        Matrix matrix = new Matrix();
        // 根据传入的参数设置缩放比例
        float sx = (float)new_width / (float)baseBitmap.getWidth();
        float sy = (float)new_height / (float)baseBitmap.getHeight();
        matrix.setScale(sx, sy);
        // 根据缩放比例，把图片draw到Canvas上
        canvas.drawBitmap(baseBitmap, matrix, this.paint);
        return afterBitmap;
    }

    /**
     * 倾斜图片
     */
    protected Bitmap bitmapSkew(Bitmap baseBitmap, float dx, float dy) {
        // 根据图片的倾斜比例，计算变换后图片的大小，
        Bitmap afterBitmap = Bitmap.createBitmap(baseBitmap.getWidth()
                + (int) (baseBitmap.getWidth() * dx), baseBitmap.getHeight()
                + (int) (baseBitmap.getHeight() * dy), baseBitmap.getConfig());
        Canvas canvas = new Canvas(afterBitmap);
        Matrix matrix = new Matrix();
        // 设置图片倾斜的比例
        matrix.setSkew(dx, dy);
        canvas.drawBitmap(baseBitmap, matrix, paint);
        return afterBitmap;
    }

    /**
     * 图片移动
     */
    protected Bitmap bitmapTranslate(Bitmap baseBitmap,float dx, float dy) {
        // 需要根据移动的距离来创建图片的拷贝图大小
        Bitmap afterBitmap = Bitmap.createBitmap(
                (int) (baseBitmap.getWidth() + dx),
                (int) (baseBitmap.getHeight() + dy), baseBitmap.getConfig());
        Canvas canvas = new Canvas(afterBitmap);
        Matrix matrix = new Matrix();
        // 设置移动的距离
        matrix.setTranslate(dx, dy);
        canvas.drawBitmap(baseBitmap, matrix, paint);
        return afterBitmap;
    }

    /**
     * 图片绕中心旋转
     */
    protected Bitmap bitmapRotate(Bitmap baseBitmap, float degrees) {
        // 创建一个和原图一样大小的图片
        Bitmap afterBitmap = Bitmap.createBitmap(baseBitmap.getWidth(),
                baseBitmap.getHeight(), baseBitmap.getConfig());
        Canvas canvas = new Canvas(afterBitmap);
        Matrix matrix = new Matrix();
        // 根据原图的中心位置旋转
        matrix.setRotate(degrees, baseBitmap.getWidth() / 2,
                baseBitmap.getHeight() / 2);
        canvas.drawBitmap(baseBitmap, matrix, paint);
        return afterBitmap;
    }
    /**
     * 图片绕中心旋转
     */
    public static Bitmap rotaingImage(Bitmap bitmap, int angle) {
        //旋转图片 动作
        Matrix matrix = new Matrix();;
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
}
