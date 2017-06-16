package app.miti.com.iot_reduce_daily_stress_application;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

/**
 * Created by Ricardo on 10-06-2017.
 */

public class SocketService extends Service {

    private static final String TAG = "SocketService";
    public static final String SERVER_IP = "http://84.23.192.131";
    public static final int SERVER_PORT = 3001;

    private boolean isRunning;
    private Thread backgroundThread;

    @Override
    public void onCreate(){
        super.onCreate();
        this.isRunning = false;
        this.backgroundThread = new Thread(backgroundRunnable);
    }

    private Runnable backgroundRunnable = new Runnable(){

        @Override
        public void run() {
            try {

                SocketIO mSocket = new SocketIO(SERVER_IP + ":" + SERVER_PORT);
                mSocket.connect(new IOCallback() {

                    @Override
                    public void on(String event, IOAcknowledge ioAcknowledge, Object... args) {

                        switch(event){
                            case "write":
                                Log.d(TAG, event + " = " + args[0]);
                                break;
                            case "update":
                                Log.d(TAG, event + " = " + args[0]);
                                break;
                            case "delete":
                                Log.d(TAG, event + " = " + args[0]);
                                break;
                            default:
                                Log.d(TAG, event + " = " + args[0]);
                                break;
                        }
                    }

                    @Override
                    public void onMessage(JSONObject json, IOAcknowledge ioAcknowledge) {
                        Log.d(TAG, json.toString());
                    }

                    @Override
                    public void onMessage(String data, IOAcknowledge ioAcknowledge) {
                        Log.d(TAG, data);
                    }

                    @Override
                    public void onError(SocketIOException socketIOException) {
                        Log.d(TAG, "An Error occured");
                        socketIOException.printStackTrace();
                    }

                    @Override
                    public void onDisconnect() {
                        Log.d(TAG, "Disconnected");
                    }

                    @Override
                    public void onConnect() {
                        Log.d(TAG, "Connected");
                    }
                });

                mSocket.send("Hello Server");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDestroy() {
        this.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(!this.isRunning){
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
