package com.veniware.distrib;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Ui extends AppCompatActivity {
    public static Ui self;

    public static RelativeLayout container;
    public static LinearLayout btnGo;
    public static LinearLayout btnGlow;
    public static TextView lblIp;
    public static ImageView btnAccess;

    public boolean isBusy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.self = this;

        isBusy = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //always keep screen on, for high thread priority
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        container = (RelativeLayout) findViewById(R.id.container);
        btnGo = (LinearLayout) findViewById(R.id.btnGo);
        btnGlow = (LinearLayout) findViewById(R.id.btnGlow);
        lblIp = (TextView) findViewById(R.id.lblIp);
        btnAccess = (ImageView) findViewById(R.id.btnAccess);

        if (isServiceRunning(HttpListener.class)) {
            btnGlow.setVisibility(View.VISIBLE);
            lblIp.setVisibility(View.VISIBLE);
        }

        lblIp.setText(HttpListener.getLocalIpAddress(this) + ":" + HttpListener.PORT);

        btnGo.setOnClickListener(new BtnGo_onclick());
        btnGlow.setOnClickListener(new BtnGo_onclick());
        btnAccess.setOnClickListener(new BtnAccess_onclick());
    }

    private void showToast(String message){
        final TextView lblToast = (TextView) findViewById(R.id.lblToast);
        lblToast.setText(message);

        AlphaAnimation alphaIn = new AlphaAnimation(0f, 1f);
        alphaIn.setDuration(400);
        alphaIn.setStartOffset(1000);
        alphaIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                AlphaAnimation alphaOut = new AlphaAnimation(1f, 0f);
                alphaOut.setDuration(400);
                alphaOut.setStartOffset(5000);
                alphaOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        lblToast.setVisibility(View.GONE);
                    }
                });

                lblToast.startAnimation(alphaOut);
            }
        });

        lblToast.startAnimation(alphaIn);
        lblToast.setVisibility(View.VISIBLE);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;

        return false;
    }


    private class BtnGo_onclick implements View.OnClickListener {
        @Override public void onClick(View v) {
            ScaleAnimation scale =  new ScaleAnimation(.95f, 1f, .95f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(200);
            btnGo.startAnimation(scale);

            if (isBusy) return;

            boolean running = isServiceRunning(HttpListener.class);

            if (!running) {
                isBusy = true;

                AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
                alpha.setDuration(1500);
                alpha.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) { }
                    @Override public void onAnimationRepeat(Animation animation) { }
                    @Override public void onAnimationEnd(Animation animation) {
                        isBusy = false;

                        if (!isServiceRunning(HttpListener.class) && lblIp.getText() != "No internet access") {
                            btnGlow.setBackgroundResource(R.drawable.red_glow);
                            lblIp.setTextColor(Color.rgb(255, 0, 0));
                            lblIp.setText(getString(R.string.fail_to_start));
                        }
                    }
                });

                String ip = HttpListener.getLocalIpAddress(Ui.self);

                if (ip.compareTo("0.0.0.0") == 0) {
                    btnGlow.setBackgroundResource(R.drawable.red_glow);
                    lblIp.setTextColor(Color.rgb(255, 0, 0));
                    lblIp.setText(getString(R.string.no_internet_access));
                } else {
                    btnGlow.setBackgroundResource(R.drawable.green_glow);
                    lblIp.setTextColor(Color.rgb(0, 255, 0));
                    lblIp.setText(ip + ":" + HttpListener.PORT);

                    startService(new Intent(Ui.self, HttpListener.class));
                    showToast(getString(R.string.after_toast));
                }

                btnGlow.startAnimation(alpha);
                lblIp.startAnimation(alpha);
                btnGlow.setVisibility(View.VISIBLE);
                lblIp.setVisibility(View.VISIBLE);

            } else {
                isBusy = true;

                AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
                alpha.setDuration(1500);
                alpha.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) { }
                    @Override public void onAnimationRepeat(Animation animation) { }
                    @Override public void onAnimationEnd(Animation animation) {
                        btnGlow.setVisibility(View.GONE);
                        lblIp.setVisibility(View.GONE);
                        isBusy = false;
                    }
                });

                btnGlow.startAnimation(alpha);
                lblIp.startAnimation(alpha);

                stopService(new Intent(Ui.self, HttpListener.class));
            }

        }
    }

    private class BtnAccess_onclick implements View.OnClickListener {
        @Override public void onClick(View v) {
            ScaleAnimation scale =  new ScaleAnimation(.9f, 1f, .9f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(200);
            btnAccess.startAnimation(scale);

            if (HttpListener.grayList.size() == 0) return;

            final String array[] = new String[HttpListener.grayList.size()];
            for (int i = 0; i < array.length; i++) array[i] = HttpListener.grayList.get(i);

            AlertDialog.Builder builder = new AlertDialog.Builder(self);
            builder.setTitle(R.string.access_title);

            builder.setItems(array, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    HttpListener.currentAccess = array[which];

                    SharedPreferences saved = PreferenceManager.getDefaultSharedPreferences(self);
                    SharedPreferences.Editor editor = saved.edit();
                    editor.putString("com.veniware.distrib.access", array[which]);
                    editor.commit();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
