package com.rainchen.filetools.utils.compress;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.rainchen.filetools.bean.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImgCompress implements Handler.Callback {
    private static final String TAG = "ImgCompress";
    private static final String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";

    private static final int MSG_COMPRESS_SUCCESS = 0;
    private static final int MSG_COMPRESS_START = 1;
    private static final int MSG_COMPRESS_ERROR = 2;

    private String mTargetDir;
    private List<InputStreamProvider> mStreamProviders;
    private int mLeastCompressSize;
    private OnCompressListener mCompressListener;

    private Handler mHandler;

    private ImgCompress(Builder builder) {
        this.mStreamProviders = builder.mStreamProviders;
        this.mTargetDir = builder.mTargetDir;
        this.mCompressListener = builder.mCompressListener;
        this.mLeastCompressSize = builder.mLeastCompressSize;
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    /**
     * Returns a mFile with a cache audio name in the private cache directory.
     *
     * @param context A context.
     */
    private File getImageCacheFile(Context context, String path) {
        if (TextUtils.isEmpty(mTargetDir)) {
            mTargetDir = getImageCacheDir(context).getAbsolutePath();
        }

        String cacheBuilder = mTargetDir  + path.substring(path.lastIndexOf(File.separator));

        return new File(cacheBuilder);
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved audio.
     *
     * @param context A context.
     * @see #getImageCacheDir(Context, String)
     */
    @Nullable
    private File getImageCacheDir(Context context) {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context   A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getImageCacheDir(Context)
     */
    @Nullable
    private static File getImageCacheDir(Context context, String cacheName) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    /**
     * start asynchronous compress thread
     * 子线程异步压缩
     */
    private void launchAsync(final Context context) {
        if (mStreamProviders == null || mStreamProviders.size() == 0 && mCompressListener != null) {
            mCompressListener.onError(new NullPointerException("image file cannot be null"));
        }

        Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();
        while (iterator.hasNext()) {
            final InputStreamProvider path = iterator.next();
            if (Checker.isImage(path.getPath())) {
                AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START));

                            File result = Checker.isNeedCompress(mLeastCompressSize, path.getPath()) ?
                                    new Engine(path, getImageCacheFile(context, path.getPath())).compress() :
                                    new File(path.getPath());

                            mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS, result));
                        } catch (IOException e) {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR, e));
                        }
                    }
                });
            } else {
                mCompressListener.onError(new IllegalArgumentException("can not read the path : " + path));
            }
            iterator.remove();
        }
    }
/**
 * 主线程同步压缩
 * */
    private void launchOnUiThread(final Context context) {
        if (mStreamProviders == null || mStreamProviders.size() == 0 && mCompressListener != null) {
            mCompressListener.onError(new NullPointerException("image file cannot be null"));
        }

        Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();
        while (iterator.hasNext()) {
            final InputStreamProvider path = iterator.next();
            if (Checker.isImage(path.getPath())) {
                try {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START));

                    File result = Checker.isNeedCompress(mLeastCompressSize, path.getPath()) ?
                            new Engine(path, getImageCacheFile(context, path.getPath())).compress() :
                            new File(path.getPath());

                    mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS, result));
                } catch (IOException e) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR, e));
                }
            } else {
                mCompressListener.onError(new IllegalArgumentException("can not read the path : " + path));
            }
            iterator.remove();
        }
    }


    /**
     * start compress and return the mFile
     */
    @WorkerThread
    private File get(InputStreamProvider path, Context context) throws IOException {
        return new Engine(path, getImageCacheFile(context, Checker.checkSuffix(path.getPath()))).compress();
    }

    @WorkerThread
    private List<File> get(Context context) throws IOException {
        List<File> results = new ArrayList<>();
        Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();

        while (iterator.hasNext()) {
            InputStreamProvider path = iterator.next();
            if (Checker.isImage(path.getPath())) {
                results.add(new Engine(path, getImageCacheFile(context, Checker.checkSuffix(path.getPath()))).compress());
            }
            iterator.remove();
        }

        return results;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (mCompressListener == null) {
            return false;
        }

        switch (msg.what) {
            case MSG_COMPRESS_START:
                Log.d("onStart","===== onStart =====");
                mCompressListener.onStart();
                break;
            case MSG_COMPRESS_SUCCESS:
                File file = (File) msg.obj;
                Log.d("onStart","===== onSuccess =="+(file.isFile()?file.getPath():"file is null"));
                mCompressListener.onSuccess(file);
                break;
            case MSG_COMPRESS_ERROR:
                Exception e = (Exception) msg.obj;
                Log.d("onStart","===== onError ====="+e.getMessage());
                mCompressListener.onError(e);
                break;
                default:break;
        }
        return false;
    }

    public static class Builder {
        private Context context;
        private String mTargetDir;
        private List<InputStreamProvider> mStreamProviders;
        private int mLeastCompressSize = 100;
        private OnCompressListener mCompressListener;

        Builder(Context context) {
            this.context = context;
            this.mStreamProviders = new ArrayList<>();
        }

        private ImgCompress build() {
            return new ImgCompress(this);
        }

        public Builder load(InputStreamProvider inputStreamProvider) {
            mStreamProviders.add(inputStreamProvider);
            return this;
        }

        public Builder load(final File file) {
            mStreamProviders.add(new InputStreamProvider() {
                @Override
                public InputStream open() throws IOException {
                    return new FileInputStream(file);
                }

                @Override
                public String getPath() {
                    return file.getAbsolutePath();
                }
            });
            return this;
        }

        public Builder load(final String path) {
            Log.d("load-path",path);
            mStreamProviders.add(new InputStreamProvider() {
                @Override
                public InputStream open() throws IOException {
                    if (TextUtils.isEmpty(path)){
                        return null;
                    }
                    return new FileInputStream(path);
                }

                @Override
                public String getPath() {
                    return path;
                }
            });
            return this;
        }

        public <T extends FileInfo> Builder load(List<T> list) {
            for (T t : list) {
                load(t.getPath());
            }
            return this;
        }

        public Builder load(final Uri uri) {
            mStreamProviders.add(new InputStreamProvider() {
                @Override
                public InputStream open() throws IOException {
                    return context.getContentResolver().openInputStream(uri);
                }

                @Override
                public String getPath() {
                    return uri.getPath();
                }
            });
            return this;
        }

        public Builder putGear(int gear) {
            return this;
        }

        public Builder setCompressListener(OnCompressListener listener) {
            this.mCompressListener = listener;
            return this;
        }

        public Builder setTargetDir(String targetDir) {
            this.mTargetDir = targetDir;
            return this;
        }

        /**
         * do not compress when the origin image file size less than one value
         *
         * @param size the value of file size, unit KB, default 100K
         */
        public Builder ignoreBy(int size) {
            this.mLeastCompressSize = size;
            return this;
        }

        /**
         * begin compress image with asynchronous
         * 子线程异步压缩
         */
        public void launchAsync() {
            build().launchAsync(context);
        }
        /**
         * 主线程同步压缩
         */
        public void launchOnUiThread() {
            build().launchOnUiThread(context);
        }

        public File get(final String path) throws IOException {
            return build().get(new InputStreamProvider() {
                @Override
                public InputStream open() throws IOException {
                    return new FileInputStream(path);
                }

                @Override
                public String getPath() {
                    return path;
                }
            }, context);
        }

        /**
         * begin compress image with synchronize
         *
         * @return the thumb image file list
         */
        public List<File> get() throws IOException {
            return build().get(context);
        }
    }
}