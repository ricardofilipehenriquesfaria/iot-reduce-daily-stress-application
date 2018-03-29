package com.aware.plugin.google.activity_recognition;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.google.activity_recognition.Google_AR_Provider.Google_Activity_Recognition_Data;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/*
    IntentService que processa a resposta/resultado da API Activity Recognition.
*/
public class Algorithm extends IntentService {

    /*
        Construtor Default
    */
    public Algorithm() {
        super(Plugin.TAG);
    }

    /*
        Quando é chamado o método startService() para iniciar este IntentService, é passado um Intent como parâmetro.
        O Intent contém dados que necessitam de ser processados.
        O método onHandleIntent() processa os dados desse Intent.
    */
    @Override
    protected void onHandleIntent(Intent intent) {

        /*
            Primeiro tem de ser verificado se alguma nova atividade foi identificada.
            O método hasResult() irá retornar true se o Intent contém um ActivityRecognitionResult ou false caso contrário ou caso o Intent fornecido seja null.
            O ActivityRecognitionResult contém uma lista de atividades que o utilizador possa ter realizado num determinado momento.
            Um nível de confiança é associado a cada atividade indicando o nível de confiança relacionado com a(s) atividade(s) devolvida(s).
        */
        if (ActivityRecognitionResult.hasResult(intent)) {

            /*
                Extrai a ActivityRecognitionResult a partir do Intent que foi passado como parâmetro.
                É extraída a partir dos EXTRAS do Intent que foi enviado a partir do método onStartService() que iniciou este IntentService.
            */
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            /*
                Guarda a atividade mais provável detetada pelo dispositivo tal como o respetivo nível de confiança,
                no momento em que o Activity Recognition foi executado.
            */
            DetectedActivity mostProbable = result.getMostProbableActivity();

            /*
                JSONArray para guardar o nome e o nível de confiança de cada actividade detetada.
            */
            JSONArray activities = new JSONArray();

            /*
                Lista para guardar as atividades que foram detetadas, com o valor de confiança associado a cada atividade.
                As atividades são ordenadas, começando pela mais provável primeiro.
                A soma dos níveis de confiança de todas as atividades detetadas, não necessitam de ser <= 100,
                uma vez que algumas atividades não são mutuamente exclusivas.
                Por exemplo, podemos estar a andar enquanto estamos num autocarro,
                enquanto que outras atividades são hierárquicas (ON_FOOT é uma generalização de andar e correr).
            */
            List<DetectedActivity> otherActivities = result.getProbableActivities();

            /*
                Ciclo para guardar as atividades detetadas e os respetivos níveis de confiança num JSONArray.
            */
            for(DetectedActivity activity : otherActivities) {
                try {
                    JSONObject item = new JSONObject();
                    item.put("activity", getActivityName(activity.getType()));
                    item.put("confidence", activity.getConfidence());
                    activities.put(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            /*
                O método getConfidence() retorna um valor entre 0 e 100 indicando a probabilidade de o utilizador estar a executar uma determinada atividade.
                O valor retornado é guardado na variável estática current_confidence.
            */
            Plugin.current_confidence = mostProbable.getConfidence();

            /*
                O método getType() retorna um valor entre 0 e 8 indicando o tipo de atividade que o utilizador está a executar num determinado momento.
                O valor retornado (int) é guardado na variável estática current_activity.
            */
            Plugin.current_activity = mostProbable.getType();

            /*
                 Uma vez que o tipo da atividade retornado é do tipo numérico (int), passamos esse valor para String, ao utilizarmos a função getActivityName().
                 Este procedimento é utilizado de modo a podermos guardar o nome da atividade na base de dados.
            */
            String activity_name = getActivityName(Plugin.current_activity);

            /*
                Define um objeto que irá conter os novos valores que serão inseridos na base de dados.
                Utilizado para armazenar um conjunto de valores que o ContentResolver possa processar.
            */
            ContentValues data = new ContentValues();

            /*
                São definidos e inseridos os valores para cada coluna.
                Os argumentos para o método put() são "nome da coluna" e "valor".
                A coluna _ID não é adicionada pois esta coluna é inserida automaticamente.
                O Provider atribui um valor exclusivo de _ID para cada linha adicionada.
                O Provider irá utilizar este valor de _ID como chave principal da tabela da base de dados.
            */
            data.put(Google_Activity_Recognition_Data.TIMESTAMP, System.currentTimeMillis());
            data.put(Google_Activity_Recognition_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
            data.put(Google_Activity_Recognition_Data.ACTIVITY_NAME, activity_name);
            data.put(Google_Activity_Recognition_Data.ACTIVITY_TYPE, Plugin.current_activity);
            data.put(Google_Activity_Recognition_Data.CONFIDENCE, Plugin.current_confidence);
            data.put(Google_Activity_Recognition_Data.ACTIVITIES, activities.toString());

            /*
                Uma vez que o objeto data (ContentValues) tenha sido carregado,
                será chamado o método insert() para inserir os dados na base de dados.
                O valor do CONTENT_URI corresponde a um URI (URL) que representa a tabela na qual iremos inserir os dados.
                Este método irá retornar o URI (URL) da linha recém-criada na base de dados.
            */
            getContentResolver().insert(Google_Activity_Recognition_Data.CONTENT_URI, data);

            /*
                Este é o Intent sobre o qual será feito o broadcast.
                A String de ação, identifica de maneira exclusiva o evento de transmissão, e deve ser única.
                Todos os BroadcastReceivers que correspondam a este Intent receberão o broadcast.
            */
            Intent context = new Intent(Plugin.ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION);

            /*
                Para anexar informações adicionais ao Intent é utilizado o método putExtra().
            */
            context.putExtra(Plugin.EXTRA_ACTIVITY, Plugin.current_activity);
            context.putExtra(Plugin.EXTRA_CONFIDENCE, Plugin.current_confidence);

            /*
                Realiza o broadcast do Intent a todos os BroadcastReceivers interessados.
            */
            sendBroadcast(context);
        }
    }

    /*
        Método para passar o tipo de atividade obtido (numérico) do tipo int para String.
    */
    private static String getActivityName(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.WALKING:
                return "walking";
            default:
                return "unknown";
        }
    }
}