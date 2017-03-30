package ru.translator.network;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;



import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.translator.App;


//здесь билдиться структура взаимодействия с интернетом
public class ServiceGenerator {

    private static OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES);

    private static Retrofit.Builder sBuilder = new Retrofit.Builder()
            .baseUrl(ru.translator.network.Request.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createSrevice(Class<S> serviceClass){
        long SIZE_OF_CACHE = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(new File(App.getContext().getCacheDir(), "http"), SIZE_OF_CACHE);
        httpBuilder.cache(cache);
        httpBuilder.networkInterceptors().add(new CachingControlInterceptor());
        Retrofit retrofit = sBuilder.client(httpBuilder
                .build()).build();
        return retrofit.create(serviceClass);
    }

    //Думаю то, как запросы организованны сейчас лучше, чем этот вариант.
    public static void changeApiBaseUrl(String newApiBaseUrl) {
        sBuilder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(newApiBaseUrl);
    }

    public static class CachingControlInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            final Request[] request = {chain.request()};
            if (request[0].method().equals("GET")) {
                if (isOnline(App.getContext())) {
                    request[0] = request[0].newBuilder()
                            .header("Cache-Control", "only-if-cached")
                            .build();
                } else {
                    request[0] = request[0].newBuilder()
                            .header("Cache-Control", "public, max-stale=2419200")
                            .build();
                }
            }
            Response originalResponse = chain.proceed(request[0]);
            return originalResponse.newBuilder()
                    .header("Cache-Control", "max-age=120000")
                    .build();
        }
    }

    private static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
