package app.miti.com.iot_reduce_daily_stress_application;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ricardo on 10-02-2017.
 */

public class JsonParsingService extends IntentService {

    public JsonParsingService() {
        super("JsonParsingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection httpURLConnection = null;

        try {

            URL url = new URL("http://iotapplication.esy.es");

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

        try {
            JSONArray jArray = new JSONArray(stringBuilder.toString());

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject json_data = jArray.getJSONObject(i);

                Log.i("Output", "id: " + json_data.getInt("id") +
                        ", estrada: " + json_data.getString("estrada") +
                        ", rua: " + json_data.getString("rua") +
                        ", data_inicio: " + json_data.getString("data_inicio") +
                        ", data_fim: " + json_data.getString("data_fim") +
                        ", hora_inicio: " + json_data.getString("hora_inicio") +
                        ", hora_fim: " + json_data.getString("hora_fim") +
                        ", latitude_inicio: " + json_data.getDouble("latitude_inicio") +
                        ", longitude_inicio: " + json_data.getDouble("longitude_inicio") +
                        ", latitude_fim: " + json_data.getDouble("latitude_fim") +
                        ", longitude_inicio: " + json_data.getDouble("longitude_fim")
                );
            }
        } catch (JSONException e) {
            Log.e("Error parsing data: ", e.toString());
        }
    }
}
