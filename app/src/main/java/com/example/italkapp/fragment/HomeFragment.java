package com.example.italkapp.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.os.TraceCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.AddPostActivity;
import com.example.italkapp.R;
import com.example.italkapp.SearchActivity;
import com.example.italkapp.adapter.AdapterGroupChatList;
import com.example.italkapp.adapter.AdapterPost;
import com.example.italkapp.adapter.AdapterUser;
import com.example.italkapp.model.ModelGroupChatList;
import com.example.italkapp.model.ModelPost;
import com.example.italkapp.model.ModelUser;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
public class HomeFragment extends Fragment {
    View view;
    RelativeLayout statusBtn;
    ImageView actionIv, avatarIv, dayTimeIv;
    private ShimmerFrameLayout mShimmerViewContainer; // hiện khi chưa có dữ liệu

    FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    DatabaseReference userRef;
    RecyclerView recyclerView,recyclerViewPostHorizontal;
    List<ModelPost> postsList;
    List<ModelPost> postsListHorizontal;
    public static AdapterPost adapterPost;
    LinearLayout recyclerViewPostHorizontalLayout;
    RelativeLayout timeHelloLayout;

    TextView nameTv,helloTv;
    String name;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadLocale();
        view = inflater.inflate(R.layout.fragment_home, container, false);

        statusBtn = view.findViewById(R.id.statusBtn);
        actionIv = view.findViewById(R.id.actionIv);
        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        helloTv = view.findViewById(R.id.helloTv);
        dayTimeIv = view.findViewById(R.id.dayTimeIv);
        recyclerView = view.findViewById(R.id.recyclerViewPost);
        recyclerViewPostHorizontal = view.findViewById(R.id.recyclerViewPostHorizontal);
        recyclerViewPostHorizontalLayout = view.findViewById(R.id.recyclerViewPostHorizontalLayout);
        timeHelloLayout = view.findViewById(R.id.timeHelloLayout);
        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);

        postsList = new ArrayList<>();
        postsListHorizontal = new ArrayList<>();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();


        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AddPostActivity.class));
            }
        });

        actionIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        try {
            Query query = userRef.orderByChild("email").equalTo(user.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //Dựa vào key đã put lên lấy data rồi đổ vào các view
                        String image = "" + ds.child("image").getValue();
                        name = "" + ds.child("name").getValue();
                        nameTv.setText(" "+name);
                        try {
                            if (!image.equals("")) {
                                Picasso.with(getContext()).load(image).placeholder(R.drawable.avatar_default).into(avatarIv);
                            } else {
                                avatarIv.setImageResource(R.drawable.avatar_default);
                            }
                        } catch (Exception e) {
                            avatarIv.setImageResource(R.drawable.avatar_default);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            loadPost();
            setTime();
        }catch (Exception e){
            Log.d("Error",e.getMessage());
        }


        return view;
    }
    private void setTime(){
        String pTimeStamp=System.currentTimeMillis()+"";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", calendar).toString();

        pTime.trim();
        String[] nameSplit = pTime.split("\\s+");
        String last=nameSplit[nameSplit.length-1];
        String hour=nameSplit[nameSplit.length-2];

        char[] hourChar=hour.toCharArray();
        char hourFrist=hourChar[0];
        char hourSecond=hourChar[1];

        int numberHourFirst=Integer.parseInt(String.valueOf(hourSecond));
        int numberHourSum=Integer.parseInt(String.valueOf(hourFrist+hourSecond));
        Log.d("Time", "Time now: "+pTime+" \nHourse :"+hour +"\n"+" Lastime: "+ last+" \nHournumber"+ numberHourFirst );

       //english
        if(last.equals("AM")){
            dayTimeIv.setImageResource(R.drawable.ic_morning);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Morning) +getString(R.string.happy));
        }
        if(last.equals("PM")&& numberHourFirst<=5){
            dayTimeIv.setImageResource(R.drawable.ic_afternoon);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Afternoon)  + getString(R.string.happy));
        }
        if(last.equals("PM")&& numberHourFirst>5){
            dayTimeIv.setImageResource(R.drawable.ic_evening);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Evening)  + getString(R.string.happy));
        }
        if(last.equals("PM")&&numberHourSum>=10){
            dayTimeIv.setImageResource(R.drawable.ic_evening);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Evening) +getString(R.string.happy));
        }

        //vietnam
        if(last.equals("SA")){
            dayTimeIv.setImageResource(R.drawable.ic_morning);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Morning) +getString(R.string.happy));
        }
        if(last.equals("CH")&& numberHourFirst<=5){
            dayTimeIv.setImageResource(R.drawable.ic_afternoon);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Afternoon)  + getString(R.string.happy));
        }
        if(last.equals("CH")&& numberHourFirst>5){
            dayTimeIv.setImageResource(R.drawable.ic_evening);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Evening) +getString(R.string.happy));
        }
        

             //han quoc
        if(last.equals("오전")){
            dayTimeIv.setImageResource(R.drawable.ic_morning);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Morning) +getString(R.string.happy));
        }
        if(last.equals("오후")&& numberHourFirst<=5){
            dayTimeIv.setImageResource(R.drawable.ic_afternoon);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Afternoon)  + getString(R.string.happy));
        }
        if(last.equals("오후")&& numberHourFirst>5){
            dayTimeIv.setImageResource(R.drawable.ic_morning);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Morning) +getString(R.string.happy));
        }
        if(last.equals("오후")&&numberHourSum>=10){
            dayTimeIv.setImageResource(R.drawable.ic_evening);
            helloTv.setText(getString(R.string.May_this)+getString(R.string.Evening) +getString(R.string.happy));
        }


    }

    private void loadPost() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                postsListHorizontal.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postsList.add(modelPost);
                    if(!modelPost.getpImage().equals("noImage")){
                        postsListHorizontal.add(modelPost);
                        if(postsListHorizontal.size()==0){
                            recyclerViewPostHorizontalLayout.setVisibility(View.GONE);
                        }else{
                            recyclerViewPostHorizontalLayout.setVisibility(View.VISIBLE);
                            adapterPost = new AdapterPost(getContext(),R.layout.row_post_recent, postsListHorizontal);
                            recyclerViewPostHorizontal.setAdapter(adapterPost);
                        }

                    }
                    adapterPost = new AdapterPost(getContext(),R.layout.row_post, postsList);
                    recyclerView.setAdapter(adapterPost);
                    adapterPost.notifyDataSetChanged();
                }
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
                timeHelloLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
        loadLocale();
    }

    @Override
    public void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }

    public void saveLocale(String lang) {
        String langPref = "Language";
        SharedPreferences prefs = getActivity().getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }

    public void loadLocale() {
        String langPref = "Language";
        SharedPreferences prefs = getActivity().getSharedPreferences("CommonPrefs",
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
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
