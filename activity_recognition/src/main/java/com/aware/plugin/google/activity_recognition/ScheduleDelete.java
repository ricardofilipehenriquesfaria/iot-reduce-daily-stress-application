package com.aware.plugin.google.activity_recognition;

import android.content.Context;
import android.database.Cursor;

import java.util.Calendar;
import java.util.TimeZone;

abstract class ScheduleDelete {

    /*
        Este método irá permitir eliminar entradas antigas na base de dados, de modo a que esta não ocupe demasiado espaço na memória do smartphone.
    */
    public void deleteOldEntries(Context context) {

        /*
            Constante para colocar a zeros o valor das horas, minutos, segundos e milissegundos do objeto Calendar.
            Deste modo poderemos obter apenas a data atual às 00:00,
            para que possam ser eliminados todos os dados na base de dados anteriores à data atual.
        */
        int ZERO = 0;

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
            Isto é realizado pois quando chamamos o método delete(), mesmo que nada seja eliminado na base de dados,
            o ContentObserver é novamente chamado, entrando num ciclo infinito.
        */
        Cursor cursor = context.getContentResolver().query(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI,
                null,
                "timestamp < " + calendar.getTimeInMillis(),
                null,
                null);

        /*
            Como havia sido descrito anteriormente, é verificado se existem entradas na base de dados que não tenham como timestamp a data atual.
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
}