package com.aware.plugin.google.activity_recognition;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.TimeZone;

import static java.lang.String.valueOf;

/*
    O ContentObserver suporta um conjunto de callbacks que são disparadas quando os dados mudam.
    Assim, este acaba por ter como principal funcionalidade observar as mudanças na base de dados ou dos dados fornecidos pelos ContentProviders.
    Isto permite que possamos reagir às mudanças efetuadas nos dados da base de dados.
*/
public class Google_AR_Observer extends ContentObserver {

    /*
        Constante para colocar a zeros o valor das horas, minutos, segundos e milisegundos do objeto Calendar.
        Deste modo poderemos obter apenas a data atual às 00:00, para que possam ser eliminados todos os dados na base de dados anteriores à data atual.
    */
    private static final int ZERO = 0;

    /*
        Interface para informações globais acerca do ambiente da aplicação.
        Esta é uma classe abstracta, que permite o acesso a recursos e classes específicos da aplicação.
    */
    private Context mContext;

    /*
        Construtor
        Normalmente o construtor de um ContentObserver é definido como ContentObserver(Handler handler).
        Mas, uma vez que não existe a necessidade da utilização de um Handler, este será null.
    */
    public Google_AR_Observer(Context context) {

        /*
            Passamos null como parâmetro no método super() uma vez que não estamos utilizando um Handler.
            A sintaxe utilizada, caso estivéssemos utilizando um Handler seria super(handler).
        */
        super(null);

        mContext = context;
    }

    /*
        Este método é chamado sempre que exista alguma alteração na base de dados.
    */
    @Override
    public void onChange (boolean selfChange) {

        /*
            A chamada a este método irá permitir eliminar entradas antigas na base de dados.
        */
        deleteOldEntries(mContext);

        /*
            O método setUserActivity() atualiza os atributos do objeto UserActivity.
        */
        setUserActivity(mContext);
    }

    /*
        Este método irá permitir eliminar entradas antigas na base de dados, de modo a que esta não ocupe demasiado espaço na memória do smartphone.
    */
    private static void deleteOldEntries(Context context) {

        /*
            Obtemos a data e hora atuais.
        */
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        /*
            As horas, minutos, segundos e milisegundos são passados a 0.
            Isto é feito de modo a que apenas sejam eliminadas as entradas na base de dados do(s) dia(s) anterior(es) ao dia atual.
        */
        calendar.set(Calendar.HOUR_OF_DAY, ZERO);
        calendar.set(Calendar.MINUTE, ZERO);
        calendar.set(Calendar.SECOND, ZERO);
        calendar.set(Calendar.MILLISECOND, ZERO);

        /*
            Antes de eliminar as entradas propriamente ditas, é verificado se existem entradas na base de dados, que não tenham como timestamp a data atual.
            Isto é realizado pois quando chamamos o método delete(), mesmo que nada seja eliminado na base de dados, o ContentObserver é novamente chamado, entrando num ciclo infinito.
        */
        Cursor cursor = context.getContentResolver().query(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI,
                null,
                "timestamp <" + calendar.getTimeInMillis(),
                null,
                null);

        /*
            Como havia sido descrito é verificado se existem entradas na base de dados que não tenham como timestamp a data atual.
        */
        if(cursor != null && cursor.getCount() >= 1) {

            /*
                Se sim, são então eliminadas todas as entradas do(s) dia(s) anterior(es) à data atual.
            */
            context.getContentResolver().delete(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI,
                    "timestamp <" + calendar.getTimeInMillis(),
                    null);
        }

        /*
            O Cursor é então fechado, libertando todos os seus recursos e tornando-o completamente inválido.
        */
        assert cursor != null;
        cursor.close();
    }

    /*
        Método que permite atualizar os atributos da classe UserActivity,
        ao aceder à base de dados e ao obter a última leitura realizada pela Activity Recognition API.
    */
    private static void setUserActivity(Context context) {

        Cursor cursor = context.getContentResolver().query(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI, null, null, null, null);

        /*
            É verificado se já existe alguma entrada na base de dados.
        */
        if(cursor != null && cursor.moveToLast()) {

            UserActivity.setActivityName(valueOf(cursor.getString(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITY_NAME))));
            UserActivity.setActivityType(cursor.getInt(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITY_TYPE)));
            UserActivity.setConfidence(cursor.getInt(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.CONFIDENCE)));

            try {
                UserActivity.setActivities(new JSONArray(cursor.getString(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITIES))));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            cursor.close();

        } else {

            /*
                Caso não exista nenhuma entrada na base de dados, os dados são atribuídos por default.
            */
            UserActivity.setActivityName("unknown");
            UserActivity.setActivityType(4);
            UserActivity.setConfidence(100);

            try {
                UserActivity.setActivities(new JSONArray("[{\"activity\":\"unknown\",\"confidence\":100}]"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}