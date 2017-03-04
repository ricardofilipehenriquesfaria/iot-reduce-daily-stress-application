package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by Ricardo on 10-02-2017.
 */

class ParseURL {

    private String URL = "http://toxic.pt/prociv/pt/informacao-a-populacao/32-noticias/estradas/38-estradas-encerradas-17-06-2016.html";

    private String[] string = new String[100];

    ParseURL(final Context context){

        final String activity = DbHelper.retrieveActivityRecognitionData(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Document document = Jsoup.parse(response);

                int i = 0;

                for( Element element : document.select("div[itemprop*=\"articleBody\"]").select("p:has(a[href$=.pdf])" )) {
                    element.select("a[href$=.pdf]").remove();
                    string[i] = element.text();
                    i++;
                }
                if(activity.equals("in_vehicle")) {
                    TextSpeech.TextToSpeech(context, string[0]);
                }
                else {
                    Intent service = new Intent(context, NotificationService.class);
                    service.putExtra("TEXT", string[0]);
                    service.putExtra("TITLE", "Estrada Encerrada");
                    context.startService(service);
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}
