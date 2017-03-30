package ru.translator.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.translator.R;
import ru.translator.adapters.DictionaryAdapter;
import ru.translator.interfaces.HistoryClickListener;
import ru.translator.interfaces.LanguageSelectListener;
import ru.translator.network.Callbacker;
import ru.translator.network.Request;
import ru.translator.network.responses.TranslateFullResponse;
import ru.translator.network.responses.TranslateResponse;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.speechkit.Synthesis;
import ru.yandex.speechkit.Vocalizer;
import ru.yandex.speechkit.VocalizerListener;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static ru.translator.MainActivity.db;

//Экран перевода
public class TranslateFragment extends Fragment implements RecognizerListener, VocalizerListener{
    private static final int REQUEST_PERMISSION_CODE = 1;
    private Recognizer recognizer;
    private EditText text;
    private TextView translateAfter,translateBefore,fromLanguage,toLangeage;
    private Vocalizer vocalizer;
    private String languageTo="en",languageFrom="ru";
    private RelativeLayout relResult;
    public static  HistoryClickListener mHistoryClickListener;
    private DictionaryAdapter mDictionaryAdapter;
    private boolean saveNeeded = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translate, container, false);
        ImageView mic = (ImageView) view.findViewById(R.id.mic);

        fromLanguage = (TextView)view.findViewById(R.id.language_from);
        toLangeage = (TextView)view.findViewById(R.id.language_to);

        fromLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseLanguage(false);
            }
        });
        toLangeage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseLanguage(true);
            }
        });

        mDictionaryAdapter = new DictionaryAdapter();
        RecyclerView resultList = (RecyclerView)view.findViewById(R.id.dict_list);
        resultList.setLayoutManager(new LinearLayoutManager(getActivity()));
        resultList.setAdapter(mDictionaryAdapter);

        text = (EditText) view.findViewById(R.id.res_edt);
        relResult = (RelativeLayout) view.findViewById(R.id.rel_result);
        translateAfter = (TextView)view.findViewById(R.id.translate_result);
        translateBefore = (TextView)view.findViewById(R.id.translate_custom);

        relResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareText(translateAfter.getText().toString());
            }
        });

        mHistoryClickListener = new HistoryClickListener() {

            @Override
            public void translateFromHistory(String fromLang, String toLang, String fromLangText, String toLangText, String textToTranslate) {
                saveNeeded = false;
                languageFrom=fromLang;
                languageTo=toLang;
                fromLanguage.setText(fromLangText);
                toLangeage.setText(toLangText);
                text.setText(textToTranslate);
            }
        };


        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndStartRecognizer();
            }
        });
        ImageView sound = (ImageView) view.findViewById(R.id.din);
        ImageView soundResult = (ImageView) view.findViewById(R.id.din_result);
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndStartVocalizer(text.getText().toString());
            }
        });
        ImageView clear = (ImageView) view.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.getText().clear();
            }
        });
        final Handler handler =new Handler();
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if(saveNeeded){
                    addItemToBD();
                }else{
                    saveNeeded=true;
                }
                handler.removeCallbacks(this);
            }
        };

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String textToTranslate =  charSequence.toString();

                if (textToTranslate.length()!=0){
                    translate(textToTranslate,false);
                }else {
                    mDictionaryAdapter.setInfo(null);
                    relResult.setVisibility(View.GONE);
                }
                handler.removeCallbacks(run);
            }
            @Override
            public void afterTextChanged(Editable editable) {
                handler.removeCallbacks(run);
                if (text.getText().length()!=0){
                    handler.postDelayed(run,3000);
                }
            }
        });
        soundResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndStartVocalizer(
                        translateAfter.getText().toString()
                );
            }
        });
        return view;
    }
    //отправить переведенный текст
    private void shareText(String textToShare){

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.send_text));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textToShare);
        startActivity(sharingIntent);
    }

    //переход к выбору языка
    private void chooseLanguage(final boolean dest){
        String language;
        if(dest){
            language = languageFrom;
        }else{
            language=languageTo;
        }
        LanguageChooseFragment languageChooseFragment =  LanguageChooseFragment.getInstance(dest,language);
        languageChooseFragment.setOnLanguageSelectListener(new LanguageSelectListener() {
            @Override
            public void onSelect(String languageKey, String languageValue) {
                getActivity().onBackPressed();
                if(dest){
                    languageTo=languageKey;
                    toLangeage.setText(languageValue);
                }else{
                    languageFrom=languageKey;
                    fromLanguage.setText(languageValue);
                }
                if (text.getText().toString().length()!=0){
                    translate(text.getText().toString(),true);
                }else {
                    mDictionaryAdapter.setInfo(null);
                    relResult.setVisibility(View.GONE);
                }
            }
        });

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,
                R.anim.slide_out_right, R.anim.slide_in_right);
        transaction.add(R.id.first_conteiner, languageChooseFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //запрос на перевод текста
    private void translate(final String textToTranslate, final boolean saveHard){
        Request.sRestService.translate(
                Request.URL_SIMPLE,
                Request.TRANSLATE_API_KEY,
                textToTranslate,
                languageTo)
                .enqueue(new Callbacker<TranslateResponse>(getActivity(),new Callback<TranslateResponse>() {
            @Override
            public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
                relResult.setVisibility(View.VISIBLE);
                translateBefore.setText(textToTranslate);
                translateAfter.setText(response.body().text.get(0));
                if(saveHard){
                    addItemToBD();
                }
                translateFull(textToTranslate);
            }
            @Override
            public void onFailure(Call<TranslateResponse> call, Throwable t) {

            }
        }));
    }

    //новый объект в бд
    private void addItemToBD(){
        db.addRec(translateBefore.getText().toString(),
                translateAfter.getText().toString(),
                languageFrom,
                languageTo,
                fromLanguage.getText().toString(),
                toLangeage.getText().toString(),
                false);
    }

    //полный перевод
    private void translateFull(String textToTranslate){
        Request.sRestService.translateDict(Request.URL_FULL,
                Request.DICTINARY_API_KEY,
                textToTranslate,
                languageFrom+"-"+languageTo)
                .enqueue(new Callbacker<TranslateFullResponse>(getActivity(), new Callback<TranslateFullResponse>() {
            @Override
            public void onResponse(Call<TranslateFullResponse> call, Response<TranslateFullResponse> response) {
                if(response.body().def.size()>0)
                {
                    mDictionaryAdapter.setInfo(response.body().def.get(0).tr);
                }else{
                    mDictionaryAdapter.setInfo(null);
                }
            }
            @Override
            public void onFailure(Call<TranslateFullResponse> call, Throwable t) {
            }
        }));
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
        resetRecognizer();
        resetVocalizer();
    }


    //запуск записи
    private void createAndStartRecognizer() {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(context, RECORD_AUDIO) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
        } else {
            resetRecognizer();
            recognizer = Recognizer.create(Recognizer.Language.RUSSIAN, Recognizer.Model.NOTES, TranslateFragment.this);
            recognizer.start();
        }
    }

    //запуск воспроизведения
    private void createAndStartVocalizer(String textToTalk) {
        if (TextUtils.isEmpty(textToTalk)) {
            Toast.makeText(getContext(), "Write smth to be vocalized!", Toast.LENGTH_SHORT).show();
        } else {
            resetVocalizer();
            vocalizer = Vocalizer.createVocalizer(Vocalizer.Language.RUSSIAN, textToTalk, true, Vocalizer.Voice.ERMIL);
            vocalizer.setListener(TranslateFragment.this);
            vocalizer.start();
        }
    }


    private void resetRecognizer() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer = null;
        }
    }

    private void resetVocalizer() {
        if (vocalizer != null) {
            vocalizer.cancel();
            vocalizer = null;
        }
    }

    @Override
    public void onSynthesisBegin(Vocalizer vocalizer) {

    }

    @Override
    public void onSynthesisDone(Vocalizer vocalizer, Synthesis synthesis) {

    }

    @Override
    public void onPlayingBegin(Vocalizer vocalizer) {

    }

    @Override
    public void onPlayingDone(Vocalizer vocalizer) {

    }

    @Override
    public void onVocalizerError(Vocalizer vocalizer, ru.yandex.speechkit.Error error) {
        Toast.makeText(getActivity(), R.string.error_sound,Toast.LENGTH_SHORT).show();
        resetVocalizer();
    }

    @Override
    public void onRecordingBegin(Recognizer recognizer) {
        Toast.makeText(getActivity(), R.string.speek,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSpeechDetected(Recognizer recognizer) {

    }

    @Override
    public void onSpeechEnds(Recognizer recognizer) {

    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {

    }

    @Override
    public void onSoundDataRecorded(Recognizer recognizer, byte[] bytes) {

    }

    @Override
    public void onPowerUpdated(Recognizer recognizer, float v) {

    }

    @Override
    public void onPartialResults(Recognizer recognizer, Recognition recognition, boolean b) {
        text.setText(recognition.getBestResultText());
    }

    @Override
    public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {
        text.setText(recognition.getBestResultText());
    }

    @Override
    public void onError(Recognizer recognizer, Error error) {
        Toast.makeText(getActivity(), R.string.error_record,Toast.LENGTH_SHORT).show();
    }


}
