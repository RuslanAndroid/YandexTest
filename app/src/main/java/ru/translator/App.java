package ru.translator;

import android.app.Application;
import android.content.Context;

import ru.yandex.speechkit.SpeechKit;



public class App extends Application {
    String SPEECH_API_KEY="7eeb9bad-2451-4735-9358-4beb0374c724";
    static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        SpeechKit.getInstance().configure(this,SPEECH_API_KEY);

    }
    public static Context getContext() {
        return context;
    }
}
