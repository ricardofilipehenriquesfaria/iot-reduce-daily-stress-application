package com.aware.plugin.google.activity_recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
    O AlarmManager necessita de um BroadcastReceiver para receber e executar a ação necessária.
    O BroadcastReceiver receberá uma notificação assim que o alarme seja acionado (triggered).
*/
public class ScheduleDeleteReceiver extends BroadcastReceiver implements ScheduleDelete{

    /*
        Este método é invocado assim que um Intent seja recebido (o alarme tenha sido acionado).
    */
    @Override
    public void onReceive(Context context, Intent intent) {

        /*
            Chamada ao método deleteOldEntries() da Interface ScheduleDelete.
        */
        ScheduleDelete.super.deleteOldEntries(context);
    }
}