package ru.translator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.MenuItem;


import ru.translator.adapters.MainPagerAdapter;

import ru.translator.interfaces.MoveToTranslate;
import ru.translator.util.DB;
import ru.translator.util.items.CustomViewPager;

//главная активити
public class MainActivity extends AppCompatActivity {
    public static MoveToTranslate mTranslate;
    public static DB db;
    FragmentManager fragmentManager;
    @Override
    protected void onStart() {
        super.onStart();
        db = new DB(this);
        db.open();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        final CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.main_pager);
        final MainPagerAdapter adapter = new MainPagerAdapter
                (getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        final BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                super.onPageSelected(position);
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_translate:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.action_history:
                                viewPager.setCurrentItem(1);
                                break;
                        }
                        return true;
                    }
                });
        mTranslate = new MoveToTranslate() {
            @Override
            public void move() {
                bottomNavigationView.getMenu().getItem(0).setChecked(true);
                viewPager.setCurrentItem(0);
            }
        };

    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        int backStackEntryCount = fm.getBackStackEntryCount();
        if (backStackEntryCount == 0) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getResources().getString(R.string.exit_title))
                    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();

                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })

                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
