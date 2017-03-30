package ru.translator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.translator.interfaces.ClickListener;
import ru.translator.R;
import ru.translator.adapters.LanguageChooseAdapter;
import ru.translator.interfaces.LanguageSelectListener;
import ru.translator.network.responses.Lang;
import ru.translator.util.Data;

//экран выбора языка

public class LanguageChooseFragment extends Fragment {

    List<Lang> languagesToShow;

    private LanguageSelectListener mListener;
    public static LanguageChooseFragment getInstance(boolean dest,String language){
        LanguageChooseFragment languageChooseFragment = new LanguageChooseFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("dest",dest);
        bundle.putString("language",language);
        languageChooseFragment.setArguments(bundle);
        return languageChooseFragment;
    }
    public void setOnLanguageSelectListener(LanguageSelectListener listener) {
        this.mListener = listener;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_language_choose,container,false);
        RecyclerView langsList = (RecyclerView)view.findViewById(R.id.langs_list);
        langsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        LanguageChooseAdapter adapter = new LanguageChooseAdapter();
        languagesToShow = new ArrayList<>();
        String language=getArguments().getString("language");
        boolean dest = getArguments().getBoolean("dest");

        for (Lang item : Data.dirs) {
            String key;
            String value;
            if (dest){
                key = item.key;
                value = item.value;
            }else{
                value = item.key;
                key = item.value;
            }
            if(key.equals(language)){
                for(int j =0;j<Data.languages.size();j++){
                    if(Data.languages.get(j).key.equals(value)){
                        languagesToShow.add(Data.languages.get(j));
                    }
                }
            }
        }

        langsList.setAdapter(adapter);
        adapter.setOnClickListener(new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                    mListener.onSelect(languagesToShow.get(position).key,languagesToShow.get(position).value);
            }
        });
        adapter.setInfo(languagesToShow);


        return view;
    }
}
