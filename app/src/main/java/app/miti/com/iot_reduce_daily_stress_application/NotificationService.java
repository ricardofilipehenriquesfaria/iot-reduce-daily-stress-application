package app.miti.com.iot_reduce_daily_stress_application;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Ricardo on 06-02-2017.
 */

public class NotificationService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        showNotification(intent.getExtras().getString("TEXT"));
        return flags;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification(String text)
    {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("New notification")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long []{300,100,300,100})
                .addAction(R.mipmap.ic_launcher, "Abrir", pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }
}
