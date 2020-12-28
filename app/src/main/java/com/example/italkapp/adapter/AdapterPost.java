package com.example.italkapp.adapter;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.italkapp.AddPostActivity;
import com.example.italkapp.PostDetailActivity;
import com.example.italkapp.PostLikeByActivity;
import com.example.italkapp.R;
import com.example.italkapp.HisProfileActivity;
import com.example.italkapp.fragment.HomeFragment;
import com.example.italkapp.fragment.ProfileFragment;
import com.example.italkapp.model.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.ViewHolder> {
    Context context;
    List<ModelPost> postsList;
    int layout;
    String myUid;

    private DatabaseReference postsRef;

    SweetAlertDialog sd;
    public static int count = 0;
    FirebaseAuth firebaseAuth;
    boolean checkLike=false;

    public AdapterPost(Context context, int layout, List<ModelPost> postsList) {
        this.context = context;
        this.postsList = postsList;
        this.layout = layout;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = FirebaseAuth.getInstance().getUid();
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ModelPost modelPost = postsList.get(position);
        final String uid = modelPost.getUid();
        String uNameTv = modelPost.getuName();
        String uAvatar = modelPost.getuAvatar();
        final String pId = modelPost.getpId();
        final String pDescription = modelPost.getpDescr();
        final String pImage = modelPost.getpImage();
        String pTimeStamp = modelPost.getpTime();
        if (layout == R.layout.row_post) {
            sd = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            sd.setTitleText(R.string.Loading);
            sd.setCancelable(true);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
            String pTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", calendar).toString();

            holder.uNameTv.setText(uNameTv);
            holder.pTimeTv.setText(pTime);
            holder.pDescriptionTv.setText(pDescription);

//            setLiked(modelPost.getpId(), holder.likeIv);
            setLikes(holder,pId);
            setTextCoutLike(holder.pLikeTv, modelPost.getpId());
            setCommentCount(modelPost.getpId(), holder.pCommentTv);

            try {
                if (!uAvatar.equals("")) {
                    Picasso.with(context).load(uAvatar).placeholder(R.drawable.avatar_default).into(holder.uPictureIv);
                } else {
                    holder.uPictureIv.setImageResource(R.drawable.avatar_default);
                }
            } catch (Exception e) {
                holder.uPictureIv.setImageResource(R.drawable.avatar_default);
            }
            // nếu status không hình thì sẽ ấn imageview , ngược lại hiện
            if (pImage.equals("noImage")) {
                holder.pImageIv.setVisibility(View.GONE);
            } else {
                try {
                    Picasso.with(context).load(pImage).placeholder(R.drawable.ic_gallery_grey).into(holder.pImageIv);
                } catch (Exception e) {
                    holder.pImageIv.setImageResource(R.drawable.ic_gallery_grey);
                }
            }
            holder.commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);  // truyền bài đăng có id này qua PostDetailactivity để show dữu liệu của bài viết này
                    intent.putExtra("count", count);  // truyền bài đăng có id này qua PostDetailactivity để show dữu liệu của bài viết này
                    context.startActivity(intent);

                }
            });
            holder.seenByLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PostLikeByActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
            });

            holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkLike=true;
                    final String postId = postsList.get(position).getpId();
                    postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (checkLike) {
                                if (dataSnapshot.child(postId).child("Likes").hasChild(myUid)) {
                                    postsRef.child(postId).child("Likes").child(myUid).removeValue();
                                    checkLike = false;
                                } else {
                                    postsRef.child(postId).child("Likes").child(myUid).setValue(true);
                                    count++;
                                    addToNotifications(uid, pId, "like");
                                    checkLike = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
            // chuyển màn hình qua profile đã đăng post đã click
            holder.profileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // nếu là mã id trong bài đăng là của user đang đăng nhập thì chuyển qua profile chính user đó ngược lại chuyển qua progile user khác
                    if (myUid.equals(uid)) {
                        AppCompatActivity activity = (AppCompatActivity) v.getContext();
                        ProfileFragment profileFragment = new ProfileFragment();
                        Bundle bundle = new Bundle();
//                    bundle.putString("linktrang2", postsList.get(position).getpId());
                        profileFragment.setArguments(bundle);
                        activity.getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_container, profileFragment)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Intent intent = new Intent(context, HisProfileActivity.class);
                        intent.putExtra("hisUid", uid);
                        context.startActivity(intent);
                    }

                }
            });
            holder.moreBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    showMoreOption(holder.moreBtn, uid, myUid, pId, pDescription,pImage);
                }
            });
            holder.pImageIv.setOnLongClickListener(new View.OnLongClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public boolean onLongClick(View view) {
                    showOptionImage(modelPost.getpImage());
//                    showDialogDownLoadImage(modelPost.getpImage());
                    return false;
                }
            });
        }
        if (layout == R.layout.row_post_recent) {
            modelPost.getuName().trim();
            String[] name = modelPost.getuName().split("\\s+");

            holder.uNameTv.setText(name[name.length - 1]);
            try {
                Picasso.with(context).load(pImage).placeholder(R.drawable.ic_gallery_grey).into(holder.pImageIv);
            } catch (Exception e) {
                holder.pImageIv.setImageResource(R.drawable.ic_gallery_grey);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);  // truyền bài đăng có id này qua PostDetailactivity để show dữu liệu của bài viết này
                    intent.putExtra("count", count);  // truyền bài đăng có id này qua PostDetailactivity để show dữu liệu của bài viết này
                    context.startActivity(intent);
                }
            });
        }

    }

    private void showOptionImage(final String urlImage) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_click_image_post);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);
        LinearLayout downloadImageBtn=dialog.findViewById(R.id.downloadImageBnt);
        LinearLayout copyImageBtn=dialog.findViewById(R.id.copyimageBtn);
        LinearLayout cancelImageBtn=dialog.findViewById(R.id.cancelimageBtn);
        downloadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downLoadImage(urlImage);
                dialog.dismiss();
            }
        });
        copyImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("String", urlImage);
                clipboardManager.setPrimaryClip(clip);
                clip.getDescription();
                Toast.makeText(context, R.string.Copied, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        cancelImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setLikes(final ViewHolder holder, final String postKey) {
        postsRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).child("Likes").hasChild(myUid)) {
                    holder.likeIv.setImageResource(R.drawable.ic_liked);
                } else {
                    holder.likeIv.setImageResource(R.drawable.ic_like);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setTextCoutLike(final TextView textView, String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.child(postId).child("Likes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        textView.setText(dataSnapshot.getChildrenCount() + " ");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setCommentCount(String postId, final TextView commentCountTv) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(postId).child("Comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentCountTv.setText(dataSnapshot.getChildrenCount() + " " + context.getResources().getString(R.string.comment));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOption(final ImageButton moreBtn, final String uid, String myUid, final String pId, final String description, final String image) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        // chỉ trạng thái của mình mới show lên
        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.delete);
            popupMenu.getMenu().add(Menu.NONE, 1, 0, R.string.Edit);

        } else {
            popupMenu.getMenu().add(Menu.NONE, 3, 0, R.string.View_profile);
        }
        if (!description.equals("")) {
            popupMenu.getMenu().add(Menu.NONE, 2, 0, R.string.Copy_content);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    deletePost(pId,image);
                }
                if (id == 1) {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                if (id == 2) {
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("String", description);
                    clipboardManager.setPrimaryClip(clip);
                    clip.getDescription();
                    Toast.makeText(context, R.string.Copied, Toast.LENGTH_SHORT).show();
                }
                if (id == 3) {
                    Intent intent = new Intent(context, HisProfileActivity.class);
                    intent.putExtra("hisUid", uid);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();

    }

    private void addToNotifications(final String hisUid, String pId, String typeNotification) {
        if (!FirebaseAuth.getInstance().getUid().equals(hisUid)) {
            String timestamp = "" + System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("pId", pId);
            hashMap.put("timestamp", timestamp);
            hashMap.put("hisId", hisUid);
            hashMap.put("typeNotification", typeNotification);
            hashMap.put("myId", myUid);
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notifications");
            ref.child(hisUid).child(hisUid).child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
            ref.child(hisUid).child("NotificationCount").setValue(count + "");

            ref.child(hisUid).child("NotificationCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        count = 0;
                    } else {
                        count = Integer.parseInt(dataSnapshot.getValue() + "");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

    }
    private void deletePost(String pId, String pImage) {
        if (pImage.equals("noImage")) {
            // status không hình ảnh
            deleteNoImage(pId);
        } else {
            // status có hình ảnh
            deleteWithImage(pId, pImage);
        }
    }
    private void deleteNoImage(String pId) {
    sd.show();
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();
                    HomeFragment.adapterPost.notifyDataSetChanged();
                }
                Toast.makeText(context, context.getString(R.string.Delete_successful), Toast.LENGTH_SHORT).show();
                sd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sd.dismiss();
            }
        });

    }

    private void deleteWithImage(final String pId, String pImage) {
        sd.show();
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                            HomeFragment.adapterPost.notifyDataSetChanged();
                        }
                        Toast.makeText(context, context.getString(R.string.Delete_successful), Toast.LENGTH_SHORT).show();
                        sd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        sd.dismiss();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sd.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    public int getItemCount() {
        return postsList.size();
    }

    private void downLoadImage(String url) {
        Uri uri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.PAUSED_QUEUED_FOR_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("File download from iTakApp ");
        request.setDescription("Download using android");

        request.allowScanningByMediaScanner();
        request.setShowRunningNotification(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                "/images/" + "/" + "itakl" + ".png");
        request.setMimeType("*/*");
        downloadManager.enqueue(request);
    }
    private void showDialogDownLoadImage(final String url) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        TextView messagCfTv = dialog.findViewById(R.id.messagCfTv);
        TextView cancelTv = dialog.findViewById(R.id.cancelTv);
        TextView downloadTv = dialog.findViewById(R.id.updatetv);
        LinearLayout cancelBtn = dialog.findViewById(R.id.cancelBtn);
        LinearLayout downLoadBtn = dialog.findViewById(R.id.deleteBtn);
        downloadTv.setText(R.string.Download);
        cancelTv.setText(context.getResources().getString(R.string.cancel));
        downloadTv.setTextColor(context.getResources().getColor(R.color.blue));

        messagCfTv.setText(R.string.Are_you_want_to_download_this_image);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        downLoadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downLoadImage(url);
                dialog.dismiss();
            }
        });
        dialog.show();

    }
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView uPictureIv, pImageIv, likeIv;
        TextView uNameTv, pTimeTv, pDescriptionTv, pLikeTv, pCommentTv;
        ImageButton moreBtn;
        LinearLayout likeBtn, commentBtn, seenByLikeBtn;
        RelativeLayout profileLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            likeIv = itemView.findViewById(R.id.likeIv);
            pImageIv = itemView.findViewById(R.id.pImageIvPost);
            pCommentTv = itemView.findViewById(R.id.pCommentTv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikeTv = itemView.findViewById(R.id.pLikeTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            seenByLikeBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.reaHeader);
        }

    }
}
