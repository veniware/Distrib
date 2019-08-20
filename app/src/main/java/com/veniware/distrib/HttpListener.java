package com.veniware.distrib;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Formatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.support.v4.app.NotificationCompat;

public class HttpListener extends Service {
    final static int PORT = 1234;

    static Context self;

    static ArrayList<String> grayList = new ArrayList<>();
    static String currentAccess = "";

    public static boolean isListening = false;

    private static Thread listener;
    private static ServerSocket serverSocket;
    private static String startedDate = "";

    public HttpListener() { this.self = this; }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences saved = PreferenceManager.getDefaultSharedPreferences(self);
        currentAccess = saved.getString("com.veniware.distrib.access", "");

        if (grayList.size() == 0 && currentAccess!="") grayList.add(currentAccess);

        start();

        NotificationCompat.Builder builter = new NotificationCompat.Builder(self);
        builter.setOngoing(true);
        builter.setSmallIcon(R.drawable.notification);
        builter.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        CharSequence from = self.getString(R.string.app_name);
        CharSequence message = self.getString(R.string.still_running);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Ui.class), 0);

        Notification notification = builter.build();
        notification.setLatestEventInfo(this, from, message, contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(self.NOTIFICATION_SERVICE);
        manager.notify(6543, notification);

        //this will restart the service, in cast os close the main thread
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        stop();

        NotificationManager manager = (NotificationManager) getSystemService(self.NOTIFICATION_SERVICE);
        manager.cancel(6543);

        super.onDestroy();
    }

    public void start() {
        isListening = true;

        Cache.initCache(this);

        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
        startedDate = format.format(now.getTime());

        listener = new Thread(new Listener());
        listener.setPriority(Thread.MAX_PRIORITY);
        listener.start();
    }

    public void stop() {
        isListening = false;
        listener.interrupt();

        //close server socket
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalIpAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    public static byte[] buildHeader(String  url, int statusCode, String  status, long  contentLength) {
        return buildHeader(url, statusCode, status, contentLength, false);
    }
    public static byte[] buildHeader(String  url, int statusCode, String  status, long  contentLength, boolean forceDownload) {

        StringBuilder header = new StringBuilder();

        //Not Modified
        if (statusCode == 304) {
            header.append("HTTP/1.1 304 Not Modified\n");
            header.append("\n");
            return header.toString().getBytes();
        }

        header.append("HTTP/1.1 " + statusCode + " " + status + "\n");
        header.append("Content-Length: " + contentLength + "\n");
        header.append("Connection: close\n");

        String extension = (url.length() > 2)? url.substring(url.length() - 3, url.length()) : "";

        //content type
        if (forceDownload) {
            header.append("Content-Type: application/octet-stream\n");
            header.append("Last-Modified: " + startedDate + "\n");

        } else
        switch (extension) {
            case "css":
                header.append("Content-Type: text/css\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case ".js":
                header.append("Content-Type: text/javascript\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "png":
                header.append("Content-Type: image/png\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "jpg": case "jpe": case "jpeg":
                header.append("Content-Type: image/jpeg\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "webp":
                header.append("Content-Type: image/webp\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "mp4":
                header.append("Content-Type: video/mp4\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "3gp":
                header.append("Content-Type: video/3gpp\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "avi":
                header.append("Content-Type: video/x-msvideo\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "aac":
                header.append("Content-Type: audio/aac\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "mp3":
                header.append("Content-Type: audio//mpeg\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "mid": case "midi":
                header.append("Content-Type: audio/midi\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "ogg":
                header.append("Content-Type: audio/ogg\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "wav":
                header.append("Content-Type: audio/wav\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            case "vtt":
                header.append("Content-Type: text/vtt\n");
                header.append("Last-Modified: " + startedDate + "\n");
                break;

            default:
                header.append("Content-Type: text/html\n");
                break;
        }

        header.append("\n");

        return header.toString().getBytes();
    }

    public static String urlDecode(String url) {
        String result = url;
        result = result.replace("%0D%0A", "\n");

        result = result.replace("%20", " ");
        result = result.replace("%21", "!");
        result = result.replace("%22", "\"");
        result = result.replace("%23", "#");
        result = result.replace("%24", "$");
        result = result.replace("%25", "%");
        result = result.replace("%26", "&");
        result = result.replace("%27", "'");
        result = result.replace("%28", "(");
        result = result.replace("%29", ")");
        result = result.replace("%2A", "*");
        result = result.replace("%2B", "+");
        result = result.replace("%2C", ",");
        result = result.replace("%2D", "-");
        result = result.replace("%2E", ".");
        result = result.replace("%2F", "/");

        result = result.replace("%3A", ":");
        result = result.replace("%3B", ";");
        result = result.replace("%3C", "<");
        result = result.replace("%3D", "=");
        result = result.replace("%3E", ">");
        result = result.replace("%3F", "?");
        result = result.replace("%40", "@");
        result = result.replace("%5B", "[");
        result = result.replace("%5C", "\\");
        result = result.replace("%5D", "]");
        result = result.replace("%5E", "^");
        result = result.replace("%5F", "_");
        result = result.replace("%60", "`");

        result = result.replace("%7B", "{");
        result = result.replace("%7C", "|");
        result = result.replace("%7D", "}");
        result = result.replace("%7E", "~");

        return result;
    }

    private class Listener extends Thread {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                serverSocket.setPerformancePreferences(0, 32, 0);
            } catch (IOException e) {
                isListening = false;
                return;
            }

            while (isListening) {
                try {
                    Socket socket = serverSocket.accept();

                    Thread client = new Server(socket);
                    client.setPriority(MAX_PRIORITY);
                    client.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Server extends Thread {
        Socket socket;
        short loopCounter = 0;

        public Server(Socket socket) { this.socket = socket; }

        @Override public void run() { httpRequest(); }

        void httpRequest() {
            try {
                InputStream inStream = socket.getInputStream();
                OutputStream outStream = socket.getOutputStream();

                socket.setReceiveBufferSize(4096);
                byte[] buffer = new byte[socket.getReceiveBufferSize()];
                int length = inStream.read(buffer);

                if (length == 0 && loopCounter < 2) {
                    httpRequest();
                    loopCounter++;
                    return;
                } else if (loopCounter >= 2) {
                    socket.close();
                    return;
                }

                byte[] response;
                byte[] header;

                String ip = socket.getInetAddress().getHostAddress();
                if (!grayList.contains(ip)) grayList.add(ip);

                //access
                if (currentAccess.compareTo(ip) != 0) {
                    response = Cache.cache.get("forbidden");
                    header = buildHeader("/", 403, "Forbidden", response.length);

                    byte[] out = new byte[header.length + response.length];
                    System.arraycopy(header, 0, out, 0, header.length);
                    System.arraycopy(response, 0, out, header.length, response.length);

                    outStream.write(out);
                    outStream.close();
                    return;
                }

                String request = new String(buffer, "UTF-8").trim();

                String[] lines     = request.split("\n");
                String[] firstLine = lines[0].split(" ");

                String ifModifiedSince = "";

                String url = (firstLine.length > 1)? firstLine[1] : "";
                if (url.startsWith("/")) url = url.substring(1);

                if (lines.length > 1)
                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i].toLowerCase().trim();
                        if (line.startsWith("if-modified-since:")) ifModifiedSince = line.substring(18).trim();
                    }

                String[] R = url.split("&");

                switch (R[0]) {

                    case "getstatus":
                        WifiInfo wifiInfo = ((WifiManager) self.getSystemService(self.WIFI_SERVICE)).getConnectionInfo();
                        int wLevel=WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);

                        Intent batteryStatus = self.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                        int bLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                        int battery = 100 * bLevel / scale;

                        Calendar now = Calendar.getInstance();
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                        response = (timeFormat.format(now.getTime()) + "|" +
                                                      battery + "|" +
                                                      wLevel + "|").getBytes();

                        header = buildHeader(url, 200, "OK", response.length);
                        break;

                    case "getfiles":
                        if (R.length < 2) {
                            response = new byte[0];
                            header = buildHeader(url, 400, "Bad Request", response.length);
                            break;
                        }
                        response = FileIO.getFiles(urlDecode(R[1]));
                        header = buildHeader(url, 200, "OK", response.length);
                        break;

                    case "delete":
                        if (R.length < 2) {
                            response = new byte[0];
                            header = buildHeader(url, 400, "Bad Request", response.length);
                            break;
                        }
                        FileIO.deleteFile(urlDecode(R[1]));
                        response = FileIO.getFiles(urlDecode(R[1]));
                        header = buildHeader(url, 200, "OK", response.length);
                        break;

                    case "getfile":
                        if (R.length < 2) {
                            response = new byte[0];
                            header = buildHeader(url, 400, "Bad Request", response.length);
                            break;
                        }
                        FileIO.sendFile(urlDecode(R[1]), socket);
                        outStream.close();
                        return;

                    case "getthumbnail":
                        if (R.length < 2) {
                            response = new byte[0];
                            header = buildHeader(url, 400, "Bad Request", response.length);
                            break;
                        }
                        response = FileIO.getThumbnail(urlDecode(R[1]));
                        if (response == null) {
                            response = new byte[0];
                            header = HttpListener.buildHeader(url, 500, "Internal Server Error", 0);
                        } else {
                            header = buildHeader(url, 200, "OK", response.length);
                        }
                        break;

                    case "getvideothumbnail":
                        if (R.length < 2) {
                            response = new byte[0];
                            header = buildHeader(url, 400, "Bad Request", response.length);
                            break;
                        }
                        response = FileIO.getVideoThumbnail(urlDecode(R[1]));
                        if (response == null) {
                            response = new byte[0];
                            header = HttpListener.buildHeader(url, 500, "Internal Server Error", 0);
                        } else {
                            header = buildHeader(url, 200, "OK", response.length);
                        }
                        break;

                    case "download":
                        if (R.length < 2) {
                            response = new byte[0];
                            header = buildHeader(url, 400, "Bad Request", response.length);
                            break;
                        }
                        FileIO.sendFile(urlDecode(R[1]), socket, true);
                        outStream.close();
                        return;

                    case "upload":
                        if (R.length < 2) {
                            response = new byte[0];
                            header = buildHeader(url, 400, "Bad Request", response.length);
                            break;
                        }
                        FileIO.uploadFile(urlDecode(R[1]), socket, buffer);
                        outStream.close();
                        return;

                    case "sendsms":
                        if (R.length < 3) {
                            response = new byte[0];
                            header = buildHeader(url, 400, "Bad Request", response.length);
                            break;
                        }
                        Messaging.sendSms(R[1], urlDecode(R[2]));
                        response = new byte[0];
                        header = buildHeader(url, 200, "OK", response.length);
                        break;

                    case "getchat":
                        response = Messaging.getChat(self);
                        header = buildHeader(url, 200, "OK", response.length);
                        break;

                    default:
                        if (ifModifiedSince.compareTo(startedDate.toLowerCase()) == 0) {
                            response = new byte[0];
                            header = buildHeader(url, 304, "Not Modified", response.length);

                        } else if (Cache.cache.containsKey(R[0])) {
                            response = Cache.cache.get(R[0]);
                            header = buildHeader(url, 200, "OK", response.length);

                        } else {
                            response = new byte[0];
                            header = buildHeader(url, 404, "Not Found", response.length);
                        }
                }

                //send and close
                outStream.write(header);
                outStream.write(response);
                outStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
