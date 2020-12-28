package com.example.italkapp.adapter;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.italkapp.fragment.LoginFragment;
import com.example.italkapp.fragment.RegisterFragment;

public class AdapterLoginAndRegister extends FragmentStatePagerAdapter {
    int numberTab = 2;


    public AdapterLoginAndRegister(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                LoginFragment loginFragment = new LoginFragment();
                return loginFragment;
            case 1:
                RegisterFragment registerFragment = new RegisterFragment();
                return registerFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return numberTab;
    }


}
