package app.miti.com.iot_reduce_daily_stress_application;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.aware.plugin.google.activity_recognition.UserActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;

/**
 * Created by ricar on 28/09/2017.
 */

public class FirebaseService extends FirebaseMessagingService implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private String text;

    @Override
    public void onCreate(){
        super.onCreate();
        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setSpeechRate((float) 1);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1410, intent, PendingIntent.FLAG_ONE_SHOT);

        int bitmap;
        if(remoteMessage.getNotification().getIcon().equals("ic_closed")){
            bitmap = R.mipmap.ic_closed;
        } else {
            bitmap = R.mipmap.ic_launcher;
        }

        text = remoteMessage.getNotification().getBody();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(bitmap)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1410, notificationBuilder.build());
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.getDefault());
            if(UserActivity.getActivityName().equals("in_vehicle")){
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        else textToSpeech = null;
    }
}
