package ru.translator.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


import java.io.IOException;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import ru.translator.R;

//коллбэкер(обработка response'ов)
public class Callbacker<T> implements Callback<T> {

    private Callback<T> mCallback;
    private Activity mActivity;

    public Callbacker(Activity activity,Callback<T> callback){
        this.mCallback = callback;
        this.mActivity = activity;
    }

    @Override
    public void onResponse(final Call<T> call, final Response<T> response) {
        JsonObject jobj = null;
        if(response.code()==200){
            mCallback.onResponse(call, response);
        }else {
            call.request().cacheControl().noCache();
            try {
                jobj = new Gson().fromJson(response.errorBody().string(), JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(jobj.has("message")){
                makeAlert(jobj.get("message").getAsString(), mActivity);
            }else{
                makeAlert(mActivity.getString(R.string.error_connect3),mActivity);
            }
                mCallback.onFailure(call, new Throwable());
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        t.printStackTrace();
        call.request().cacheControl().noCache();
        if (!isOnline(mActivity)) {
            Toast.makeText(mActivity, R.string.error_connect,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mActivity, R.string.error_connect2,Toast.LENGTH_LONG).show();
        }
        mCallback.onFailure(call, t);
    }

    private static void makeAlert(final String _msg, final Activity activity) {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   new AlertDialog.Builder(activity)
                            .setMessage(_msg)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                    .show();
                }
            });
        }catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }

    }

    private static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
