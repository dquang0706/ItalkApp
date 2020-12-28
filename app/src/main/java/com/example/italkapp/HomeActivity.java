package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.graphics.Color;
import android.media.MediaPlayer;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.italkapp.fragment.ChatListContainerFragment;

import com.example.italkapp.fragment.ChatOneUserFragment;
import com.example.italkapp.fragment.HomeFragment;
import com.example.italkapp.fragment.NotificationFragment;
import com.example.italkapp.fragment.ProfileFragment;
import com.example.italkapp.fragment.UsersFragment;

import com.example.italkapp.notification.Token;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    ImageView homeIv, profileIv, userIv, chatListIv, notificationIv;
    private FirebaseAuth firebaseAuth;
    String myUid;

    TextView profileTv, friendTv, chatTv, notificationTv, countNotificationTv;

    MediaPlayer player;

    LinearLayout profileBtn,friendBtn,chatBtn,notificationBtn;

    RelativeLayout homeLayout;
    private ConnectivityReceiver connectivityReceiver;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_home);

       ColorStatusbar.setColorStatusBar(HomeActivity.this);


        homeIv = findViewById(R.id.homeIv);
        profileIv = findViewById(R.id.profileIv);
        userIv = findViewById(R.id.userIv);
        chatListIv = findViewById(R.id.chatListIv);
        notificationIv = findViewById(R.id.notificationIv);
        profileTv = findViewById(R.id.profileTv);
        chatTv = findViewById(R.id.chatTv);
        friendTv = findViewById(R.id.friendTv);
        notificationTv = findViewById(R.id.notificationTv);
        countNotificationTv = findViewById(R.id.notificationCountTv);


        profileBtn = findViewById(R.id.profileBtn);
        friendBtn = findViewById(R.id.friendBtn);
        chatBtn = findViewById(R.id.chatBtn);
        notificationBtn = findViewById(R.id.notificationBtn);
        homeLayout = findViewById(R.id.homeLayout);

        firebaseAuth = FirebaseAuth.getInstance();


        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");
        if(firebaseAuth.getUid()!=null ){
            reference.child(firebaseAuth.getUid()).child("NotificationCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        countNotificationTv.setVisibility(View.GONE);
                    } else {
                        if (dataSnapshot.getValue().toString().equals("0")) {
                            countNotificationTv.setVisibility(View.GONE);
                        } else {
                            player = MediaPlayer.create(HomeActivity.this, R.raw.notifi);
                            player.start();
                            countNotificationTv.setText(dataSnapshot.getValue() + "");
                            countNotificationTv.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else {
            checkUserStatus();
        }


        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.frame_container, fragment1, "");
        ft1.commit();
        homeIv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                profileIv.setImageResource(R.drawable.ic_profile);
                userIv.setImageResource(R.drawable.ic_friend);
                chatListIv.setImageResource(R.drawable.ic_chat);
                notificationIv.setImageResource(R.drawable.ic_notification);

                profileTv.setTextColor(getColor(R.color.blackNhat2));
                friendTv.setTextColor(getColor(R.color.blackNhat2));
                chatTv.setTextColor(getColor(R.color.blackNhat2));
                notificationTv.setTextColor(getColor(R.color.blackNhat2));
                HomeFragment fragment1 = new HomeFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.frame_container, fragment1, "");
                ft1.commit();
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                profileIv.setImageResource(R.drawable.ic_profile_active);
                userIv.setImageResource(R.drawable.ic_friend);
                chatListIv.setImageResource(R.drawable.ic_chat);
                notificationIv.setImageResource(R.drawable.ic_notification);

                profileTv.setTextColor(getColor(R.color.main));

                friendTv.setTextColor(getColor(R.color.blackNhat2));
                chatTv.setTextColor(getColor(R.color.blackNhat2));
                notificationTv.setTextColor(getColor(R.color.blackNhat2));
                ProfileFragment fragment1 = new ProfileFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.frame_container, fragment1, "");
                ft1.commit();
            }
        });
        friendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                profileIv.setImageResource(R.drawable.ic_profile);
                userIv.setImageResource(R.drawable.ic_friend_active);
                chatListIv.setImageResource(R.drawable.ic_chat);
                notificationIv.setImageResource(R.drawable.ic_notification);

                profileTv.setTextColor(getColor(R.color.blackNhat2));
                friendTv.setTextColor(getColor(R.color.main));
                chatTv.setTextColor(getColor(R.color.blackNhat2));
                notificationTv.setTextColor(getColor(R.color.blackNhat2));
                UsersFragment fragment1 = new UsersFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.frame_container, fragment1, "");
                ft1.commit();
            }
        });
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                profileIv.setImageResource(R.drawable.ic_profile);
                userIv.setImageResource(R.drawable.ic_friend);
                chatListIv.setImageResource(R.drawable.ic_chat_active);
                notificationIv.setImageResource(R.drawable.ic_notification);


                profileTv.setTextColor(getColor(R.color.blackNhat2));
                friendTv.setTextColor(getColor(R.color.blackNhat2));
                chatTv.setTextColor(getColor(R.color.main));
                notificationTv.setTextColor(getColor(R.color.blackNhat2));
                ChatListContainerFragment fragment1 = new ChatListContainerFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.frame_container, fragment1, "");
                ft1.commit();
            }
        });
        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                countNotificationTv.setVisibility(View.GONE);
                reference.child(firebaseAuth.getUid()).child("NotificationCount").setValue("0");

                profileIv.setImageResource(R.drawable.ic_profile);
                userIv.setImageResource(R.drawable.ic_friend);
                chatListIv.setImageResource(R.drawable.ic_chat);
                notificationIv.setImageResource(R.drawable.ic_notification_active);

                profileTv.setTextColor(getColor(R.color.blackNhat2));
                friendTv.setTextColor(getColor(R.color.blackNhat2));
                chatTv.setTextColor(getColor(R.color.blackNhat2));
                notificationTv.setTextColor(getColor(R.color.main));
                NotificationFragment fragment1 = new NotificationFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.frame_container, fragment1, "");
                ft1.commit();
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // người dùng trạng thái đăng nhâp
            // set email of logged user
            myUid = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", myUid);
            editor.apply();

            updateToken(FirebaseInstanceId.getInstance().getToken());
        } else {
            // nếu user đã đăng xuất thì sẽ quay về home
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }
    private void checkOnlineStatus(String status) {
        if (firebaseAuth.getUid() == null) {
        } else {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("onlineStatus", status);
            //cap nhat trang thai cua user
            dbRef.updateChildren(hashMap);
        }

    }
    @Override
    protected void onStart() {
        loadLocale();

        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
    public  static void getAvatar(final Context context, final ImageView imageView){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            Query query = reference.orderByChild("uid").equalTo(firebaseAuth.getUid());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String image = "" + ds.child("image").getValue();
                        try {
                            if (!image.equals("")) {
                                Picasso.with(context).load(image).placeholder(R.drawable.avatar_default).into(imageView);
                            } else {
                                imageView.setImageResource(R.drawable.avatar_default);
                            }
                        } catch (Exception e) {
                            imageView.setImageResource(R.drawable.avatar_default);

                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(firebaseAuth.getUid()).setValue(mToken);
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
      Locale  myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
     getResources().updateConfiguration(config,  getResources().getDisplayMetrics());

    }

    @Override
    protected void onResume() {
        super.onResume();
        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, intentFilter);
        checkOnlineStatus("online");
        loadLocale();
    }

    public class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()){
            } else {
                Snackbar snackbar = Snackbar
                        .make(homeLayout, R.string.No_internet_connection, Snackbar.LENGTH_LONG)
                        .setAction(R.string.Go_settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        });
                // Changing action button text color
                snackbar.setActionTextColor(getResources().getColor(R.color.red));
                // Changing message text color
                View view = snackbar.getView();
                view.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimary));
                TextView textView = view.findViewById(R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        }
    }

}
