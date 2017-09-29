package io.github.adsuper.bitmapdemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * 作者：luoshen/rsf411613593@gmail.com
 * 时间：2017年09月28日
 * 说明：
 */

public class BitmapUtils {

    private static final String TAG = "BitmapUtils";


    /**
     * 从 Resource 读取图片
     * @param resources
     * @param resId 资源 id
     * @param width imageview 的宽度 （px）
     * @param height imageView 的 高度（px）
     * @return 采样后的bitmap
     */
    public Bitmap readBitmapFromResource(Resources resources, int resId, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//只解析原始图片的宽高
        BitmapFactory.decodeResource(resources, resId, options);
        //原始图片的宽高
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        //图片存储格式
        String outMimeType = options.outMimeType;
        //图片中每个像素的保存方式，默认为Bitmap.Config.ARGB_8888
        Bitmap.Config inPreferredConfig = options.inPreferredConfig;
        //实际设备的像素密度
        int inScreenDensity = options.inScreenDensity;
        // bitmap 像素密度
        int inDensity = options.inDensity;
        Log.d(TAG, "onCreate: inDensity：：" + inDensity);
        Log.d(TAG, "readBitmapFromResource: 图片存储格式：：：" + outMimeType);
        Log.d(TAG, "readBitmapFromResource: 每个像素保存方式：：：" + inPreferredConfig);
        Log.d(TAG, "onCreate: bitmap 原始宽高：：" + srcWidth + "*" + srcHeight);
        int inSampleSize = 1;
        //计算采样率
        if (srcHeight > height || srcWidth > width) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / height);
            } else {
                inSampleSize = Math.round(srcWidth / width);
            }
        }
        options.inJustDecodeBounds = false;
        //设置采样率
        options.inSampleSize = inSampleSize;
        Log.d(TAG, "readBitmapFromResource: 采样率：：：" + inSampleSize);
        return BitmapFactory.decodeResource(resources, resId, options);
    }

    /**
     * drawable 转化为 bitmap
     * @param drawable
     * @return
     */
    public Bitmap drawableToBitmap(Drawable drawable) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        System.out.println("Drawable转Bitmap");
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * @param context
     * @param dpValue
     * @return
     */
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * @param context
     * @param pxValue
     * @return
     */
    public int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
