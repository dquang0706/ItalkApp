package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.fragment.HomeFragment;
import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.suke.widget.SwitchButton;

import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingActivity extends AppCompatActivity {
    DatabaseReference reference;
    DatabaseReference postRef;
    DatabaseReference notificationRef;
    FirebaseAuth firebaseAuth;
    String uid;

    ImageView avatarIv;
    TextView nameTv;

    RelativeLayout logoutLayout, deletedUserLayout, changeNameLayout, changepasswordLayout, listBlockLayout, languageLayout, finishLayout;
    FirebaseUser user;

    SweetAlertDialog sd;

    SwitchButton switchNotification;

    private static final String TOPIC_POST_NOTIFICATION = "POST";

    Locale myLocale;
    String hisNotificationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_setting);
        ColorStatusbar.setColorStatusBar(SettingActivity.this);
        avatarIv = findViewById(R.id.avatarIv);
        nameTv = findViewById(R.id.nameTv);
        logoutLayout = findViewById(R.id.logoutLayout);
        deletedUserLayout = findViewById(R.id.deletedUserLayout);
        changeNameLayout = findViewById(R.id.changeNameLayout);
        listBlockLayout = findViewById(R.id.listBlockLayout);
        changepasswordLayout = findViewById(R.id.changepasswordLayout);
        switchNotification = findViewById(R.id.switchNotification);
        finishLayout = findViewById(R.id.finishLayout);
        languageLayout = findViewById(R.id.languageLayout);

        reference = FirebaseDatabase.getInstance().getReference("Users");
        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        notificationRef = FirebaseDatabase.getInstance().getReference("Notifications");
        firebaseAuth = FirebaseAuth.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
        user = firebaseAuth.getCurrentUser();

        sd = new SweetAlertDialog(SettingActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sd.setTitleText(R.string.Loading);
        sd.setCancelable(true);
    try {
        Query query = reference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();
                    String chatNotificationStatus = "" + ds.child("chatNotification").getValue();
                    try {
                        nameTv.setText(name);
                        if (!image.equals("")) {
                            Picasso.with(SettingActivity.this).load(image).placeholder(R.drawable.avatar_default).into(avatarIv);
                        } else {
                            avatarIv.setImageResource(R.drawable.avatar_default);
                        }
                    } catch (Exception e) {
                        avatarIv.setImageResource(R.drawable.avatar_default);

                    }
                    if(chatNotificationStatus.equals("enable")){
                        switchNotification.setChecked(true);
                    }
                    if(chatNotificationStatus.equals("disable")){
                        switchNotification.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        switchNotification.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked == true) {
                    updateNotification("enable");
                }
                if (isChecked == false) {
                    updateNotification("disable");
                }
            }
        });

        changeNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNameUpdateDialog("name");
            }
        });
        listBlockLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingActivity.this, ListBlockActivity.class));
            }
        });
        changepasswordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });
        languageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingLanguage();
            }
        });
        deletedUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser();
            }
        });
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(SettingActivity.this);
                dialog.setContentView(R.layout.dialog_confirm);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (dialog != null && dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                TextView confirmTv = dialog.findViewById(R.id.updatetv);
                TextView confirmMessage = dialog.findViewById(R.id.messagCfTv);
                TextView confirmTitle = dialog.findViewById(R.id.confirmTv);
                LinearLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);
                LinearLayout confirmBtn = dialog.findViewById(R.id.deleteBtn);
                confirmTitle.setText(R.string.Logout);
                confirmTv.setText(R.string.Confirm);
                confirmMessage.setText(R.string.Are_you_sure);

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        checkUserStatus();
                        checkOnlineStatus(timeStamp);
                        firebaseAuth.signOut();
                        startActivity(new Intent(SettingActivity.this, MainActivity.class));
                        finish();
                    }
                });

                dialog.show();

            }
        });
        finishLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingActivity.this, HomeActivity.class));
                finish();
            }
        });
        checkOnlineStatus("online");
    }catch (Exception e){
        e.getMessage();
    }


    }

    private void settingLanguage() {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.dialog_choose_language);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        RelativeLayout vietNamLayout = dialog.findViewById(R.id.vietNamLayout);
        RelativeLayout englishLayout = dialog.findViewById(R.id.enlishLayout);
        RelativeLayout koreaLayout = dialog.findViewById(R.id.KoreaLayout);
        RelativeLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);
        vietNamLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLocale("vi");
                Intent intent = getIntent();
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                dialog.dismiss();
            }
        });
        englishLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLocale("en");
                Intent intent = getIntent();
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                dialog.dismiss();
            }
        });
        koreaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLocale("ko");
                Intent intent = getIntent();
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showNameUpdateDialog(final String key) {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.dialog_edit_name);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        final EditText nameEt = dialog.findViewById(R.id.nameEt);
        LinearLayout updateBtn = dialog.findViewById(R.id.updateBtn);
        LinearLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = nameEt.getText().toString().trim();
                if (!TextUtils.isEmpty(name)) {
                    sd.show();
                    HashMap<String, Object> hasmapName = new HashMap<>();
                    hasmapName.put(key, name);
                    reference.child(user.getUid()).updateChildren(hasmapName).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // khi update tên  thì tên này sẽ sét lại cho tên có trong post của chính user này
                            updateUserNameInPost(name);
                            sd.dismiss();
                            dialog.dismiss();
                            Toast.makeText(SettingActivity.this, R.string.update_successful, Toast.LENGTH_SHORT).show();


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sd.dismiss();
                            dialog.dismiss();
                            Toast.makeText(SettingActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    dialog.dismiss();
                    Toast.makeText(SettingActivity.this, R.string.Enter_your_name, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();


    }

    private void updateUserNameInPost(final String value) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        final Query query = ref.orderByChild("uid").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey(); //lấy post id trong từng post
                    String po=dataSnapshot.getRef().child(child).toString();
                    Log.d("dataSnapshot.getRef","dataSnapshot.getRef: "+ po);
//                    https://italkapp-db573.firebaseio.com/Posts/1596687502209
                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                    /// Cập nhật lại tên những bài đăng đã comment
                    final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts").child(child).child("Comments");
                    Query query1=reference.orderByChild("uid").equalTo(uid);
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String child = ds.getKey();
                                Log.d("reference",child);
                                reference.child(child).child("uName").setValue(value);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showChangePasswordDialog() {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.dialog_changes_password);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        final EditText oldPasswordEt = dialog.findViewById(R.id.oldpasswordEt);
        final EditText newPasswordEt = dialog.findViewById(R.id.newpasswordEt);
        LinearLayout updateBtn = dialog.findViewById(R.id.updateBtn);
        LinearLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String oldPassWord = oldPasswordEt.getText().toString();
                final String newPassword = newPasswordEt.getText().toString();
                if (TextUtils.isEmpty(oldPassWord) && TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(SettingActivity.this, R.string.Password_must_not_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length() < 6) {
                    Toast.makeText(SettingActivity.this, R.string.Password_is_at_least_6_characters, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    updatePassword(oldPassWord, newPassword);
                    dialog.dismiss();
                }

            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sd.dismiss();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updatePassword(String oldPassword, final String newldPassword) {
        sd.show();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                user.updatePassword(String.valueOf(newldPassword)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingActivity.this, R.string.update_successful, Toast.LENGTH_SHORT).show();
                        sd.dismiss();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                                sd.dismiss();
                            }
                        });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        sd.dismiss();
                        Toast.makeText(SettingActivity.this, R.string.Error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteUser() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm_password);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        final EditText passwordEt = dialog.findViewById(R.id.nameEt);
        LinearLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);
        LinearLayout confirmBtn = dialog.findViewById(R.id.updateBtn);


        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sd.show();
                String password = passwordEt.getText().toString().trim();
                if (password.isEmpty()) {
                    passwordEt.setError(getString(R.string.enter_your_password));
                    sd.dismiss();
                    return;
                } else {
                    sd.show();
                    AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), passwordEt.getText().toString().trim());
                    user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sd.dismiss();
                            deleteYourAccount();
                            dialog.dismiss();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    sd.dismiss();
                                    Toast.makeText(SettingActivity.this, R.string.Incorrect, Toast.LENGTH_LONG).show();
                                }
                            });
                }


            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    public void deleteYourAccount() {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView confirmTv = dialog.findViewById(R.id.updatetv);
        TextView confirmMessage = dialog.findViewById(R.id.messagCfTv);
        TextView confirmTitle = dialog.findViewById(R.id.confirmTv);
        LinearLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);
        LinearLayout confirmBtn = dialog.findViewById(R.id.deleteBtn);
        confirmTitle.setText(R.string.Delete);
        confirmTv.setText(R.string.Confirm);
        confirmMessage.setText(R.string.Are_you_sure);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sd.show();
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            reference.child(user.getUid()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("deleteUser", "Thanh cong");
                                            sd.dismiss();
                                            dialog.dismiss();
                                            startActivity(new Intent(SettingActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            sd.dismiss();
                                        }
                                    });


                            Query query=postRef.orderByChild("uid").equalTo(user.getUid());
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                                        ds.getRef().removeValue();
                                        HomeFragment.adapterPost.notifyDataSetChanged();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                sd.dismiss();
                            }
                        });
            }
        });
        dialog.show();
    }

    private void checkUserStatus() {
        if (user != null) {
            uid = user.getUid();
        } else {
            startActivity(new Intent(SettingActivity.this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status) {
        if (uid == null) {
            startActivity(new Intent(SettingActivity.this,MainActivity.class));
        } else {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("onlineStatus", status);
            //cap nhat trang thai cua user
            dbRef.updateChildren(hashMap);
        }

    }
    public void updateNotification(String notificationStatus) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("chatNotification", notificationStatus);
        //cap nhat trang thai cua user
        dbRef.updateChildren(hashMap);
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
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

    }



}
