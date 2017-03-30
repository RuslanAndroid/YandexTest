package ru.translator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import ru.translator.R;
import ru.translator.adapters.PagerAdapter;

import static ru.translator.MainActivity.db;

//экран истории и избранного

public class AllHistoryFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_history,container,false);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.history));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.favorite));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        ImageView delete = (ImageView)view.findViewById(R.id.delete);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getActivity().getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem()==0){
                    db.removeAll();
                    Toast.makeText(getActivity(), R.string.clear_history,Toast.LENGTH_SHORT).show();
                }else{
                    db.removeAllFav();
                    Toast.makeText(getActivity(), R.string.clear_fav_history,Toast.LENGTH_SHORT).show();
                }


            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });




        return view;
    }
}
