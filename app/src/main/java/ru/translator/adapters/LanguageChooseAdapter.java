package ru.translator.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.translator.interfaces.ClickListener;
import ru.translator.R;
import ru.translator.network.responses.Lang;


//адаптер списка выбора языка
public class LanguageChooseAdapter extends RecyclerView.Adapter<LanguageChooseAdapter.ViewHolder> {
    private List<Lang> rows;

    private ClickListener click;
    public void setOnClickListener(ClickListener click) {
        this.click = click;
    }


    public void setInfo(List<Lang> languages) {
        this.rows = languages;
        notifyDataSetChanged();

    }
    @Override
    public LanguageChooseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_lang, parent, false);
        LanguageChooseAdapter.ViewHolder vh = new LanguageChooseAdapter.ViewHolder(v);
        return vh;
    }
    private Lang getItem(int position) {
        return rows.get(position);
    }
    @Override
    public void onBindViewHolder(LanguageChooseAdapter.ViewHolder holder, int position) {
        holder.language.setText(getItem(position).value);
        holder.setClickListener(click);
    }
    @Override
    public int getItemCount() {
        if (rows == null) return 0;
        return rows.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        TextView language;
        private ClickListener clickListener;
        ViewHolder(final View v) {
            super(v);
            language=(TextView)v.findViewById(R.id.language_text);
            v.setOnClickListener(this);
        }
        void setClickListener(ClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }
        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getAdapterPosition());
        }
    }
}