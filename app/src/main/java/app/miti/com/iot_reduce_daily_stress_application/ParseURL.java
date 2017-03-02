package app.miti.com.iot_reduce_daily_stress_application;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * Created by Ricardo on 10-02-2017.
 */

class ParseURL extends AsyncTask<Void, Void, String[]> {

    private String[] string = new String[100];

    interface AsyncTaskCallback{
        void process(String[] output);
    }

    private AsyncTaskCallback delegate = null;

    ParseURL(AsyncTaskCallback delegate){
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String[] doInBackground(Void... voids) {
        try{

            String URL = "https://www.procivmadeira.pt/index.php?option=com_content&view=article&id=360%3Aestradas-encerradas&catid=20%3Aestradas-encerradas&Itemid=213&lang=pt";
            Document document = Jsoup.connect(URL).get();

            int i = 0;

            for( Element element : document.select("a[href$=.pdf]")
                    .select("span[style*=\"color: #000000; text-decoration: underline;\"]") ) {
                string[i] = element.text();
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        delegate.process(strings);
    }
}
