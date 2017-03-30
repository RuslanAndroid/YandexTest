package ru.translator.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.List;

import ru.translator.MainActivity;
import ru.translator.R;
import ru.translator.adapters.HistoryAdapter;
import ru.translator.adapters.PagerAdapter;
import ru.translator.interfaces.ClickListener;
import ru.translator.interfaces.FavDataChangeListener;
import ru.translator.interfaces.HistoryDataChangeListener;
import ru.translator.util.DB;
import ru.translator.util.items.DataBean;

import static ru.translator.MainActivity.db;

//экран истории

public class HistoryFragment extends Fragment {
    HistoryAdapter adapter;
    int type;
    EditText searchView;
    RelativeLayout emptyRel,notEmptyRel;
    public static HistoryFragment getInstance(int type){
        HistoryFragment fragment = new HistoryFragment();
        Bundle  bundle = new Bundle();
        bundle.putInt("type",type);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history,container,false);
        type = getArguments().getInt("type");
        RecyclerView list = (RecyclerView)view.findViewById(R.id.history_list);
        emptyRel = (RelativeLayout)view.findViewById(R.id.empty_rel);
        notEmptyRel = (RelativeLayout)view.findViewById(R.id.not_empty_rel);
        adapter= new HistoryAdapter(getActivity());
        searchView = (EditText)view.findViewById(R.id.search_edt);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(searchView.getText().toString());
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        new Loader(db).execute();

        if(type == PagerAdapter.TYPE_HISTORY){
            adapter.setFavClick(new ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    db.addFavRec(adapter.getId(position),!adapter.isItemFav(position));
                }
            });
            db.setHistoryDataChangeListener(new HistoryDataChangeListener() {
                @Override
                public void onInsert(DataBean item) {
                    adapter.addItem(item);
                    whatToShow();
                }

                @Override
                public void onFavChange(int id) {
                    adapter.makeItemFav(adapter.getPositionById(id));
                    whatToShow();
                }

                @Override
                public void onRemoveData() {
                    adapter.setInfo(null);
                    whatToShow();
                }

                @Override
                public void onRemoveFavData() {
                    adapter.ramoveAllFavs();
                    whatToShow();
                }
            });
        }else {
            adapter.setFavClick(new ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    db.addFavRec(adapter.getId(position),!adapter.isItemFav(position));
                }
            });
            db.setFavDataChangeListener(new FavDataChangeListener() {
                @Override
                public void onInsert(DataBean item) {
                    adapter.addItem(item);
                    whatToShow();
                }

                @Override
                public void onFavRemove(int id) {
                    adapter.removeItem(adapter.getPositionById(id));
                    whatToShow();
                }

                @Override
                public void onRemoveData() {
                    adapter.setInfo(null);
                    whatToShow();
                }
            });

        }
        adapter.setonClickListener(new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                MainActivity.mTranslate.move();
                TranslateFragment.mHistoryClickListener.translateFromHistory(
                        adapter.getItem(position).getLangFrom(),
                        adapter.getItem(position).getLangTo(),
                        adapter.getItem(position).getLangFromText(),
                        adapter.getItem(position).getLangToText(),
                        adapter.getItem(position).getTextFrom());

            }
        });
        return view;
    }
    private void whatToShow(){
        if(adapter.getItemCount()==0){
            emptyRel.setVisibility(View.VISIBLE);
            notEmptyRel.setVisibility(View.GONE);
        }else{
            emptyRel.setVisibility(View.GONE);
            notEmptyRel.setVisibility(View.VISIBLE);

        }
    }

     class Loader extends AsyncTask<String,String,List<DataBean>> {
         DB db;
         private Loader(DB db) {
            this.db = db;
         }
         @Override
         protected List<DataBean> doInBackground(String... params) {
             if(type== PagerAdapter.TYPE_HISTORY){
                 return db.getAllData();
             }else{
                 return db.getAllFavoriteData();
             }
         }
         @Override
         protected void onPostExecute(List<DataBean> dataBeen) {
             adapter.setInfo(dataBeen);
             whatToShow();
         }
     }

}
