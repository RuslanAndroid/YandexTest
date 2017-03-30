package ru.translator;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.translator.network.Callbacker;
import ru.translator.network.Request;
import ru.translator.network.responses.Lang;
import ru.translator.util.Data;
//эта активность вызывается при запуске приложения
public class Splash extends AppCompatActivity {

    Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        setContentView(R.layout.activity_splash);
        getLangs();
    }

    private void getLangs(){
        Request.sRestService.getLangs(
                Request.URL_LANGS,
                Request.TRANSLATE_API_KEY,"ru")
                .enqueue(new Callbacker<JsonElement>(Splash.this, new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                JsonObject langs  = response.body().getAsJsonObject().getAsJsonObject("langs");
                Set<Map.Entry<String, JsonElement>> entries = langs.entrySet();
                for (Map.Entry<String, JsonElement> entry: entries) {
                    Data.languages.add(new Lang(entry.getKey(),String.valueOf(entry.getValue()).replace("\"", "")));
                }
                Collections.sort(Data.languages, new Comparator<Lang>() {
                    @Override
                    public int compare(Lang s1, Lang s2) {
                        return s1.value.compareToIgnoreCase(s2.value);
                    }
                });
                getDirs();
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getLangs();
                    }
                },4000);

            }
        }));
    }
    private void getDirs(){
        Request.sRestService.getDirs(Request.URL_DIRS,
                Request.DICTINARY_API_KEY)
                .enqueue(new Callbacker<JsonElement>(Splash.this, new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        String json = response.body().toString();
                        JsonArray array = new Gson().fromJson(json, JsonArray.class);
                        for(JsonElement dir : array){
                            Data.dirs.add(new Lang(dir.getAsString().split("-")[0],dir.getAsString().split("-")[1]));
                        }
                       goMain();
                    }
                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {

                    }
                }));
    }
    private void goMain(){
        Intent intent = new Intent(Splash.this, MainActivity.class).addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
