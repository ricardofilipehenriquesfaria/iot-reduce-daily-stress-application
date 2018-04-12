package com.aware.plugin.google.activity_recognition;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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

import java.util.Calendar;

/*
    A classe Plugin é uma subclasse de Aware_Plugin, que consequentemente é uma subclasse de Service do Android.
    Deste modo a classe Plugin é integrada com a Framework Aware,
    herdando todos os métodos e variáveis "public" e "protected" da classe Aware_Plugin e da classe Service.
*/
public class Plugin extends Aware_Plugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
        de chave-valor (key-value), em conjunto com o método putExtra() do objeto Intent.
        Assim, estas constantes actuarão como chaves (keys) para o envio dos valores da atividade do utilizador
        e respetivos níveis de confiança através do Broadcast Intent.
    */
    public static final String EXTRA_ACTIVITY = "activity";
    public static final String EXTRA_CONFIDENCE = "confidence";

    /*
        Quando criamos um JobInfo por meio de um JobInfo.Builder, fornecemos um int que funciona como um ID único associado a essa classe.
    */
    private static final int JOB_ID = 1;

    /*
        Constante para colocar a um o valor das horas do objeto Calendar.
        Assim, poderemos agendar o AlarmManager (API < 21) para ser executado às 01:00:00,
        de modo a que possam ser eliminados todos os dados na base de dados anteriores à data atual.
    */
    private static final int ONE = 1;

    /*
        Constante para colocar a zeros o valor dos minutos, segundos e milissegundos do objeto Calendar.
        Assim, poderemos agendar o AlarmManager (API < 21) para ser executado às 01:00:00,
        de modo a que possam ser eliminados todos os dados na base de dados anteriores à data atual.
    */
    private static final int ZERO = 0;

    /*
        Esta constante irá especificar o intervalo com que uma determinada tarefa do JobScheduler será executada.
        Assim, poderemos agendar o JobScheduler (API >= 21) para ser executado diariamente,
        de modo a que possam ser eliminados todos os dados na base de dados anteriores à data atual.
        Na realidade, não temos controlo sobre quando esta tarefa será executada,
        apenas temos a garantia de que esta será executada no máximo uma vez dentro deste intervalo.
    */
    private static final long ONE_DAY_INTERVAL = 24 * 60 * 60 * 1000L;

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
        O objeto Google_AR_Observer é um ContentObserver utilizado para responder às alterações num determinado URI.
        Quando o URI muda, o método onChange() do ContentObserver é disparado.
    */
    private Google_AR_Observer googleARObserver = null;

    /*
        Método chamado pelo sistema quando o serviço é criado pela primeira vez.
    */
    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::Google Activity Recognition";

        /*
            O Android fornece várias APIs para agendar tarefas em background (fora do ciclo de vida da aplicação).
            Estas permitem acionar eventos ou ações mesmo que a aplicação não se encontre em execução.

            O JobScheduler API permite agendar tarefas para quando o dispositivo tem mais recursos disponíveis ou para quando as condições certas sejam atendidas.
            Este também adia a execução dessas tarefas conforme seja necessário, para cumprir com as restrições do modo Doze e do App StandBy,
            permitindo combinar tarefas (jobs) para que o consumo de bateria seja reduzido.

            Já o AlarmManager API fornece acesso aos serviços de alarme ao nível do sistema.
            Este deve ser somente utilizado quando queremos tarefas que sejam executadas num horário específico,
            mas que não exigem outras condições de execução mais robustas que o JobScheduler permite que especifiquemos.
            Porque é particularmente provável que o Doze afecte as atividades que o AlarmManager gere, a partir da API 23 tornou-se necessário optimizar
            a utilização de agendamento de tarefas.

            Mas, o JobScheduler apenas pode ser utilizado a partir da API 21 (Android Lollipop), e é por isso que temos esta condição:
            - Caso a API seja igual ou superior à API 21 iremos utilizar o JobScheduler para eliminar as entradas antigas na base de dados.
            - Caso seja inferior será utilizado o AlarmManager para realizar a mesma tarefa.
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) scheduleDeleteJobScheduler();
        else scheduleDeleteAlarmManager();

        /*
            O método registerContentObserver() permite que um ContentObserver seja registado de modo a escutar quaisquer alterações que
            afetem um URI em particular.
        */
        googleARObserver = new Google_AR_Observer(this);
        getContentResolver().registerContentObserver(Google_Activity_Recognition_Data.CONTENT_URI, true, googleARObserver);

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
             - ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED (valor da constante: 2) - A versão instalada dos serviços do Google Play está desatualizada;
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
            O método unregisterContentObserver() cancela o registo do ContentObserver para que este não receba mais nenhuma callback.
            É importante cancelar o registo do ContentObserver de modo a evitar falhas de memória (os ContentObservers ficam ativos,
            mesmo que a aplicação falhe ou seja fechada pelo Android).
        */
        getContentResolver().unregisterContentObserver(googleARObserver);

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
        Aware.stopAWARE(this);
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

    /*
        Método utilizado para eliminar entradas antigas na base de dados utilizando o JobScheduler API.
        O JobScheduler API permite agendar tarefas para quando o dispositivo tem mais recursos disponíveis ou para quando as condições certas sejam atendidas.
        Este também adia a execução dessas tarefas conforme seja necessário para cumprir com as restrições do modo Doze e do App StandBy,
        permitindo combinar tarefas (jobs) para que o consumo de bateria seja reduzido.
    */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scheduleDeleteJobScheduler(){

        JobScheduler jobScheduler = (JobScheduler)  getBaseContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        /*
            Container de dados transmitido ao JobScheduler encapsulando totalmente os parâmetros necessários para agendar a tarefa (job).
            Estes são construídos utilizando o JobInfo.Builder, devendo ser especificada pelo menos uma restrição no objeto JobInfo que estamos criando.
            O objetivo aqui é fornecer ao JobScheduler uma semântica de alto nível sobre a tarefa que desejamos realizar.
        */
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(getBaseContext(), ScheduleDeleteService.class));

        /*
            Especifica que esta tarefa deverá repetir-se com o intervalo fornecido, não mais de uma vez por período.
            A constante passada como parâmetro especifica o intervalo, com que uma determinada tarefa do JobScheduler será executada.
            Assim, poderemos agendar o JobScheduler para ser executado diariamente,
            de modo a que possam ser eliminados todos os dados na base de dados anteriores à data atual.
            Na realidade, não temos controlo sobre quando esta tarefa será executada,
            apenas temos a garantia de que esta será executada no máximo uma vez dentro deste intervalo.
        */
        builder.setPeriodic(ONE_DAY_INTERVAL);

        /*
            Agenda uma tarefa a ser executada.
        */
        assert jobScheduler != null;
        jobScheduler.schedule(builder.build());
    }

    /*
        Método utilizado para eliminar entradas antigas na base de dados utilizando um AlarmManager.
        O AlarmManager API fornece acesso aos serviços de alarme ao nível do sistema.
        Este deve ser somente utilizado quando queremos tarefas que sejam executadas num horário específico,
        mas que não exigem outras condições de execução mais robustas que o JobScheduler permite que especifiquemos.
    */
    public void scheduleDeleteAlarmManager(){

        /*
            Obtemos a data e hora atuais.
        */
        Calendar calendar = Calendar.getInstance();

        /*
            Colocamos a um o valor das horas e a zero o valor dos minutos, segundos e milisegundos.
        */
        calendar.set(Calendar.HOUR_OF_DAY, ONE);
        calendar.set(Calendar.MINUTE, ZERO);
        calendar.set(Calendar.SECOND, ZERO);
        calendar.set(Calendar.MILLISECOND, ZERO);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        /*
            O Intent é onde registamos o componente (BroadcastReceiver) no qual queremos registar eventos de sistema.
            O PendingIntent especifica uma ação a ser executada no futuro.
            O que acontece é que o Intent é encapsulado (wrap) num PendingIntent e,
            em seguida, chamamos o AlarmManager para executar esse PendingIntent num horário específico no futuro.
        */
        Intent deleteIntent = new Intent(this, ScheduleDeleteReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, deleteIntent, 0);

        /*
            Agenda um alarme a ser repetido diariamente à uma da manhã.
            Parâmetros passados:
            - AlarmManager.RTC_WAKEUP - Ativa o dispositivo para disparar o PendingIntent no horário especificado.
            - AlarmManager.INTERVAL_DAY - O intervalo do alarme, neste caso para ocorrer uma vez por dia.
        */
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}