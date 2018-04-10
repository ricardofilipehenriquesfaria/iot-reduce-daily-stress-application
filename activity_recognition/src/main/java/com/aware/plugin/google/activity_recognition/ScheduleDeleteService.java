package com.aware.plugin.google.activity_recognition;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

/*
    Esta é a classe base que manipula os requests assíncronos que foram agendados anteriormente.
    A tarefa que desejamos agendar deve ser definida neste JobService.
    Este será, na verdade, um Serviço que estende a classe JobService.
    É este serviço que irá permitir que o sistema execute uma tarefa, independentemente de a aplicação se encontrar ativa ou não.
*/
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScheduleDeleteService extends JobService {

    /*
        Método chamado para indicar que o trabalho (job) começou a ser executado.
        Ao retornar false neste método significa que o trabalho(job) foi concluído.
        Desta forma o wakelock do sistema será libertado, e o método onStopJob() não será chamado.
    */
    @Override
    public boolean onStartJob(JobParameters params) {

        /*
            Chamada ao método deleteOldEntries() da classe abstracta ScheduleDelete.
        */
        (new ScheduleDelete(){}).deleteOldEntries(getApplicationContext());

        /*
            Este método é chamado para informar o JobScheduler que o trabalho (job) terminou a sua tarefa.
            Quando o sistema recebe esta mensagem, este liberta o wakelock para este trabalho (job).
        */
        jobFinished(params, false);

        return false;
    }

    /*
        Este método é chamado se o sistema determinar que a execução do trabalho (job) foi interrompido,
        antes de chamar o método jobFinished().
    */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}