package io.github.adsuper.bitmapdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

import io.github.adsuper.disklrucache.DiskLruCache;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private static final long disk_cache_size = 1024 * 1024 * 50;//disklrucache 硬盘缓存目录大小
    private LruCache<String, Bitmap> mBitmapMemoryLruCache;
    private String mCanonicalPath;
    private BitmapUtils mBitmapUtils;

    private Context mContext;
    //image 的宽高
    private int mWidth;
    private int mHeight;

    private Handler mHandler;
    private Bitmap mBitmap;
    private BitmapCaChe mBitmapCaChe;
    private DiskLruCache mDiskLruCache;
    private ImageView mImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mBitmapCaChe = new BitmapCaChe();

        mBitmapUtils = new BitmapUtils();

        mImageView = (ImageView) findViewById(R.id.iamgeView);

        initDiskLruCache();
        diskInput();

//        getImageViewSize(mImageView);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        mBitmap = mBitmapUtils.readBitmapFromResource(getResources(), R.mipmap.me, mWidth, mHeight);
                        //弱引用
                        SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(mBitmap);
                        Bitmap bitmap = softReference.get();

                        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.me);
                        int ii = bitmap1.getRowBytes() * bitmap1.getHeight() / 1024;
                        Log.d(TAG, "onCreate: 原始 bitmap 大小：：" + ii + "kb");
                        mImageView.setImageBitmap(mBitmap);

                        int width = mBitmap.getWidth();
                        int height = mBitmap.getHeight();

                        Log.d(TAG, "onCreate: 采样后 bitmap 宽高：：" + width + "*" + height);

                        int i = mBitmap.getRowBytes() * mBitmap.getHeight() / 1024;
                        Log.d(TAG, "onCreate: 采样后 bitmap 大小：：" + i + "kb");
                        break;

                }

            }
        };
        initLruCache();
//        initDir();


    }

    private void initDir() {
        //获取包名目录下cache 目录
        File cacheDir = getCacheDir();
        String name = cacheDir.getName();
        String path = cacheDir.getPath();// /data/user/0/io.github.adsuper.bitmapdemo/cache
        String parent = cacheDir.getParent();// /data/user/0/io.github.adsuper.bitmapdemo
        File absoluteFile = cacheDir.getAbsoluteFile();// /data/user/0/io.github.adsuper.bitmapdemo/cache
        try {
            mCanonicalPath = cacheDir.getCanonicalPath();// /data/data/io.github.adsuper.bitmapdemo/cache
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onCreate: cache目录::::" + mCanonicalPath);
        Log.d(TAG, "onCreate: getPath::::" + path);
        Log.d(TAG, "onCreate: getParent::::" + parent);
        //获取包名目录下 files 目录
        File filesDir = getFilesDir();
        String name1 = filesDir.getName();
        String path1 = filesDir.getPath();// /data/user/0/io.github.adsuper.bitmapdemo/files
        String absolutePath1 = filesDir.getAbsolutePath();// /data/user/0/io.github.adsuper.bitmapdemo/files
        Log.d(TAG, "onCreate: filesDir::::" + absolutePath1);
        Log.d(TAG, "onCreate: getPath::::" + path1);
        //获取 SD 卡中的缓存目录
        File externalCacheDir = getExternalCacheDir();
        String name2 = externalCacheDir.getName();
        String path2 = externalCacheDir.getPath();// /storage/emulated/0/Android/data/io.github.adsuper.bitmapdemo/cache
        String absolutePath = externalCacheDir.getAbsolutePath();// /storage/emulated/0/Android/data/io.github.adsuper.bitmapdemo/cache
        Log.d(TAG, "onCreate: externalCacheDir::::" + absolutePath);
        Log.d(TAG, "onCreate: getPath::::" + path2);


        String externalStorageState = Environment.getExternalStorageState();
        Log.d(TAG, "onCreate: SD卡状态：" + externalStorageState);

        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "onCreate: SD卡正常挂载");
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            try {
                String canonicalPath = externalStorageDirectory.getCanonicalPath();// /storage/emulated/0
                String absolutePath2 = externalStorageDirectory.getAbsolutePath();// /storage/emulated/0
                String path3 = externalStorageDirectory.getPath();// /storage/emulated/0
                String path4 = getExternalCacheDir().getPath();
                Log.d(TAG, "onCreate: getCanonicalPath::" + canonicalPath);
                Log.d(TAG, "onCreate: getAbsolutePath::" + absolutePath2);
                Log.d(TAG, "onCreate: getPath::" + path3);
                Log.d(TAG, "onCreate: SD卡默认缓存目录::" + path4);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void initLruCache() {
        /**
         * 初始化 LruCache
         */
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);//计算运行分配的内存
        int cacheSize = maxMemory / 8;//分配缓存内存为 总内存的 1/8
        mBitmapMemoryLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;//计算 bitmap 对象的大小
            }

            //LruCache 移除旧缓存时调用
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    /**
     * 获取 imageview 的宽高尺寸
     *
     * @param imageView
     */
    private void getImageViewSize(final ImageView imageView) {
        final ViewTreeObserver imageViewTreeObserver = imageView.getViewTreeObserver();
        imageViewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                mWidth = imageView.getMeasuredWidth();
                mHeight = imageView.getMeasuredHeight();
                Log.d(TAG, "onCreate: imageView 111111宽高转px之前为：：" + mWidth + "*" + mHeight);
                mHandler.sendEmptyMessage(1);
                return true;
            }
        });

    }


    private void initDiskLruCache() {

        File cacheDir = mBitmapCaChe.getDiskCacheDir(mContext, "bitmap");
        String path = cacheDir.getPath();
        Log.d(TAG, "initDiskLruCache: 存储目录：：："+ path );
        int appVersion = mBitmapCaChe.getAppVersion(mContext);
        try {
            mDiskLruCache = DiskLruCache.open(cacheDir, appVersion, 1, disk_cache_size);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void diskInput() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String imageUrl = "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
                    String key = mBitmapCaChe.hashKeyForDisk(imageUrl);
                    Log.d(TAG, "run: Bitmap 存储时候的 key ：：："+ key);
                    DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        InputStream inputStream = editor.newInputStream(0);
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageBitmap(bitmap);
                            }
                        });
                        OutputStream outputStream = editor.newOutputStream(0);
                        if (mBitmapCaChe.downloadUrlToStream(imageUrl, outputStream)) {
                            editor.commit();
                        } else {
                            editor.abort();
                        }
                    }
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
