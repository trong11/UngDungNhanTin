package com.example.baitaplonandroid.request;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder>{

    private Context context;
    private List<User> listUser;
    private DatabaseReference databaseReferenceRequest,databaseReferenceChats;
    private FirebaseUser currentUser;

    public RequestAdapter(Context context, List<User> listUser) {
        this.context = context;
        this.listUser = listUser;
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doctor_request_layout,parent,false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {
        final User user = listUser.get(position);

        holder.txtFullname.setText(user.getName());
        StorageReference fileRef = FirebaseStorage.getInstance("gs://baitaplonandroid-8036d.appspot.com").getReference().child("images"+"/"+user.getPhoto());

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri)
                        .placeholder(R.drawable.defaultpropic)
                        .error(R.drawable.defaultpropic)
                        .into(holder.ivProfile);
            }
        });

        databaseReferenceRequest = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Requests");
        databaseReferenceChats = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Chats");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.btnAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.pbDecision.setVisibility(View.VISIBLE);
                holder.btnDenyRequest.setVisibility(View.GONE);
                holder.btnAcceptRequest.setVisibility(View.GONE);

                final String userId = user.getId();
                databaseReferenceChats.child(currentUser.getUid()).child(userId)
                        .child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReferenceChats.child(userId).child(currentUser.getUid())
                                    .child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        databaseReferenceRequest.child(currentUser.getUid())
                                                .child(userId).child("Request status").setValue("accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    databaseReferenceRequest.child(userId).child(currentUser.getUid()).child("Request status").setValue("accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){

                                                                String title = "Consult Request Accepted";
                                                                String message = "Consult Request Accepted by: "+currentUser.getDisplayName();
                                                                Util.sendNotification(context,title,message,userId);

                                                                Toast.makeText(context, "Accept request successfully", Toast.LENGTH_SHORT).show();
                                                                holder.pbDecision.setVisibility(View.GONE);
                                                                holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                                                holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                                            }else{
                                                                handleException(holder, task.getException());
                                                            }
                                                        }
                                                    });
                                                }else{
                                                    handleException(holder, task.getException());
                                                }
                                            }
                                        });
                                    }else{
                                        handleException(holder,task.getException());
                                    }
                                }
                            });
                        }
                        else{
                           handleException(holder,task.getException());
                        }
                    }
                });
            }
        });

        holder.btnDenyRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.pbDecision.setVisibility(View.VISIBLE);
                holder.btnDenyRequest.setVisibility(View.GONE);
                holder.btnAcceptRequest.setVisibility(View.GONE);

                final String userId = user.getId();

                databaseReferenceRequest.child(currentUser.getUid()).child(userId).child("Request status").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReferenceRequest.child(userId).child(currentUser.getUid())
                                    .child("Request status").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(context,"Deny request successfully", Toast.LENGTH_SHORT).show();

                                        String title = "Consult Request Denied";
                                        String message = "Consult Request Denied By : "+currentUser.getDisplayName();
                                        Util.sendNotification(context,title,message,userId);

                                        holder.pbDecision.setVisibility(View.GONE);
                                        holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                    }else{
                                        Toast.makeText(context,  context.getString(R.string.failed_to_deny_request,task.getException()), Toast.LENGTH_SHORT).show();
                                        holder.pbDecision.setVisibility(View.GONE);
                                        holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(context,  context.getString(R.string.failed_to_deny_request,task.getException()), Toast.LENGTH_SHORT).show();
                            holder.pbDecision.setVisibility(View.GONE);
                            holder.btnDenyRequest.setVisibility(View.VISIBLE);
                            holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void handleException(RequestViewHolder holder,Exception exception) {
        Toast.makeText(context, context.getString(R.string.failed_to_accept_request,exception),Toast.LENGTH_SHORT).show();
        holder.pbDecision.setVisibility(View.GONE);
        holder.btnDenyRequest.setVisibility(View.VISIBLE);
        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return listUser.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder{

        private TextView txtFullname;
        private ImageView ivProfile;
        private Button btnAcceptRequest,btnDenyRequest;
        private ProgressBar pbDecision;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFullname = itemView.findViewById(R.id.txtFullName);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            btnAcceptRequest = itemView.findViewById(R.id.btnAcceptRequest);
            btnDenyRequest = itemView.findViewById(R.id.btnDenyRequest);
            pbDecision = itemView.findViewById(R.id.pbDecision);
        }
    }
}
