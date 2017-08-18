package app.miti.com.roads_width;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ricardo on 03-08-2017.
 */

public class RoadsWidthParsingService extends IntentService{

    public RoadsWidthParsingService() {
        super(RoadsWidthParsingService.class.getName());
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        Double latitude = intent.getDoubleExtra("LATITUDE", 0);
        Double longitude = intent.getDoubleExtra("LONGITUDE", 0);

        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL("http://172.104.130.173:8000/?latitude=" + String.valueOf(latitude) + "&longitude=" + String.valueOf(longitude));

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");

            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

        } catch (IOException e) {
            Log.e("Error in HTTP connection: ", e.toString());
        } finally {
            assert httpURLConnection != null;
            httpURLConnection.disconnect();
        }
    }
}

