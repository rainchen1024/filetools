package com.rainchen.filetools.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;

/**
 * Author: xzp
 * Data: 2016/11/10
 * 文件处理工具类
 */

public class FileUtils {
    public static final String TAG = FileUtils.class.getSimpleName();
    public static final String DIR_NAME = "ejingwu";
    public static final String CACHE = File.separator + DIR_NAME + File.separator;

    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        } else {
            int filePosi = filePath.lastIndexOf(File.separator);
            return filePosi == -1 ? filePath : filePath.substring(filePosi + 1);
        }
    }

    /**
     * 读取文件为字符串
     *
     * @param filePath    文件路径
     * @param charsetName 编码格式
     * @return
     */
    public static StringBuilder readFile(String filePath, String charsetName) {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file != null && file.isFile()) {
            BufferedReader reader = null;

            StringBuilder string;
            try {
                InputStreamReader e = new InputStreamReader(new FileInputStream(file), charsetName);
                reader = new BufferedReader(e);

                for (String line = null; (line = reader.readLine()) != null; fileContent.append(line)) {
                    if (!fileContent.toString().equals("")) {
                        fileContent.append("\r\n");
                    }
                }

                reader.close();
                string = fileContent;
            } catch (IOException var15) {
                throw new RuntimeException("IOException occurred. ", var15);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException var14) {
                        throw new RuntimeException("IOException occurred. ", var14);
                    }
                }

            }

            return string;
        } else {
            return null;
        }
    }

    /**
     * 将文件转换为流
     *
     * @param filePath
     * @return
     */

    public static boolean writeFile(String filePath, String content, boolean append) {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException var12) {
            throw new RuntimeException("IOException occurred. ", var12);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException var11) {
                    throw new RuntimeException("IOException occurred. ", var11);
                }
            }

        }

        return true;
    }

    /**
     * 创建文件夹
     *
     * @param DirName
     * @return
     */
    public static boolean makeDirs(String DirName) {
        if (TextUtils.isEmpty(DirName)) {
            return false;
        } else {
            File file = new File(DirName);
            return file.exists() && file.isDirectory() ? true : file.mkdirs();
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        } else {
            File file = new File(filePath);
            if (!file.isFile() || !file.exists()) {
                return false;
            }
            return true;
        }
    }

    /**
     * 判断文件夹是否存在
     *
     * @param directoryPath
     * @return
     */
    public static boolean isFolderExist(String directoryPath) {
        if (TextUtils.isEmpty(directoryPath)) {
            return false;
        } else {
            File dire = new File(directoryPath);
            return dire.exists() && dire.isDirectory();
        }
    }


    /**
     * 删除缓存文件
     *
     * @param path
     */
    public static void deleteCacheFile(String path) {
        if (path != null) {
            File f = new File(path);
            if (f.isFile() && f.exists()) {
                f.delete();
                f.deleteOnExit();
            }
        }
    }

    /**
     * 获取文件大小
     *
     * @param path
     * @return
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1L;
        } else {
            File file = new File(path);
            return file.exists() && file.isFile() ? file.length() : -1L;
        }
    }

    /**
     * 从资源文件夹读取文件为字符串
     *
     * @param context
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String getFileFromAssets(Context context, String fileName) throws IOException {
        if (context != null && !TextUtils.isEmpty(fileName)) {
            StringBuilder s = new StringBuilder("");
            InputStreamReader in = null;
            BufferedReader br = null;

            try {
                in = new InputStreamReader(context.getResources().getAssets().open(fileName));
                br = new BufferedReader(in);

                String line;
                while ((line = br.readLine()) != null) {
                    s.append(line);
                }

                String str = s.toString();
                return str;
            } finally {
                if (in != null) {
                    in.close();
                }

                if (br != null) {
                    br.close();
                }

            }
        } else {
            return null;
        }
    }

    /**
     * 从Raw文件夹读取文件为字符串
     *
     * @param context
     * @param resId
     * @return
     * @throws IOException
     */
    public static String getFileFromRaw(Context context, int resId) throws IOException {
        if (context == null) {
            return null;
        } else {
            StringBuilder s = new StringBuilder();
            InputStreamReader in = null;
            BufferedReader br = null;

            try {
                in = new InputStreamReader(context.getResources().openRawResource(resId));
                br = new BufferedReader(in);

                String line;
                while ((line = br.readLine()) != null) {
                    s.append(line);
                }

                String str = s.toString();
                return str;
            } finally {
                if (in != null) {
                    in.close();
                }

                if (br != null) {
                    br.close();
                }

            }
        }
    }

    //文件大小转换器
    public static String FormatFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileSize < 1024) {
            fileSizeString = df.format((double) fileSize) + "B";
        } else if (fileSize < 1048576) {
            fileSizeString = df.format((double) fileSize / 1024) + "K";
        } else if (fileSize < 1073741824) {
            fileSizeString = df.format((double) fileSize / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileSize / 1073741824) + "G";
        }
        return fileSizeString;
    }


    /**
     * 更新apk的缓存文件夹
     *
     * @param context
     * @return
     */
    public static String getUpdateCacheDir(Context context) {
        String dir = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = Environment.getExternalStorageDirectory().getPath();
        } else {
            dir = context.getFilesDir().toString();
        }
        dir += CACHE + "ejingwu.update/";
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        return dir;
    }

    /**
     * 图片、相片的缓存文件夹
     *
     * @param context
     * @return
     */
    public static String getImageCacheDir(Context context) {
        String dir = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = Environment.getExternalStorageDirectory().getPath();
        } else {
            dir = context.getFilesDir().toString();
        }
        dir += CACHE + "ejingwu.images/";
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        return dir;
    }

    /**
     * 音频文件的缓存文件夹
     *
     * @param context
     * @return
     */
    public static String getAudioCacheDir(Context context) {
        String dir = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = Environment.getExternalStorageDirectory().getPath();
        } else {
            dir = context.getFilesDir().toString();
        }
        dir += CACHE + "ejingwu.audios/";
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        return dir;
    }

    /**
     * 视频文件的缓存文件夹
     *
     * @param context
     * @return
     */
    public static String getVideoCacheDir(Context context) {
        String dir = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = Environment.getExternalStorageDirectory().getPath();
        } else {
            dir = context.getFilesDir().toString();
        }
        dir += CACHE + "ejingwu.videos/";
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        return dir;
    }

    /**
     * 缓存目录文件夹
     *
     * @param context
     * @return
     */
    public static String getCacheDir(Context context) {
        String dir = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = Environment.getExternalStorageDirectory().getPath();
        } else {
            dir = context.getFilesDir().toString();
        }
        dir += CACHE;
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        return dir;
    }

    /**
     * 清理缓存文件
     *
     * @param context
     */
    public static void clearCacheFiles(Context context) {
        String dir = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = Environment.getExternalStorageDirectory().getPath();
        } else {
            dir = context.getFilesDir().toString();
        }

        final File f = new File(dir + File.separator + DIR_NAME);
        if (f.exists()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteFile(f);
                }
            }).start();
        }
    }

    public static void clearLocalFiles(Context context) {
        String dir = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = Environment.getExternalStorageDirectory().getPath();
        } else {
            dir = context.getFilesDir().toString();
        }

        final File f = new File(dir + File.separator + DIR_NAME);
        if (f.exists()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteFile(f);
                }
            }).start();
        }

        final File f1 = new File(dir + CACHE + "trace/");
        if (f1.exists()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteFile(f1);
                }
            }).start();
        }
    }

    /**
     * 删除文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            file.delete();
        }
    }

    /*
    * 获取网络文件大小
    * */
    public static long getFileSizeByUrl(String urlString) {
        long lenght = 0;
        String url = null;
        HttpURLConnection conn = null;
        try {
            url = URLEncoder.encode(urlString, "UTF-8");
            //URL mUrl =  new URL(urlString);
            URL mUrl = new URL(url);
             conn = (HttpURLConnection) mUrl.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("Referer", url);
            //conn.setRequestProperty("Referer", urlString);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();

            int responseCode = conn.getResponseCode();
            // 判断请求是否成功处理
            if (responseCode == HttpStatus.SC_OK) {
                //transfer byte to kb
                lenght = conn.getContentLength()/1024;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (conn!=null) {
                conn.disconnect();
            }
        }

        return lenght;
    }
}
