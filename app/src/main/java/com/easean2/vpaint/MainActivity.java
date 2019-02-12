package com.easean2.vpaint;

import android.content.Context;
import android.content.Intent;
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

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO_REQUEST_ONE = 333;
    private static final int TAKE_PHOTO_REQUEST_TWO = 444;
    private static final int TAKE_PHOTO_REQUEST_THREE = 555;

    private Button btn_take_photo;
    private Uri imageUri;
    private ImageView iv_image;
    private BitmapOperations bo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bo = new BitmapOperations();
        iv_image = findViewById(R.id.wall);
        btn_take_photo = findViewById(R.id.takePhoto);
        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                imageUri = createImageUri(MainActivity.this);
//                Intent intent = new Intent();
//                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//如果不设置EXTRA_OUTPUT getData()  获取的是bitmap数据  是压缩后的
//                startActivityForResult(intent, TAKE_PHOTO_REQUEST_ONE);
                try {
                    imageUri = TakePhotoUtils.takePhoto(MainActivity.this, TAKE_PHOTO_REQUEST_THREE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 111:
            case 222:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }
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
                Bitmap photo = data.getParcelableExtra("data");
                iv_image.setImageBitmap(photo);
                break;
            case TAKE_PHOTO_REQUEST_THREE:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }

                String imgPath = imageUri.getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(imgPath, getOptions(imgPath));
                Bitmap showImage = fitImageView(bitmap, imgPath, iv_image.getWidth());
                iv_image.setImageBitmap(showImage);
                break;

            default:
                break;
        }
    }

    private Bitmap fitImageView(Bitmap inputImage, String imgPath, int newWidth){
        Bitmap outImage = null;

        int degree = getExifOrientation(imgPath);
        Log.d("ERR", "图片角度为：" + degree);
        //图片垂直时才选择旋转
        Bitmap tempImg = null;
        if(degree == 90 || degree == 270){
            tempImg = bo.rotaingImage(inputImage, degree);
        }else{
            tempImg = inputImage;
        }

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
     * @return
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
