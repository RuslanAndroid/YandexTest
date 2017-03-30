package ru.translator.network;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import retrofit2.http.Url;
import ru.translator.network.responses.TranslateFullResponse;
import ru.translator.network.responses.TranslateResponse;

//интерфейсы для запросов

public interface RestServiceWithCache {
    @GET
    Call<TranslateResponse> translate(@Url String url,
                                      @Query("key") String key,
                                      @Query("text") String text,
                                      @Query("lang") String lang);

    @GET
    Call<TranslateFullResponse> translateDict(@Url String url,
                                              @Query("key") String key,
                                              @Query("text") String text,
                                              @Query("lang") String lang);
    @GET
    Call<JsonElement> getLangs(@Url String url,
                               @Query("key") String key,
                               @Query("ui") String ui);
    @GET
    Call<JsonElement> getDirs(@Url String url,
                               @Query("key") String key);
}
