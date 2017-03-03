package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by Ricardo on 10-02-2017.
 */

class ParseURL {

    String URL = "https://www.procivmadeira.pt/index.php?option=com_content&view=article&id=360%3Aestradas-encerradas&catid=20%3Aestradas-encerradas&Itemid=213&lang=pt";

    private String[] string = new String[100];

    ParseURL(final Context context){

        final String activity = DbHelper.retrieveActivityRecognitionData(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Document document = Jsoup.parse(response);

                int i = 0;

                for( Element element : document.select("a[href$=.pdf]")
                        .select("span[style*=\"color: #000000; text-decoration: underline;\"]") ) {
                    string[i] = element.text();
                    i++;
                }
                if(activity.equals("in_vehicle")) {
                    TextSpeech.TextToSpeech(context, string[0]);
                }
                else {
                    Intent service = new Intent(context, NotificationService.class);
                    service.putExtra("TEXT", string[0]);
                    context.startService(service);
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}
