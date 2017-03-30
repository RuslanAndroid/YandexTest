package ru.translator.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.translator.R;
import ru.translator.network.responses.Mean;
import ru.translator.network.responses.Syn;
import ru.translator.network.responses.Tr;


//адаптер списка подробного перевода
public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.ViewHolder> {
    private List<Tr> rows;

    public void setInfo(List<Tr> rows) {
        this.rows = rows;
        notifyDataSetChanged();
    }
    @Override
    public DictionaryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_dict, parent, false);
        DictionaryAdapter.ViewHolder vh = new DictionaryAdapter.ViewHolder(v);
        return vh;
    }
    private Tr getItem(int position) {
        return rows.get(position);
    }
    @Override
    public void onBindViewHolder(DictionaryAdapter.ViewHolder holder, int position) {

        holder.number.setText(String.valueOf(position+1));
        String syn=getItem(position).text;
        if(getItem(position).syn!=null){
            for (Syn item : getItem(position).syn) {
                syn+=","+item.text;
            }
            if(position!=0)
                syn=syn.substring(1);
        }
        String mean="";
        if(getItem(position).mean!=null){
            for (Mean item : getItem(position).mean) {
                mean+=","+item.text;
            }
            mean=mean.substring(1);
        }

        holder.text.setText(syn);
        holder.info.setText(mean);

    }

    @Override
    public int getItemCount() {
        if (rows == null) return 0;
        return rows.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder  {
        TextView number,text,info;
        ViewHolder(final View v) {
            super(v);
            number = (TextView)v.findViewById(R.id.number);
            text = (TextView)v.findViewById(R.id.res_text);
            info = (TextView)v.findViewById(R.id.info);
        }



    }
}