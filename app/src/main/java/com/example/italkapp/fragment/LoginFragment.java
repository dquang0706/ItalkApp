package com.example.italkapp.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.HomeActivity;
import com.example.italkapp.R;
import com.example.italkapp.SettingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    Button loginBtn;
    EditText mEmailEt, mPasswordEt;
    View view;
    SweetAlertDialog sd;
    FrameLayout languageBtn;


    FirebaseAuth mAuth;
  Locale myLocale;

  LinearLayout recoverBtn;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
          loadLocale();
        view = inflater.inflate(R.layout.fragment_login, container, false);


        mAuth = FirebaseAuth.getInstance();
        loginBtn = view.findViewById(R.id.loginBtn);
        mEmailEt = view.findViewById(R.id.emailEt);
        mPasswordEt = view.findViewById(R.id.passwordEt);
        languageBtn = view.findViewById(R.id.languageBtn);
        recoverBtn = view.findViewById(R.id.recoverBtn);

        sd = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        sd.setTitleText(getString(R.string.Loading));
        sd.setCancelable(false);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEt.getText().toString();
                String password = mPasswordEt.getText().toString();
                String passwordCf = mPasswordEt.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    // email phải đúng định dạng
                    mEmailEt.setError(getString(R.string.Emai_must_right_the_format));
                    mEmailEt.setFocusable(true);
                    sd.dismiss();
                } else if (email.isEmpty()) {
                    mEmailEt.setError(getString(R.string.Email_cannot_be_empty));
                    mEmailEt.setFocusable(true);
                    sd.dismiss();
                } else if (password.length() < 6) {
                    //mật khẩu ít nhất 6 ký tự
                    mPasswordEt.setError(getString(R.string.Password_is_at_least_6_characters));
                    mPasswordEt.setFocusable(true);
                    sd.dismiss();
                }
                else {
                    try {
                        loginWithEmailPassword(email, password);

                    }catch (Exception e){
                        Log.d("Error",e.getMessage().toString());
                    }

                }
            }
        });

        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogEnterEmail();
            }
        });
        languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getContext());
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
                        Intent intent = getActivity().getIntent();
                        getActivity().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        getActivity().finish();
                        dialog.dismiss();
                    }
                });
                englishLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveLocale("en");
                        Intent intent = getActivity().getIntent();
                        getActivity().startActivity(intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK));
                        getActivity().finish();
                        dialog.dismiss();
                    }
                });
                koreaLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveLocale("ko");
                        Intent intent = getActivity().getIntent();
                        getActivity().startActivity(intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK));
                        getActivity().finish();
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
        });


        return view;
    }

    private void showDialogEnterEmail() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_edit_name);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        final EditText emailEt = dialog.findViewById(R.id.nameEt);
        TextView recoverTv=dialog.findViewById(R.id.updatetv);
        TextView mnametv=dialog.findViewById(R.id.mnametv);
        LinearLayout recoverBtn = dialog.findViewById(R.id.updateBtn);
        LinearLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);
        emailEt.setHint(getContext().getResources().getString(R.string.enter_your_email));
        mnametv.setText(R.string.Send_email);
        recoverTv.setText(R.string.Recover);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEt.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(getActivity(), getContext().getResources().getString(R.string.enter_your_email), Toast.LENGTH_SHORT).show();
                } else {
                    beginRecovery(email);
                }
            }
        });
        dialog.show();
    }

    private void beginRecovery(String email) {
        SweetAlertDialog sweetAlertDialog  = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText(getString(R.string.Sending_your_email));
        sweetAlertDialog.setCancelable(false);

        sd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sd.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.Please_check_your_mail, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.Email_does_not_exist, Toast.LENGTH_SHORT).show();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sd.dismiss();
                Toast.makeText(getContext(), R.string.Error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithEmailPassword(String email, String password) {
        sd.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            sd.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            sd.setTitleText(getString(R.string.good_job));
                            sd.setContentText(getString(R.string.You_clicked_button));

                            // Khỏi tạo referen
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("onlineStatus", "online");
                            //CẬp nhật lại trạng thái online của người dùng khi đăng nhập
                            dbRef.updateChildren(hashMap);
                            sd.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    startActivity(new Intent(getContext(), HomeActivity.class));
                                    getActivity().finish();
                                }
                            });

                        } else {
                            progressError(getString(R.string.Authentication_failed));
                            sd.dismiss();

                        }

                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sd.dismiss();
            }
        });
    }

    private void progressError(String message) {
        sd.dismiss();
        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.Error))
                .setContentText(message)
                .show();
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
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getActivity().getResources().updateConfiguration(config,  getActivity().getResources().getDisplayMetrics());

    }
}
