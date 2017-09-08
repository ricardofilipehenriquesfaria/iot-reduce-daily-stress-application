package app.miti.com.routes;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static android.content.ContentValues.TAG;

/**
 * Created by Ricardo on 08-09-2017.
 */

public class RoutesService extends IntentService {

    /*
        Construtor
    */
    public RoutesService() {
        super(RoutesService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        assert intent != null;
        String request_url = intent.getStringExtra("ROUTES");

        URLConnection urlConnection;
        InputStream inputStream = null;
        JSONObject jsonResponse = null;

        try {
            URL url = new URL(request_url);

            urlConnection = url.openConnection();

            inputStream = urlConnection.getInputStream();
            String string = IOUtils.toString( urlConnection.getInputStream(), "utf-8");

            jsonResponse = new JSONObject(string.replace("renderAdvancedNarrative(", "").replace(")", ""));

        }catch (MalformedURLException e){
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            try{
                assert inputStream != null;
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        sendBroadcast(jsonResponse);
    }

    private void sendBroadcast (JSONObject jsonResponse){
        Intent intent = new Intent ("ROUTE");
        intent.putExtra("JSONRESPONSE", jsonResponse.toString());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
