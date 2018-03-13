package com.aware.plugin.google.activity_recognition;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.google.activity_recognition.Google_AR_Provider.Google_Activity_Recognition_Data;
import com.aware.ui.PermissionsHandler;
import com.aware.utils.Aware_Plugin;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

/*
    A classe Plugin é uma subclasse de Aware_Plugin, que consequentemente é uma subclasse de Service do Android.
    Deste modo a classe Plugin é integrada com a Framework Aware,
    herdando todos os métodos e variáveis "public" e "protected" da classe Aware_Plugin e da classe Service.
*/
public class Plugin extends Aware_Plugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    /*
        Constante que permitirá ativar ou desativar o Plugin ("true" ou "false").
    */
    public static final String STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION = "status_plugin_google_activity_recognition";

    /*
        Constante que permitirá definir a frequência com que será detetada a atividade do utilizador (em segundos).
        Por defeito, a frequência de leitura é de 60 segundos.
    */
    public static final String FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION = "frequency_plugin_google_activity_recognition";

    /*
        Quando um Broadcast Intent é criado, este deve possuir uma Action String.
        Esta Action String identifica de maneira exclusiva o evento de transmissão (broadcast), e deve ser única.
        Os Broadcasts são detetados ao registarmos um BroadcastReceiver, que, por sua vez,
        está configurado para "escutar" esta Action String em particular.
    */
    public static final String ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION = "ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION";

    /*
        Tal como acontece com os Intent padrão, os dados são adicionados a um Broadcast Intent utilizando pares
        de chave-valor (key-value), em conjunção com o método putExtra() do objeto Intent.
        Assim, estas constantes actuarão como chaves (keys) para o envio dos valores da atividade do utilizador
        e respetivos níveis de confiança através do Broadcast Intent.
    */
    public static final String EXTRA_ACTIVITY = "activity";
    public static final String EXTRA_CONFIDENCE = "confidence";

    /*
        Variáveis onde serão guardados os valores da atividade do utilizador e respetivos níveis de confiança,
        que serão passados através do Broadcast Intent.
    */
    public static int current_activity = -1;
    public static int current_confidence = -1;

    /*
        O objeto GoogleApiClient é utilizado para aceder às APIs da Google fornecidas na biblioteca de serviços do GooglePlay.
        Este também administra as conexões de rede entre o dispositivo do utilizador e cada serviço da Google.
    */
    private static GoogleApiClient gARClient;

    /*
        De modo a que a aplicação possa monitorizar atividades em segundo plano, sem ter um serviço que esteja sempre em background consumindo recursos,
        é especificada uma PendingIntent callback (geralmente um IntentService) que será chamada quando as atividades forem detetadas.
    */
    private static PendingIntent gARPending;

    /*
        Método chamado pelo sistema quando o serviço é criado pela primeira vez.
    */
    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::Google Activity Recognition";

        /*
            Copiamos as tabelas da base de dados para a variável DATABASE_TABLES da classe Aware_Plugin da Framework Aware.
            Deste modo as tabelas da base de dados são sincronizadas e atualizadas remotamente.
        */
        DATABASE_TABLES = Google_AR_Provider.DATABASE_TABLES;

        /*
            Copiamos os campos da tabela da base de dados para a variável TABLE_FIELDS da classe Aware_Plugin da Framework Aware.
            Deste modo os campos das tabelas da base de dados são sincronizados e atualizados remotamente.
        */
        TABLES_FIELDS = Google_AR_Provider.TABLES_FIELDS;

        /*
            Copiamos o CONTENT_URI do ContentProvider para a variável CONTEXT_URIS da classe Aware_Plugin da Framework Aware.
            Deste modo o CONTENT_URI é sincronizado e atualizado remotamente.
        */
        CONTEXT_URIS = new Uri[]{Google_Activity_Recognition_Data.CONTENT_URI};

        /*
            O ContextProducer é uma interface que permite compartilhar o contexto com outras aplicações/plugins.
            Neste caso o contexto é também partilhado com a Framework Aware.
        */
        CONTEXT_PRODUCER = new ContextProducer() {

            @Override
            public void onContext() {

                /*
                    Realizamos o broadcast quando temos uma nova atividade, com os seguintes extras:
                    - atividade: (int) a atual atividade detetada;
                    - confiança: (int) quão confiante é a previsão (0 - 100%).
                */
                Intent context = new Intent(ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION);
                context.putExtra(EXTRA_ACTIVITY, current_activity);
                context.putExtra(EXTRA_CONFIDENCE, current_confidence);
                sendBroadcast(context);
            }
        };

        /*
            O método isGooglePlayServicesAvailable() verifica se os serviços do Google Play estão instalados, e ligados no dispositivo.
            Também verifica se a versão instalada no dispositivo não é mais antiga do que a requerida pelo cliente.
            Os valores retornados pelo método isGooglePlayServicesAvailable() são:
             - ConnectionResult.SUCCESS (valor da constante: 0) - A conexão foi bem sucedida;
             - ConnectionResult.SERVICE_MISSING (valor da constante: 1) - Os serviços do Google Play não existem neste dispositivo;
             - ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED (valor da constante: 2) - A versão instalada dos serviços do Google Play está desatualizada);
             - ConnectionResult.SERVICE_DISABLED (valor da constante: 3) - A versão instalada dos serviços do Google Play foi desativada no dispositivo;
             - ConnectionResult.SERVICE_INVALID (valor da constante: 9) - A versão dos serviços do Google Play instalada neste dispositivo não é autêntica.
        */
        if (!(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS)) {
            if (DEBUG) Log.e(TAG, "Google Services is not available on this device.");
        } else {

            /*
                A classe GoogleApiClient.Builder fornece métodos que permitem que especifiquemos as APIs da Google que desejamos utilizar,
                sendo que neste caso apenas iremos utilizar a API ActivityRecognition.
                O método addConnectionCallbacks() permite que registemos um Listener para receber eventos de conexão deste GoogleApiClient.
                O método addOnConnectionFailedListener() permite adicionar um Listener para receber eventos de falhas de conexão deste GoogleApiClient.
                Por fim, é chamado o método build() para criar uma instância do objeto.
            */
            gARClient = new GoogleApiClient.Builder(this)
                    .addApiIfAvailable(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            /*
                A flag FLAG_UPDATE_CURRENT indica que, se o PendingIntent descrito já existir,
                mantém esse PendingIntent, mas substitui os dados EXTRA pelos que estão neste novo Intent.
            */
            Intent gARIntent = new Intent(getApplicationContext(), Algorithm.class);
            gARPending = PendingIntent.getService(getApplicationContext(), 0, gARIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            /*
                A frequência com que será detetada a atividade do utilizador é colocada a 120 segundos.
            */
            Aware.setSetting(this, Plugin.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 120);

            /*
                Inicia o Plugin. Caso este já se encontre em execução, são aplicadas as novas definições.
            */
            Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");
        }
    }

    /*
        Método chamado pelo sistema sempre que um cliente inicia explicitamente o serviço, chamando startService(Intent).
        Este método é chamado a cada 5 minutos pelo Aware para garantir que este Plugin ainda esteja em execução.
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean permissions_ok = true;

        /*
            São determinadas se todas as permissões foram concedidas.
        */
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                permissions_ok = false;
                break;
            }
        }

        if (permissions_ok) {

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            /*
                Se todas as permissões tiverem sido concedidas o Plugin é ativado.
            */
            Aware.setSetting(this, Plugin.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, true);

            /*
                Se ainda não tiver sido definida nenhuma frequência com que será detetada a atividade do utilizador,
                colocamos o valor da frequência a 120 (segundos).
            */
            if (Aware.getSetting(this, Plugin.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).length() == 0) {
                Aware.setSetting(this, Plugin.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 120);
            }

            if (gARClient != null && !gARClient.isConnected()) gARClient.connect();

        } else {

            /*
                Caso alguma permissão não tenha sido concedida, criamos um novo Intent e passamos as permissões que necessitamos.
                A Activity PermissionsHandler é uma atividade invisível, da Framework Aware,
                utilizada para solicitar as permissões necessárias ao utilizador (a partir da API 23).
            */
            Intent permissions = new Intent(this, PermissionsHandler.class);
            permissions.putExtra(PermissionsHandler.EXTRA_REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS);
            permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(permissions);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /*
        Método chamado pelo sistema para notificar que um serviço não está a ser utilizado e está sendo removido.
        O serviço deve limpar todos os recursos que possui (threads, receivers registados, ...) nesse momento.
    */
    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
            O Plugin é desativado.
        */
        Aware.setSetting(getApplicationContext(), Plugin.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, false);

        /*
            Fecha a conexão com os serviços do Google Play.
        */
        if (gARClient != null && gARClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(gARClient, gARPending);
            gARClient.disconnect();
        }

        /*
            O Plugin é desligado (turned OFF).
        */
        Aware.stopAWARE();
    }

    /*
        Método chamado quando existe algum erro ao conectar o cliente ao serviço.
    */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connection_result) {
        if (DEBUG) Log.w(TAG, "Error connecting to Google's activity recognition services, will try again in 5 minutes");
    }

    /*
        Depois de chamar o método connect(), este método será invocado de forma assíncrona quando o pedido de conexão for concluído com sucesso.
        Depois desta callback, a aplicação pode fazer pedidos noutros métodos fornecidos pelo cliente.
    */
    @Override
    public void onConnected(Bundle bundle) {
        if (DEBUG) Log.i(TAG, "Connected to Google's Activity Recognition API");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(gARClient, Long.valueOf(Aware.getSetting(getApplicationContext(), Plugin.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION)) * 1000, gARPending);
    }

    /*
        Método chamado quando o cliente está temporariamente desconectado.
        Por exemplo, pode acontecer existir um problema com o serviço remoto.
        O GoogleApiClient tentará automaticamente restaurar a conexão.
    */
    @Override
    public void onConnectionSuspended(int i) {
        if (DEBUG) Log.w(TAG, "Error connecting to Google's activity recognition services, will try again in 5 minutes");
    }
}