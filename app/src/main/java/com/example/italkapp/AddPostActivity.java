package com.example.italkapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.italkapp.statusbar.ColorStatusbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    LinearLayout linearAnh;

    TextView titleAciton, pubLishPostBtn;

    EditText descriptionEt;
    ImageView imageIv, backIv,avatarIv;

    String name, email, uid, avatar;
    String edtDesciption, edtImage;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;


    private String[] cameraPermission;
    private String[] storagePermission;
    Uri image_uri = null;

    SweetAlertDialog sd;

    RelativeLayout postLayout;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        ColorStatusbar.setColorStatusBar(AddPostActivity.this);

        descriptionEt = findViewById(R.id.pDescription);
        imageIv = findViewById(R.id.pImageIv);
        linearAnh = findViewById(R.id.linearAnh);
        titleAciton = findViewById(R.id.titleTv);
        backIv = findViewById(R.id.backIv);
        avatarIv = findViewById(R.id.avatarIv);
        pubLishPostBtn = findViewById(R.id.pubLishPostBtn);
        postLayout = findViewById(R.id.postLayout);
        sd = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sd.setTitleText(getString(R.string.Loading));
        sd.setCancelable(false);

        HomeActivity.getAvatar(AddPostActivity.this,avatarIv);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        final Intent intent = getIntent();
        final String isUpdateKey = "" + intent.getStringExtra("key");
        final String edtitPostsId = "" + intent.getStringExtra("editPostId");
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        try {
            checkUserStatus();
            backIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            //lay du lieu tu AdapterPost cần sửa intent qua
            if (isUpdateKey.equals("editPost")) {
                titleAciton.setText("Edit status");
                pubLishPostBtn.setText(R.string.Edit);
                loadPostData(edtitPostsId);
            } else {
                titleAciton.setText("Upload status");
                pubLishPostBtn.setText(R.string.Post);
            }

            Query query = userDbRef.orderByChild("email").equalTo(email);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        name = "" + ds.child("name").getValue();
                        email = "" + ds.child("email").getValue();
                        avatar = "" + ds.child("image").getValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            linearAnh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showImagePickDialog();
                }
            });
            postLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String description = descriptionEt.getText().toString().trim();
                    if (isUpdateKey.equals("editPost")) {
                        UpdatePost(description, edtitPostsId);
                    } else {
                        if (imageIv.getDrawable() != null && description.equals("")) {
                            description = "";
                        }
                        if (imageIv.getDrawable() == null && description.equals("")) {
                            Toast.makeText(AddPostActivity.this, R.string.Please_enter_the_article_description_or_image, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        upLoadData(description);
                    }
                }
            });
            checkOnlineStatus("online");
        }catch (Exception e){
            Log.d("e",e.getMessage());
        }


    }

    private void upLoadData(final String desciption) {
        sd.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post_" + timeStamp;
        if (imageIv.getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);
            ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String downloadUri = uriTask.getResult().toString();
                    if (uriTask.isSuccessful()) {
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uAvatar", avatar);
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pDescr", desciption);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime", timeStamp);


                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sd.dismiss();
//                                Toast.makeText(AddPostActivity.this, R.string.Post_successful, Toast.LENGTH_SHORT).show();
                                descriptionEt.setText("");
                                imageIv.setImageURI(null);
                                image_uri = null;
                                imageIv.setVisibility(View.GONE);
                                startActivity(new Intent(AddPostActivity.this,HomeActivity.class));
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                sd.dismiss();
                                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    sd.dismiss();
                }
            });
        } else {
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uAvatar", avatar);
            hashMap.put("pId", timeStamp);
            hashMap.put("pDescr", desciption);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sd.dismiss();
//                    Toast.makeText(AddPostActivity.this, R.string.Post_successful, Toast.LENGTH_SHORT).show();
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_uri = null;
                    imageIv.setVisibility(View.GONE);
                    startActivity(new Intent(AddPostActivity.this,HomeActivity.class));
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sd.dismiss();
                    Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void loadPostData(String edtitPostsId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query fQuery = reference.orderByChild("pId").equalTo(edtitPostsId);
        fQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    edtDesciption = "" + ds.child("pDescr").getValue();
                    edtImage = "" + ds.child("pImage").getValue();
                    descriptionEt.setText(edtDesciption);
                    if (!edtImage.equals("noImage")) {
                        try {
                            imageIv.setVisibility(View.VISIBLE);
                            Picasso.with(AddPostActivity.this).load(edtImage).into(imageIv);
                        } catch (Exception e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void UpdatePost(String desciption, String edtitPostsId) {
        sd.show();
        if (!edtImage.equals("noImage")) {
            updateNoImage(desciption, edtitPostsId);
        } else if (imageIv.getDrawable() != null) {
            updateWithImage(desciption, edtitPostsId);
        } else {
            updateWithAgainNoImage(desciption, edtitPostsId);
        }
    }

    private void updateNoImage(final String desciption, final String edtitPostsId) {
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(edtImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/" + "post_" + timestamp;

                Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uAvatar", avatar);
                            hashMap.put("pDescr", desciption);
                            hashMap.put("pImage", downloadUri);
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(edtitPostsId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sd.dismiss();
                                    Toast.makeText(AddPostActivity.this, R.string.update_successful, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            sd.dismiss();
                                            Toast.makeText(AddPostActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        sd.dismiss();
                        Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void updateWithImage(final String desciption, final String edtitPostsId) {
        String timstamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timstamp;

        Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("uid", uid);
                    hashMap.put("uName", name);
                    hashMap.put("uEmail", email);
                    hashMap.put("uAvatar", avatar);
                    hashMap.put("pDescr", desciption);
                    hashMap.put("pImage", downloadUri);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    ref.child(edtitPostsId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sd.dismiss();
                            Toast.makeText(AddPostActivity.this, getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    sd.dismiss();
                                    Toast.makeText(AddPostActivity.this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sd.dismiss();
                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithAgainNoImage(String desciption, String edtitPostsId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uAvatar", avatar);
        hashMap.put("pDescr", desciption);
        hashMap.put("pImage", "noImage");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(edtitPostsId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sd.dismiss();
                Toast.makeText(AddPostActivity.this, R.string.update_successful, Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        sd.dismiss();
                        Toast.makeText(AddPostActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImagePickDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                this, R.style.BottomSheetDialogTheme
        );

        View bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.dialog_choose_image,
                (LinearLayout) bottomSheetDialog.findViewById(R.id.bottomSheetContainer)
        );
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 0);
        bottomSheetView.setLayoutParams(params);

        bottomSheetDialog.setCancelable(false);
        bottomSheetView.findViewById(R.id.cameraBtn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.galleryBtn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                if (!checkStoragePermission()) {
                    requestStoragetPermission();
                } else {
                    pickFromGallery();
                }
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(AddPostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragetPermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(AddPostActivity.this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(AddPostActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_uri = AddPostActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccept = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccept && storageAccept) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(AddPostActivity.this, R.string.Please_enable_camera_permissions, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccpted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccpted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(AddPostActivity.this, R.string.Please_enable_gallery_permissions, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                imageIv.setImageURI(image_uri);
                imageIv.setVisibility(View.VISIBLE);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                imageIv.setImageURI(image_uri);
                imageIv.setVisibility(View.VISIBLE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // người dùng trạng thái đăng nhâp
            // set email of logged user
            email = user.getEmail();
            uid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
    @Override
    protected void onPause() {
        String timeStamp =""+System.currentTimeMillis();
        checkOnlineStatus(timeStamp);
        super.onPause();
    }

    public void checkOnlineStatus(String status) {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //cap nhat trang thai cua user
        dbRef.updateChildren(hashMap);


    }
}
