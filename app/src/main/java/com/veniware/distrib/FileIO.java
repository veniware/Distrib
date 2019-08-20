package com.veniware.distrib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Hashtable;

/**
 * Created by veni on 10/1/2015.
 */
public class FileIO {

    private static int BUFFER_SIZE = 4096;

    public static byte[] getFiles(String dir) {
        StringBuilder sb = new StringBuilder();

        //photos
        if (dir.compareTo("photos") == 0 || dir.compareTo("Photos") == 0) {
            Hashtable<String, String> ht = new Hashtable();

            sb.append(getPhotos("/storage/emulated/0/DCIM", null, ht));
            sb.append(getPhotos("/storage/emulated/0/Pictures", null, ht));
            sb.append(getPhotos("/storage/emulated/0/Download", null, ht));

            sb.append(getPhotos("/storage/external_SD/DCIM", null, ht));
            sb.append(getPhotos("/storage/external_SD/Pictures", null, ht));
            sb.append(getPhotos("/storage/external_SD/download", null, ht));

            sb.append(getPhotos("/storage/sdcard0/DCIM", null, ht));
            sb.append(getPhotos("/storage/sdcard0/Pictures", null, ht));
            sb.append(getPhotos("/storage/sdcard0/Download", null, ht));

            sb.append(getPhotos("/storage/sdcard1/DCIM", null, ht));
            sb.append(getPhotos("/storage/sdcard1/Pictures", null, ht));
            sb.append(getPhotos("/storage/sdcard1/Download", null, ht));

            sb.append(getPhotos(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString(), null, ht));
            sb.append(getPhotos(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString(), null, ht));
            sb.append(getPhotos(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), null, ht));

            return sb.toString().getBytes();
        }

        //videos
        if (dir.compareTo("videos") == 0 || dir.compareTo("Videos") == 0) {
            Hashtable<String, String> ht = new Hashtable();

            sb.append(getVideos("/storage/emulated/0/DCIM", null, ht));
            sb.append(getVideos("/storage/emulated/0/Movies", null, ht));
            sb.append(getVideos("/storage/emulated/0/video", null, ht));
            sb.append(getVideos("/storage/emulated/0/videos", null, ht));
            sb.append(getVideos("/storage/emulated/0/Download", null, ht));

            sb.append(getVideos("/storage/external_SD/DCIM", null, ht));
            sb.append(getVideos("/storage/external_SD/video", null, ht));
            sb.append(getVideos("/storage/external_SD/videos", null, ht));
            sb.append(getVideos("/storage/external_SD/download", null, ht));

            sb.append(getVideos("/storage/sdcard1/DCIM", null, ht));
            sb.append(getVideos("/storage/sdcard1/video", null, ht));
            sb.append(getVideos("/storage/sdcard1/videos", null, ht));
            sb.append(getVideos("/storage/sdcard1/download", null, ht));

            sb.append(getVideos(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString(), null, ht));
            sb.append(getVideos(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString(), null, ht));
            sb.append(getVideos(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), null, ht));

            return sb.toString().getBytes();
        }

        File root;
        if (dir == null) root = android.os.Environment.getExternalStorageDirectory();
        else root = new File(dir);

        File[] files = root.listFiles();
        if (files == null) return sb.toString().getBytes();

        SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

        for (int i = 0; i < files.length; i++)
            if (files[i].isDirectory()) {
                sb.append("true|");
                sb.append(files[i].getName() + "|");
                sb.append(files[i].getAbsolutePath() + "|");
                sb.append("|");
                sb.append(files[i].canWrite() + "|");

               if (new SimpleDateFormat("yyyy").format(files[i].lastModified()).compareTo("1970") == 0)
                   sb.append("|");
               else
                   sb.append(lastModifiedFormat.format(files[i].lastModified()) + "|");
            }

        for (int i = 0; i < files.length; i++)
            if (files[i].isFile()) {
                sb.append("false|");
                sb.append(files[i].getName() +  "|");
                sb.append(files[i].getAbsolutePath() + "|");
                sb.append(sizeToString(files[i].length()) + "|");
                sb.append(files[i].canWrite() + "|");

                if (new SimpleDateFormat("yyyy").format(files[i].lastModified()).compareTo("1970") == 0)
                    sb.append("|");
                else
                    sb.append(lastModifiedFormat.format(files[i].lastModified()) + "|");
            }

        return sb.toString().getBytes();
    }

    public static String getPhotos(String dir, StringBuilder psb, Hashtable ht) {
        StringBuilder sb = (psb==null)? new StringBuilder() : psb;

        File root = new File(dir);
        File[] files = root.listFiles();
        if (files == null) return sb.toString();

        SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

        for (int i = 0; i < files.length; i++)
            if (files[i].isFile()) {

                String key = files[i].getName() + files[i].length();
                if (ht.containsKey(key)) continue;

                if (files[i].getAbsolutePath().contains(".thumbnails")) continue;

                int e = files[i].getName().lastIndexOf('.');
                String extension = (e > 0) ? files[i].getName().substring(e+1).toLowerCase() : "";

                if (extension.compareTo("png") != 0 &&
                    extension.compareTo("jpg") != 0 &&
                    extension.compareTo("jpe") != 0 &&
                    extension.compareTo("jpeg") != 0 &&
                    extension.compareTo("webp") != 0 ) continue;

                sb.append("false|");
                sb.append(files[i].getName() +  "|");
                sb.append(files[i].getAbsolutePath() + "|");
                sb.append(sizeToString(files[i].length()) + "|");
                sb.append(files[i].canWrite() + "|");

                if (new SimpleDateFormat("yyyy").format(files[i].lastModified()).compareTo("1970") == 0)
                    sb.append("|");
                else
                    sb.append(lastModifiedFormat.format(files[i].lastModified()) + "|");

                ht.put(key, key);
            } else {
                getPhotos(files[i].getAbsolutePath(), sb, ht);
            }

        return sb.toString();
    }

    public static String getVideos(String dir, StringBuilder psb, Hashtable ht) {
        StringBuilder sb = (psb==null)? new StringBuilder() : psb;

        File root = new File(dir);
        File[] files = root.listFiles();
        if (files == null) return sb.toString();

        SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

        for (int i = 0; i < files.length; i++)
            if (files[i].isFile()) {

                String key = files[i].getName() + files[i].length();
                if (ht.containsKey(key)) continue;

                int e = files[i].getName().lastIndexOf('.');
                String extension = (e > 0) ? files[i].getName().substring(e+1).toLowerCase() : "";

                if (extension.compareTo("mp4") != 0 &&
                    extension.compareTo("avi") != 0 &&
                    extension.compareTo("3gp") != 0) continue;

                sb.append("false|");
                sb.append(files[i].getName() +  "|");
                sb.append(files[i].getAbsolutePath() + "|");
                sb.append(sizeToString(files[i].length()) + "|");
                sb.append(files[i].canWrite() + "|");

                if (new SimpleDateFormat("yyyy").format(files[i].lastModified()).compareTo("1970") == 0)
                    sb.append("|");
                else
                    sb.append(lastModifiedFormat.format(files[i].lastModified()) + "|");

                ht.put(key, key);

            } else {
                getVideos(files[i].getAbsolutePath(), sb, ht);
            }

        return sb.toString();
    }

    public static byte[] getThumbnail(String filename) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inTempStorage = new byte[4 * 1024];
            options.inJustDecodeBounds = false;
            options.inSampleSize = 16;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filename, options), 96, 96);

            if (thumbnail != null)
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 33, stream);
            else
                return null;

            thumbnail.recycle();

            return stream.toByteArray();
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public static byte[] getVideoThumbnail(String filename) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(filename, 0);

            if (thumbnail != null)
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 33, stream);
            else
                return null;

            thumbnail.recycle();

            return stream.toByteArray();
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public static boolean deleteFile(String filename) {
        if (!filename.startsWith("/")) return false;

        File file = new File(filename);

        if (file.canWrite()) {
            file.delete();
        } else {
            return false;
        }

        return true;
    }

    public static void sendFile(String filename, Socket socket) {
        sendFile(filename, socket, false);
    }
    public static void sendFile(String filename, Socket socket, boolean forceDownload) {
        File file = new File(filename);

        try {
            if (!file.canRead()) socket.getOutputStream().write(HttpListener.buildHeader(filename, 500, "Internal Server Error", 0));

            socket.getOutputStream().write(HttpListener.buildHeader(filename, 200, "OK", file.length(), forceDownload));

            RandomAccessFile raf = new RandomAccessFile(file, "r");

            byte[] buffer = new byte[BUFFER_SIZE];
            int count;

            while ((count = raf.read(buffer)) > 0)
                socket.getOutputStream().write(buffer, 0, count);

            socket.close();
            raf.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void uploadFile(String filename, Socket socket, byte[] initBuffer) {
        File file = new File(HttpListener.urlDecode(filename));

        try {
            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();

            if (!filename.startsWith("/")) { //invalid path
                outStream.write(HttpListener.buildHeader(filename, 500, "Internal Server Error", 0));
                socket.close();
                return;
            }

            if (file.exists()) { //file already exist
                outStream.write(HttpListener.buildHeader(filename, 500, "Internal Server Error", 0));
                socket.close();
                return;
            }

            FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile());

            if (!file.canWrite()) { //can't write, mostly cos KitKat sucks multiple dicks
                outStream.write(HttpListener.buildHeader(filename, 500, "Internal Server Error", 0));
                socket.close();
                return;
            }

            outStream.write(HttpListener.buildHeader("upload", 200, "OK", 0));

            int initFrom = 0;
            int initTo = initBuffer.length - 1;

            //find initBuffer's 1st header
            for (int i = 3; i < initBuffer.length; i++)
                if (initBuffer[i-3] == 13 &&
                    initBuffer[i-2] == 10 &&
                    initBuffer[i-1] == 13 &&
                    initBuffer[i] == 10) { //find \n\r\n\r
                    initFrom = i + 1;
                    break;
                }

            //find initBuffer's 2st header(if exist)
            if (initBuffer[initFrom] == 45 && initBuffer[initFrom+1] == 45) //find --
                for (int i = initFrom; i < initBuffer.length-5; i++)
                    if (initBuffer[i] == 13 &&
                        initBuffer[i+1] == 10 &&
                        initBuffer[i+2] == 13 &&
                        initBuffer[i+3] == 10) { //find \n\r\n\r
                        initFrom = i + 4;
                        break;
                    }

            //find initBuffer's end
            for (int i = initBuffer.length - 1; i > 0; i--)
                if (initBuffer[i] != 0) {
                    if (i - 1 < initFrom) initTo = i;
                    else initTo = i - 1;
                    break;
                }

            //if (initFrom > initTo) no body, only headers
            if (initFrom < initTo) {
                fos.write(initBuffer, initFrom, initTo - initFrom);
            }


            byte[] buffer = new byte[BUFFER_SIZE];
            int from, to;

            while ((to = inStream.read(buffer)) > 0) {
                from = 0;

                //find and remove header
                if (buffer[from] == 45 && buffer[from+1] == 45) //find --
                    for (int i = from; i < buffer.length - 5; i++)
                        if (buffer[i] == 13 &&
                                buffer[i+1] == 10 &&
                                buffer[i+2] == 13 &&
                                buffer[i+3] == 10) { //find \n\r\n\r
                            from = i + 4;
                            break;
                        }

                //find and remove footer
                if (to > 64)
                for (int i = to - 64; i < to-3; i++)
                    if (buffer[i] == 13 &&
                        buffer[i+1] == 10 &&
                        buffer[i+2] == 45 &&
                        buffer[i+3] == 45) {
                        to = i;
                        break;
                    }

                fos.write(buffer, from, to - from);
            }

            fos.close();
            socket.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String sizeToString(long size) {
        if (size < 1024) return round(size, 2) + " B";

        double kb = size / 1024;
        if (kb < 1024) return round(kb, 2) + " KB";

        double mb = kb / 1024;
        if (mb < 1024) return round(mb, 2) + " MB";

        double gb = mb / 1024;
        if (gb < 1024) return round(gb, 2) + " GB";

        double tb = gb / 1024;
        return round(tb, 2) + " TB";
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
