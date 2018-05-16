package de.tinf15b4.ihatestau.ihatestau_androidapp.util;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class Talker  {

    private static TextToSpeech talker;

    public static void init(Context context) {
        talker = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                talker.setLanguage(Locale.GERMANY);
            }
        });
    }

    public static void speak(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            talker.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            talker.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
