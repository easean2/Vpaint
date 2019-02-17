package com.easean2.vpaint;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO_REQUEST_ONE = 333;
    private static final int TAKE_PHOTO_REQUEST_TWO = 444;
    private static final int TAKE_PHOTO_REQUEST_THREE = 555;
    static final double CIRCLE_SIZE_CM = 10;// 圆形实际尺寸10cm

    private Uri imageUri;
    private ImageView iv_image;
    private BitmapOperations bo;
    private Bitmap originalImage;
    private Bitmap showImage;

    private int showDegree;
    boolean detectFlag; //是否需要检测

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        staticLoadCVLibraries();
        setContentView(R.layout.activity_main);
        bo = new BitmapOperations();
        iv_image = findViewById(R.id.wall);
        detectFlag = true; //初始时图片需要检测

        //拍照键处理
        Button btn_take_photo = findViewById(R.id.takePhoto);
        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    imageUri = TakePhotoUtils.takePhoto(MainActivity.this, TAKE_PHOTO_REQUEST_THREE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //旋转键处理
        Button btn_rotate = findViewById(R.id.rotate);
        btn_rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage  = rotateImage(originalImage, iv_image.getWidth());
                iv_image.setImageBitmap(showImage);
            }
        });

        //相册按钮处理
        Button btn_album = findViewById(R.id.album);
        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 222);
            }
        });

        //next按钮处理
        Button btn_next = findViewById(R.id.next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processImg(showImage);
            }
        });
    }

    public double processImg(Bitmap inputImage){
        Mat src = new Mat();
        Bitmap grayBitmap = Bitmap.createBitmap(inputImage.getWidth(), inputImage.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(inputImage, src);
        double rate = 0.0f;
        double[] vCircle1;
        double[] vCircle2;

        Mat dst = src.clone();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);

        Mat circles = new Mat();
        Imgproc.HoughCircles(dst, circles, Imgproc.HOUGH_GRADIENT, 1, 1, 440, 50, 0, 1000);

        Log.i("Result", "find" + circles.cols() + "circles");
        for (int i = 0; i < circles.cols() - 1 && detectFlag; i++) { //检测到2个矩形就退出，flag:是否需要继续检测
            vCircle1 = circles.get(0, i);
            double p1x = vCircle1[0];
            double p1y = vCircle1[1];
            double p1r = vCircle1[2];
            for (int j = i + 1; j < circles.cols(); j++) {
                vCircle2 = circles.get(0, j);
                double p2x = vCircle2[0];
                double p2y = vCircle2[1];
                double p2r = vCircle2[2];
                double dis = getDistance(p2x, p2y, p1x, p1y);
                if ((dis / (p1r + p2r) < 1.05) && (dis / (p1r + p2r) > 0.95)) {
                    Log.i("Result", "\tfind 2 circles:");
                    Log.i("Result", "C" + i + ">>(" + p1x + "," + p1y + ")" + "R:" + p1r);
                    Log.i("Result", "C" + j + ">>(" + p2x + "," + p2y + ")" + "R:" + p2r);
                    Log.i("Result", "dis:" + dis);
                    rate = dis / CIRCLE_SIZE_CM; // 像素与实际尺寸之间的对应比例
                    detectFlag = false;

                    break;
                }
            }
        }

        return rate;
    }

    public static double getDistance(double p1x, double p1y, double p2x, double p2y) {
        double _x = Math.abs(p2x - p1x);
        double _y = Math.abs(p2y - p1y);
        return Math.sqrt(_x * _x + _y * _y);
    }

    private void convertGray(Bitmap inputImage) {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(inputImage, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(dst, inputImage);
        iv_image.setImageBitmap(inputImage);
    }

    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath ;
        switch (requestCode) {
            case 111:
            case 222:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }

                if (data != null) {
                    imageUri = data.getData();
                }
                imgPath = getImagePath(imageUri, null);
                originalImage = BitmapFactory.decodeFile(imgPath, getOptions(imgPath));
                showImage = fitImageView(originalImage, imgPath, iv_image.getWidth());
                iv_image.setImageBitmap(showImage);

                break;
            case TAKE_PHOTO_REQUEST_ONE:
                if (resultCode == RESULT_CANCELED) {
                    delteImageUri(MainActivity.this, imageUri);
                    Toast.makeText(MainActivity.this, "点击取消  拍照", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    //如果拍照图片过大会无法显示
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    iv_image.setImageBitmap(bitmap1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TAKE_PHOTO_REQUEST_TWO:
                if (resultCode == RESULT_CANCELED) {
                    delteImageUri(MainActivity.this, imageUri);
                    return;
                }
                Bitmap photo = null;
                if (data != null) {
                    photo = data.getParcelableExtra("data");
                }
                iv_image.setImageBitmap(photo);

                break;
            case TAKE_PHOTO_REQUEST_THREE:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }

                imgPath = imageUri.getPath();
                originalImage = BitmapFactory.decodeFile(imgPath, getOptions(imgPath));
                showImage = fitImageView(originalImage, imgPath, iv_image.getWidth());
                iv_image.setImageBitmap(showImage);

                break;

            default:
                break;
        }
    }

    //让初始照片适应imageview的尺寸
    private Bitmap fitImageView(Bitmap inputImage, String imgPath, int newWidth){
        Bitmap outImage;

        showDegree = getExifOrientation(imgPath);
        Log.d("ERR", "图片角度为：" + showDegree);

        Bitmap tempImg = bo.rotaingImage(inputImage, showDegree);

//        按比例缩放图片
        float rate = (float)tempImg.getHeight() / (float)tempImg.getWidth();
        int height = (int)(rate * newWidth);
        outImage = bo.bitmapScale(tempImg, newWidth, height);

        return outImage;
    }

    private Bitmap rotateImage(Bitmap inputImage, int newWidth){
        Bitmap outImage;

        showDegree = (showDegree + 90) % 360;
        Log.d("ERR", "旋转后图片角度为：" + showDegree);
        Bitmap tempImg = bo.rotaingImage(inputImage, showDegree);
        //        按比例缩放图片
        float rate = (float)tempImg.getHeight() / (float)tempImg.getWidth();
        int height = (int)(rate * newWidth);
        outImage = bo.bitmapScale(tempImg, newWidth, height);

        return outImage;
    }

    public static void delteImageUri(Context context, Uri uri) {
        context.getContentResolver().delete(uri, null, null);
    }
    /**
     * 获取压缩图片的options
     *
     */
    public static BitmapFactory.Options getOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 2;      //此项参数可以根据需求进行计算
        options.inJustDecodeBounds = false;

        return options;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null; // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            Log.d("ERR", "cannot read exif" + ex);
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }
}
