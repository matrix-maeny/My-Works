package com.matrix_maeny.myworks.receivers;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.matrix_maeny.myworks.NotificationSender;
import com.matrix_maeny.myworks.R;
import com.matrix_maeny.myworks.databases.WorkDataBaseHelper;

import java.util.Calendar;

public class WorkReceiver extends BroadcastReceiver {

    Context context;
    AlarmManager alarmManager;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        int id = intent.getIntExtra("id", -1);
        String name = intent.getStringExtra("name");
        String fn = intent.getStringExtra("fn");
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(context.getApplicationContext(), uri);

        if (fn == null) {
            sendNotification(name, id);
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(3000);
                ringtone.play();
            }
            ;

            Intent resetIntent = new Intent(context.getApplicationContext(), WorkReceiver.class);
            resetIntent.putExtra("name", name);
            resetIntent.putExtra("id", id);
            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent resetPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, getTime(), resetPendingIntent);
        }

//        if(fn!=null && fn.equals("no")){
//
//        }

        if (fn != null && fn.equals("yes")) {
            WorkDataBaseHelper db = new WorkDataBaseHelper(context.getApplicationContext());

            if (db.updateData(name, 1)) {
                Toast.makeText(context, "Task completed", Toast.LENGTH_SHORT).show();
            }
            db.updateNotification("");
            db.close();

            Intent cancelIntent = new Intent(context.getApplicationContext(), WorkReceiver.class);
            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(cancelPendingIntent);
        }


    }

    private int notificationTime(){
        WorkDataBaseHelper dataBaseHelper = new WorkDataBaseHelper(context.getApplicationContext());
        Cursor cursor = dataBaseHelper.getNotificationData();

        try {
            cursor.moveToNext();
            return cursor.getInt(2);
        }catch (Exception e){
            return -1;
        }
    }

    private void updateNotification(String name) {

        WorkDataBaseHelper dataBaseHelper = new WorkDataBaseHelper(context.getApplicationContext());

        if (!dataBaseHelper.updateNotification(name)) {
            Toast.makeText(context, "Some error occurred: WorkAdapter: 207", Toast.LENGTH_SHORT).show();
        }
        dataBaseHelper.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendNotification(String name, int id) {

        if (id == -1) {
            id = 0;
        }

        Intent intent = new Intent(context.getApplicationContext(), WorkReceiver.class);
        intent.putExtra("fn", "yes");// fn = from notification
        intent.putExtra("name", name);
        intent.putExtra("id", id);

        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id + 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification notification = new NotificationCompat.Builder(context.getApplicationContext(), NotificationSender.CHANNEL_ID)
                .setSmallIcon(R.drawable.test)
                .setContentTitle(name)
                .setContentIntent(pendingIntent)
                .setContentText("Pending work: Click to complete")
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 0, 2000})
                .setCategory(Notification.CATEGORY_MESSAGE)
                .build();


        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.notify(id, notification);
//        handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (isNotificationBooked && tempModel.getState() == 0) {
//                    notificationCompat.notify(0, notification);
//                    handler.postDelayed(this, 300000);
//                } else {
//                    isNotificationBooked = false;
//                    notificationCompat.cancel(0);
//                }
//            }
//        }, 300000);


    }

    private long getTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int time = notificationTime();

        if(time == -1){
           time = 10;
        }
        int minute = calendar.get(Calendar.MINUTE) + time;

        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        long tempTime = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));

        if (System.currentTimeMillis() > tempTime) {

            if (Calendar.AM_PM == 0) {
                tempTime = tempTime + (1000 * 60 * 60 * 12);
            } else {
                tempTime = tempTime + (1000 * 60 * 60 * 24);

            }

        }

        return tempTime;
    }

}
