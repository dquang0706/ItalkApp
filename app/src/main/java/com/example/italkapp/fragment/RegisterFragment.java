package com.example.italkapp.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.HomeActivity;
import com.example.italkapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class RegisterFragment extends Fragment {
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    EditText mEmailEt, mnameEt, mPasswordEt, mcfPasswordEt;
    Button mRegisterBtn;

    SweetAlertDialog sd;

    private FirebaseAuth mAuth;


    View view;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_register, container, false);
        mEmailEt = view.findViewById(R.id.emailEt);
        mPasswordEt = view.findViewById(R.id.passwordEt);
        mcfPasswordEt = view.findViewById(R.id.cfpasswordEt);
        mRegisterBtn = view.findViewById(R.id.registerBtn);
        mnameEt = view.findViewById(R.id.nameEt);
        //
        mAuth = FirebaseAuth.getInstance();

        sd = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
//        sd.getProgressHelper().setBarColor(Color.parseColor("#0099ff"));
        sd.setTitleText("Loading");
        sd.setCancelable(false);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEt.getText().toString();
                String name = mnameEt.getText().toString();
                String password = mPasswordEt.getText().toString().trim();
                String cfpassword = mcfPasswordEt.getText().toString().trim();
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
                if (name.isEmpty()) {
                    mnameEt.setError(getString(R.string.name_not_be_emty));
                    mnameEt.setFocusable(true);
                } else if (!matcher.matches()) {
                    // email phải đúng định dạng
                    mEmailEt.setError(getString(R.string.Emai_must_right_the_format));
                    mEmailEt.setFocusable(true);
                } else if (password.length() < 6) {
                    //mật khẩu ít nhất 6 ký tự
                    mPasswordEt.setError(getString(R.string.Password_is_at_least_6_characters));
                    mPasswordEt.setFocusable(true);
                }else if(name.length()<6){
                    mnameEt.setError(getString(R.string.Name_must_be_greater_than_6_characters));
                    mnameEt.setFocusable(true);
                }
                else if(name.matches((".*[0-9].*"))){
                    mnameEt.setError(getString(R.string.Name_must_be_characters));
                    mnameEt.setFocusable(true);
                }

                else if(!password.equals(cfpassword)){
                    // Mật khẩu xác nhận không chính xác
                    mcfPasswordEt.setError(getString(R.string.password_is_incorrect));
                    mcfPasswordEt.setFocusable(true);
                }
                else {
                    try {
                        registerUser(name, email, password);

                    }catch (Exception e){
                        e.getMessage();
                    }
                }
            }
        });


        return view;
    }

    private void registerUser(final String name, String email, String password) {
        sd.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sd.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            sd.setTitleText(getString(R.string.good_job));
                            sd.setContentText(getString(R.string.You_clicked_button));
                            // Khỏi tạo firebaseuser
                            final FirebaseUser user = mAuth.getCurrentUser();
                            // lấy email đã đăng ký
                            String email = user.getEmail();
                            // lấy id đã đăng ký
                            String uid = user.getUid();

                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("image", ""); //sẽ update trong sửa thông tin
                            hashMap.put("cover", ""); //sẽ update trong sửa thông tin
                            hashMap.put("chatNotification", "enable"); //sẽ update trong sửa thông tin

                            // Khai báo firebase
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            // put dữ liệu lên Firebase bằng hashmap
                            reference.child(uid).setValue(hashMap);
                            // bắt sự kiện click button confirm của sweetdialog
                            sd.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    startActivity(new Intent(getContext(), HomeActivity.class));
                                    getActivity().finish();
                                }
                            });

                        } else {
                            progressError(getString(R.string.Authentication_failed));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void progressError(String message) {
        sd.dismiss();
        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
//                .setConfirmButtonBackgroundColor(getResources().getColor(R.color.blue))
                .setTitleText(getString(R.string.Error))
                .setContentText(message)
                .show();
    }

}
