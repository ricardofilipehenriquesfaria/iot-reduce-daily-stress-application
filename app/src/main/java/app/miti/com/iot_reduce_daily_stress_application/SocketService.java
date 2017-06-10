package app.miti.com.iot_reduce_daily_stress_application;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;

/**
 * Created by Ricardo on 10-06-2017.
 */

public class SocketService extends Service implements IOCallback {

    private SocketIO mSocket;
    private static final String TAG = "SocketService";
    public static final String SERVER_IP = "http://84.23.192.131";
    public static final int SERVER_PORT = 3000;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        mSocket = new SocketIO();

        try {
            mSocket.connect(SERVER_IP + ":" + SERVER_PORT, this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mSocket.send("Hello Server");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {
        try {
            System.out.println(TAG + " - Server said: " + jsonObject.toString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String data, IOAcknowledge ioAcknowledge) {
        System.out.println(TAG + " - Server said: " + data);
    }

    @Override
    public void onDisconnect() {
        System.out.println(TAG + " - Connection terminated.");
    }

    @Override
    public void onConnect() {
        System.out.println(TAG + " - Connection established");
    }

    @Override
    public void on(String event, IOAcknowledge ioAcknowledge, Object... args) {
        System.out.println(TAG + " - Server triggered event '" + event + "'");
    }

    @Override
    public void onError(io.socket.SocketIOException socketIOException) {
        System.out.println(TAG + " - An Error occured");
        socketIOException.printStackTrace();
    }
}
