package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Context;
import android.content.Intent;

import com.aware.utils.Aware_TTS;

/**
 * Created by Ricardo on 09-02-2017.
 */

class TextSpeech {

    TextSpeech(Context context, String string) {
        Intent speak = new Intent(Aware_TTS.ACTION_AWARE_TTS_SPEAK);
        speak.putExtra(Aware_TTS.EXTRA_TTS_TEXT, string);
        speak.putExtra(Aware_TTS.EXTRA_TTS_REQUESTER, "app.miti.com.iot_reduce_daily_stress_application");
        context.sendBroadcast(speak);
    }
}
