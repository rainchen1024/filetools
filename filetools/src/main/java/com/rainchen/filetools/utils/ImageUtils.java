package com.rainchen.filetools.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ImageUtils {

    /**
     * 生成圆角图片
     *
     * @param bitmap
     * @return
     */
    public static Bitmap GetRoundedCornerBitmap(Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config
                    .ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            final float roundPx = 14;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

            final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }

    /**
     * dip转换为px
     *
     * @param context
     * @param dip
     * @return
     */
    public static int dipToPx(Context context, float dip) {
        return (int) (dip * getDisplayMetrics(context).density + 0.5f);
    }

    /**
     * px转换为dip
     *
     * @param context
     * @param px
     * @return
     */
    public static int pxToDip(Context context, int px) {
        return (int) (px / getDisplayMetrics(context).density + 0.5f);
    }


    /**
     * 获取DisplayMetrics对象
     *
     * @param context
     * @return
     */
    private static DisplayMetrics getDisplayMetrics(Context context) {
        Display display = getDisplay(context);
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        return dm;
    }

    /**
     * 获取显示设备参数
     *
     * @param context
     * @return
     */
    public static Display getDisplay(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay();
    }

    /**
     * bitmap放大
     * @param sx 宽放大sx倍
     * @param sy 高放大sy倍
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, float sx, float sy) {
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight
                (), matrix, true);
        return resizeBmp;
    }

    /**
     * 创建一条图片地址uri,用于保存拍照后的照片
     *
     * @param context
     * @return 图片的uri
     */
    public static Uri createImagePathUri(Context context) {
        Uri imageFilePath = null;
        String status = Environment.getExternalStorageState();
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));
        // ContentValues是我们希望这条记录被创建时包含的数据信息
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            imageFilePath = context.getContentResolver().insert(MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI, values);
        } else {
            imageFilePath = context.getContentResolver().insert(MediaStore.Images.Media
                    .INTERNAL_CONTENT_URI, values);
        }
        Log.i("", "生成的照片输出路径：" + imageFilePath.toString());
        return imageFilePath;
    }

    /**
     * uri转换为url
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getRealFilePath(Context context, Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore
                    .Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 保存网络图片到本地
     *
     * @param fileUrl
     * @param savePath
     */
    public static void savePicToLocation(String fileUrl, String savePath) {

        try {
            /* 将网络资源地址传给,即赋值给url */
            URL url = new URL(fileUrl);

			/* 此为联系获得网络资源的固定格式用法，以便后面的in变量获得url截取网络资源的输入流 */
            DataInputStream in = new DataInputStream(url.openStream());

			/* 此处也可用BufferedInputStream与BufferedOutputStream 需要保存的路径 */
            DataOutputStream out = new DataOutputStream(new FileOutputStream(savePath));

			/* 将参数savePath，即将截取的图片的存储在本地地址赋值给out输出流所指定的地址 */
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) != 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 转化网络图片为drawable
     *
     * @param imageUrl
     * @param readTimeOutMillis
     * @return
     */
    public static Drawable getDrawableFromUrl(String imageUrl, int readTimeOutMillis) {
        InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOutMillis);
        Drawable d = Drawable.createFromStream(stream, "src");
        closeInputStream(stream);
        return d;
    }

    /**
     * 转化网络图片为bitmap
     *
     * @param imageUrl
     * @param readTimeOutMillis
     * @return
     */
    public static Bitmap getBitmapFromUrl(String imageUrl, int readTimeOutMillis) {
        InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOutMillis);
        Bitmap b = BitmapFactory.decodeStream(stream);
        closeInputStream(stream);
        return b;
    }

    /**
     * 把网络图片转化为流
     *
     * @param imageUrl
     * @param readTimeOutMillis
     * @return
     */
    public static InputStream getInputStreamFromUrl(String imageUrl, int readTimeOutMillis) {
        InputStream stream = null;

        try {
            URL e = new URL(imageUrl);
            HttpURLConnection con = (HttpURLConnection) e.openConnection();
            if (readTimeOutMillis > 0) {
                con.setReadTimeout(readTimeOutMillis);
            }

            stream = con.getInputStream();
            return stream;
        } catch (MalformedURLException e) {
            closeInputStream(stream);
            throw new RuntimeException("MalformedURLException occurred. ", e);
        } catch (IOException e) {
            closeInputStream(stream);
            throw new RuntimeException("IOException occurred. ", e);
        }
    }

    /**
     * 关闭流
     *
     * @param s
     */
    private static void closeInputStream(InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                throw new RuntimeException("IOException occurred. ", e);
            }
        }
    }

    /**
     * 旋转bitmap
     *
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m,
                    true);
            return bitmap;
        }
        return bitmap;
    }

    /**
     * 压缩指定路径的图片，并得到图片对象
     *
     * @param path bitmap source path
     * @return Bitmap {@link Bitmap}
     */
    public final static Bitmap compressBitmap(String path, int rqsW, int rqsH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, rqsW, rqsH);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * calculate the bitmap sampleSize
     *
     * @return
     */
    public final static int calculateInSampleSize(BitmapFactory.Options options, int rqsW, int
            rqsH) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (rqsW == 0 || rqsH == 0) return 1;
        if (height > rqsH || width > rqsW) {
            final int heightRatio = Math.round((float) height / (float) rqsH);
            final int widthRatio = Math.round((float) width / (float) rqsW);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 压缩指定路径图片，并将其保存在缓存目录中，通过isDelSrc判定是否删除源文件，并获取到缓存后的图片路径
     *
     * @param context
     * @param srcPath
     * @param rqsW
     * @param rqsH
     * @param isDelSrc
     * @return
     */
    public final static String compressBitmap(Activity context, String srcPath, int rqsW, int
            rqsH, boolean isDelSrc) {
        Bitmap bitmap = compressBitmap(srcPath, rqsW, rqsH);
        File srcFile = new File(srcPath);
        String desPath = FileUtils.getImageCacheDir(context) + "compress." + srcFile.getName();
        int degree = getDegress(srcPath);
        try {
            if (degree != 0) bitmap = rotateBitmap(bitmap, degree);
            File file = new File(desPath);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, fos);
            fos.close();
            if (isDelSrc) {
                srcFile.delete();
                srcFile.deleteOnExit();
            }
        } catch (Exception e) {
            Log.e("compressBitmap", e.getMessage() + "");
        }
        return desPath;
    }

    //压缩图片并保存在原路径里
    public final static String compressFile(String filePath, int size) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath,
                options);
        // 压缩图片
        bitmap = compressImage(bitmap,size);
        if (bitmap != null) {
            // 保存图片
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.flush();
                fos.close();
                bitmap.recycle();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    //压缩图片并保存在newpath路径中，返回newpath
    public static String compressFile(String oldPath, String newPath, int size) {
        if (size < 50) size = 50;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(oldPath,
                options);
        // 压缩图片
        bitmap = compressImage(bitmap, size);
        if (bitmap != null) {
            try {
                // 保存图片
                new File(newPath);
                FileOutputStream fos = new FileOutputStream(newPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return oldPath;//压缩失败就返回原图片
            } finally {
                bitmap.recycle();
            }
        }
        return newPath;//压缩成功就返回新图片路径
    }

    private static Bitmap compressImage(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > size) {
            // 重置baos即清空baos
            baos.reset();
            // 每次都减少10
            options -= 10;
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);

        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }


    public final static int getDegress(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 压缩某个输入流中的图片，可以解决网络输入流压缩问题，并得到图片对象
     *
     * @return Bitmap {@link Bitmap}
     */
    public final static Bitmap compressBitmap(InputStream is, int reqsW, int reqsH) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ReadableByteChannel channel = Channels.newChannel(is);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (channel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) baos.write(buffer.get());
                buffer.clear();
            }
            byte[] bts = baos.toByteArray();
            Bitmap bitmap = compressBitmap(bts, reqsW, reqsH);
            is.close();
            channel.close();
            baos.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 压缩指定byte[]图片，并得到压缩后的图像
     *
     * @param bts
     * @param reqsW
     * @param reqsH
     * @return
     */
    public final static Bitmap compressBitmap(byte[] bts, int reqsW, int reqsH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqsW, reqsH);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
    }

    /**
     * 压缩已存在的图片对象，并返回压缩后的图片
     *
     * @param bitmap
     * @param reqsW
     * @param reqsH
     * @return
     */
    public final static Bitmap compressBitmap(Bitmap bitmap, int reqsW, int reqsH) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bts = baos.toByteArray();
            Bitmap res = compressBitmap(bts, reqsW, reqsH);
            baos.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    /**
     * 压缩资源图片，并返回图片对象
     *
     * @param res   {@link Resources}
     * @param resID
     * @param reqsW
     * @param reqsH
     * @return
     */
    public final static Bitmap compressBitmap(Resources res, int resID, int reqsW, int reqsH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resID, options);
        options.inSampleSize = calculateInSampleSize(options, reqsW, reqsH);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resID, options);
    }

    /**
     * 基于质量的压缩算法， 此方法未 解决压缩后图像失真问题 <br>
     * 可先调用比例压缩适当压缩图片后，再调用此方法可解决上述问题
     *
     * @param maxBytes 压缩后的图像最大大小 单位为byte
     * @return
     */
    public final static Bitmap compressBitmap(Bitmap bitmap, long maxBytes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            int options = 90;
            while (baos.toByteArray().length > maxBytes) {
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.PNG, options, baos);
                options -= 10;
            }
            byte[] bts = baos.toByteArray();
            Bitmap bmp = BitmapFactory.decodeByteArray(bts, 0, bts.length);
            baos.close();
            return bmp;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 得到指定路径图片的options
     *
     * @param srcPath
     * @return Options {@link BitmapFactory.Options}
     */
    public final static BitmapFactory.Options getBitmapOptions(String srcPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcPath, options);
        return options;
    }

    /**
     * 回收多张bitmap图片
     *
     * @param vs
     */
    public static void recycleBGBitmap(View... vs) {
        if (vs != null) {
            for (View v : vs) {
                recycleBGBitmap(v);
            }
            vs = null;
        }
    }

    /**
     * 回收单张bitmap图片
     *
     * @param v
     */
    private static void recycleBGBitmap(View v) {
        try {
            if (v != null) {
                v.setBackgroundResource(0);
                BitmapDrawable b = (BitmapDrawable) v.getBackground();
                if (b != null) {
                    if (b.getBitmap() != null) {
                        b.getBitmap().recycle();
                    }
                    b.setCallback(null);
                }
                if (v instanceof ImageView) {
                    ((ImageView) v).setImageBitmap(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据网址获得图片，优先从本地获取，本地没有则从网络下载
     *
     * @param url     图片网址
     * @param context 上下文
     * @return 图片
     */
    public static Bitmap getBitmap(String url, Context context) {
        String imageName = url.substring(url.lastIndexOf("/") + 1, url.length());
        File file = new File(FileUtils.getImageCacheDir(context), imageName);
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getPath());
        }
        return getNetBitmap(url, file, context);
    }

    /**
     * 根据传入的list中保存的图片网址，获取相应的图片列表
     *
     * @param list    保存图片网址的列表
     * @param context 上下文
     * @return 图片列表
     */
    public static List<Bitmap> getBitmap(List<String> list, Context context) {
        List<Bitmap> result = new ArrayList<>();
        for (String strUrl : list) {
            Bitmap bitmap = getBitmap(strUrl, context);
            if (bitmap != null) {
                result.add(bitmap);
            }
        }
        return result;
    }

    /**
     * 网络可用状态下，下载图片并保存在本地
     *
     * @param strUrl  图片网址
     * @param file    本地保存的图片文件
     * @param context 上下文
     * @return 图片
     */
    private static Bitmap getNetBitmap(String strUrl, File file, Context context) {
        Bitmap bitmap = null;
        if (CommonUtils.isConnected(context)) {
            try {
                URL url = new URL(strUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.connect();
                InputStream in = con.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
                FileOutputStream out = new FileOutputStream(file.getPath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */

    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        // 根据旋转角度，生成旋转矩阵
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
}
