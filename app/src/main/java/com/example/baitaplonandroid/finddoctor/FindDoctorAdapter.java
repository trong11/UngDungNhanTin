package com.example.baitaplonandroid.finddoctor;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.baitaplonandroid.R;
import com.example.baitaplonandroid.Util;
import com.example.baitaplonandroid.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FindDoctorAdapter extends RecyclerView.Adapter<FindDoctorAdapter.FindDoctorViewHolder>{

    private Context context;
    private List<User> userList;

    private DatabaseReference doctorRequestDatabse;
    private FirebaseUser currentUser;
    private String userId;

    public FindDoctorAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void setFilteredList(List<User> filteredList){
        this.userList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FindDoctorAdapter.FindDoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.find_doctor_layout,parent,false);
        return new FindDoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindDoctorAdapter.FindDoctorViewHolder holder, int position) {
        User user = userList.get(position);

        holder.txtFullName.setText(user.getName());
        StorageReference fileRef = FirebaseStorage.getInstance("gs://baitaplonandroid-8036d.appspot.com").getReference().child("images/"+user.getPhoto());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri)
                        .placeholder(R.drawable.defaultpropic)
                        .error(R.drawable.defaultpropic)
                        .into(holder.ivprofile);
            }
        });

        doctorRequestDatabse = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Requests");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(user.isRequestSent()){
            holder.btnSendRequest.setVisibility(View.GONE);
            holder.btnCancelRequest.setVisibility(View.VISIBLE);
        }else{
            holder.btnSendRequest.setVisibility(View.VISIBLE);
            holder.btnCancelRequest.setVisibility(View.GONE);
        }

        holder.btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.btnSendRequest.setVisibility(View.GONE);
                holder.pbRequest.setVisibility(View.VISIBLE);

                userId = user.getId();

                doctorRequestDatabse.child(currentUser.getUid()).child(userId).child("Request status")
                        .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            doctorRequestDatabse.child(userId).child(currentUser.getUid()).child("Request status")
                                    .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(context, "Request sent successfully", Toast.LENGTH_SHORT).show();
                                        String title = "New Consult Request";
                                        String message = "Consult Request From : "+currentUser.getDisplayName();
                                        Util.sendNotification(context,title,message,userId);

                                        holder.btnSendRequest.setVisibility(View.GONE);
                                        holder.pbRequest.setVisibility(View.GONE);
                                        holder.btnCancelRequest.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        Toast.makeText(context,context.getString( R.string.failed_to_send_request,task.getException()), Toast.LENGTH_SHORT).show();
                                        holder.btnSendRequest.setVisibility(View.VISIBLE);
                                        holder.pbRequest.setVisibility(View.GONE);
                                        holder.btnCancelRequest.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(context,context.getString( R.string.failed_to_send_request,task.getException()), Toast.LENGTH_SHORT).show();
                            holder.btnSendRequest.setVisibility(View.VISIBLE);
                            holder.pbRequest.setVisibility(View.GONE);
                            holder.btnCancelRequest.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        holder.btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.btnCancelRequest.setVisibility(View.GONE);
                holder.pbRequest.setVisibility(View.VISIBLE);

                userId = user.getId();

                doctorRequestDatabse.child(currentUser.getUid()).child(userId).child("Request status")
                        .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            doctorRequestDatabse.child(userId).child(currentUser.getUid()).child("Request status")
                                    .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(context, "Request cancel successfully", Toast.LENGTH_SHORT).show();
                                        holder.btnSendRequest.setVisibility(View.VISIBLE);
                                        holder.pbRequest.setVisibility(View.GONE);
                                        holder.btnCancelRequest.setVisibility(View.GONE);
                                    }
                                    else{
                                        Toast.makeText(context,context.getString( R.string.failed_to_cancel_request,task.getException()), Toast.LENGTH_SHORT).show();
                                        holder.btnSendRequest.setVisibility(View.GONE);
                                        holder.pbRequest.setVisibility(View.GONE);
                                        holder.btnCancelRequest.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(context,context.getString( R.string.failed_to_cancel_request,task.getException()), Toast.LENGTH_SHORT).show();
                            holder.btnSendRequest.setVisibility(View.GONE);
                            holder.pbRequest.setVisibility(View.GONE);
                            holder.btnCancelRequest.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class FindDoctorViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivprofile;
        private TextView txtFullName;
        private Button btnSendRequest,btnCancelRequest;
        private ProgressBar pbRequest;



        public FindDoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivprofile = itemView.findViewById(R.id.ivProfile);
            txtFullName = itemView.findViewById(R.id.txtFullName);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);
            btnCancelRequest = itemView.findViewById(R.id.btnCancelRequest);
            pbRequest = itemView.findViewById(R.id.pbRequest);
        }
    }
}
