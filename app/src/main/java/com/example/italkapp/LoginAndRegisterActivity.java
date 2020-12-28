package com.example.italkapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.italkapp.adapter.AdapterLoginAndRegister;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.android.material.tabs.TabLayout;

import java.util.Locale;

public class LoginAndRegisterActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_login_and_register);
        ColorStatusbar.setColorStatusBar(LoginAndRegisterActivity.this);
        viewPager = findViewById(R.id.viewpager_Login_Register);
        tabLayout = findViewById(R.id.tablayout_Login__Register);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.LOGIN));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.REGISTER));

        AdapterLoginAndRegister adapter = new AdapterLoginAndRegister(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

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
    }

    public void saveLocale(String lang) {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }
    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        String language = prefs.getString(langPref, "");
        changeLang(language);
    }

    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        Locale myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getResources().updateConfiguration(config,  getResources().getDisplayMetrics());

    }

    @Override
    protected void onStart() {

        super.onStart();
    }
}
