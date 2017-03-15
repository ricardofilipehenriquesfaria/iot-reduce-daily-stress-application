package app.miti.com.iot_reduce_daily_stress_application;

import android.annotation.SuppressLint;
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

    public static final String RESPONSE_STRING = "response";

    public JsonParsingService() {
        super(JsonParsingService.class.getName());
    }

    @SuppressLint("LongLogTag")
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

            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            Intent locationIntent = new Intent(this, Algorithm.class);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonData = jsonArray.getJSONObject(i);

                locationIntent.putExtra("ID", jsonData.getInt("id"));
                locationIntent.putExtra("ESTRADA", jsonData.getString("estrada"));
                locationIntent.putExtra("RUA", jsonData.getString("rua"));
                locationIntent.putExtra("DATA_INICIO", jsonData.getString("data_inicio"));
                locationIntent.putExtra("DATA_FIM", jsonData.getString("data_fim"));
                locationIntent.putExtra("HORA_INICIO", jsonData.getString("hora_inicio"));
                locationIntent.putExtra("HORA_FIM", jsonData.getString("hora_fim"));
                locationIntent.putExtra("LATITUDE_INICIO", jsonData.getDouble("latitude_inicio"));
                locationIntent.putExtra("LONGITUDE_INICIO", jsonData.getDouble("longitude_inicio"));
                locationIntent.putExtra("LATITUDE_FIM", jsonData.getDouble("latitude_fim"));
                locationIntent.putExtra("LONGITUDE_FIM", jsonData.getDouble("longitude_fim"));

                Log.i("Output", "id: " + jsonData.getInt("id") +
                        ", estrada: " + jsonData.getString("estrada") +
                        ", rua: " + jsonData.getString("rua") +
                        ", data_inicio: " + jsonData.getString("data_inicio") +
                        ", data_fim: " + jsonData.getString("data_fim") +
                        ", hora_inicio: " + jsonData.getString("hora_inicio") +
                        ", hora_fim: " + jsonData.getString("hora_fim") +
                        ", latitude_inicio: " + jsonData.getDouble("latitude_inicio") +
                        ", longitude_inicio: " + jsonData.getDouble("longitude_inicio") +
                        ", latitude_fim: " + jsonData.getDouble("latitude_fim") +
                        ", longitude_inicio: " + jsonData.getDouble("longitude_fim")
                );
                startService(locationIntent);
            }
        } catch (JSONException e) {
            Log.e("Error parsing data: ", e.toString());
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.JsonBroadcastReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESPONSE_STRING, stringBuilder.toString());
        sendBroadcast(broadcastIntent);
    }
}
