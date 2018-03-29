package com.aware.plugin.google.activity_recognition;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;

import static java.lang.String.valueOf;

/*
    O ContentObserver suporta um conjunto de callbacks que são disparadas quando os dados mudam.
    Assim, este acaba por ter como principal funcionalidade observar as mudanças na base de dados ou dos dados fornecidos pelos ContentProviders.
    O ContentObserver não observa diretamente as alterações feitas numa base de dados, tabela ou ContentProviders específicos, no entanto
    reage a todas as alterações que afetem um URI em particular, estas sim causadas por alguma mudança na base de dados.
    Isto permite que possamos reagir às mudanças efetuadas nos dados da base de dados.
*/
public class Google_AR_Observer extends ContentObserver {

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
            O método updateUserActivity() atualiza os atributos do objeto UserActivity.
        */
        updateUserActivity(mContext);
    }

    /*
        Retorna true de modo a que o ContentObserver possa receber notificações acerca das alterações realizadas no próprio conteúdo.
    */
    @Override
    public boolean deliverSelfNotifications(){
        return true;
    }

    /*
        Método que permite atualizar os atributos da classe UserActivity,
        ao aceder à base de dados e ao obter a última leitura realizada pela Activity Recognition API.
    */
    private static void updateUserActivity(Context context) {

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