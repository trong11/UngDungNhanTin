package com.example.baitaplonandroid.request;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baitaplonandroid.R;
import com.example.baitaplonandroid.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class RequestFragment extends Fragment {

    private RecyclerView rvRequests;
    private RequestAdapter adapter;
    private List<User> userList;
    private TextView txtEmptyRequestList;

    private DatabaseReference databaseReferenceRequests,databaseReferenceUsers;
    private FirebaseUser currentUser;
    private View progressBar;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRequests =(RecyclerView) view.findViewById(R.id.rvRequests);
        txtEmptyRequestList = view.findViewById(R.id.txtEmptyRequestList);

        progressBar = view.findViewById(R.id.progressBar);
        rvRequests.setLayoutManager(new LinearLayoutManager(getActivity()));
        userList = new ArrayList<>();

        adapter = new RequestAdapter(getActivity(),userList);
        rvRequests.setAdapter(adapter);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceUsers = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        databaseReferenceRequests = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Requests").child(currentUser.getUid());

        txtEmptyRequestList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        databaseReferenceRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                userList.clear();

                for(DataSnapshot ds : snapshot.getChildren()){
                    if(ds.exists()){
                        String requestStatus = ds.child("Request status").getValue().toString();
                        if(requestStatus.equals("received")){
                            String userId = ds.getKey();
                            databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String name = snapshot.child("name").getValue().toString();
                                    String photoName = snapshot.child("photo").getValue().toString();
                                    User user = new User(userId,name,photoName);
                                    userList.add(user);
                                    adapter.notifyDataSetChanged();
                                    txtEmptyRequestList.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_requests,error.getMessage()), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_requests,error.getMessage()), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}