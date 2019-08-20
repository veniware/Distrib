package com.veniware.distrib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Created by veni on 9/28/2015.
 */
public class Cache {
    public static Hashtable<String, byte[]> cache = new Hashtable<>();

    public static void add(String name, byte[] content) {
        if (!cache.containsKey(name)) cache.put(name, content);
    }

    public static  byte[] drawableToBytes(Drawable drawable, int width, int height) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        bitmap.recycle();

        return stream.toByteArray();
    }

    public static void initCache(Context context) {
        Cache.add("", context.getString(R.string.index_html).getBytes());
        Cache.add("files", context.getString(R.string.files_html).getBytes());
        Cache.add("messaging", context.getString(R.string.messaging_html).getBytes());
        Cache.add("forbidden", context.getString(R.string.forbidden_html).getBytes());

        Cache.add("global.css", context.getString(R.string.global_css).getBytes());
        Cache.add("window.css", context.getString(R.string.window_css).getBytes());

        Cache.add("windows.js", context.getString(R.string.windows_js).getBytes());
        Cache.add("desktop.js", context.getString(R.string.desktop_js).getBytes());
        Cache.add("files.js", context.getString(R.string.files_js).getBytes());
        Cache.add("messaging.js", context.getString(R.string.messaging_js).getBytes());

        Cache.add("sd.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.sd), 96, 96));
        Cache.add("phone.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.phone), 96, 96));
        Cache.add("folder.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.folder), 96, 96));
        Cache.add("file.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.file), 96, 96));
        Cache.add("photos.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.photos), 96, 96));
        Cache.add("audio.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.audio), 96, 96));
        Cache.add("video.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.video), 96, 96));
        Cache.add("message.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.message), 96, 96));
        Cache.add("favicon.png", Cache.drawableToBytes(context.getResources().getDrawable(R.drawable.favicon), 24, 24));
    }

}
