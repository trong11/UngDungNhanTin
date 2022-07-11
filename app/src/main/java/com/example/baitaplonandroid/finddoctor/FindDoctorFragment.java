package com.example.baitaplonandroid.finddoctor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FindDoctorFragment extends Fragment {
    private RecyclerView rvFindDoctor;
    private FindDoctorAdapter findDoctorAdapter;
    private List<User> userList;
    private TextView txtEmptyDoctorList;

    private DatabaseReference databaseReference,databaseReferenceDoctorRequest;
    private FirebaseUser currentUser;
    private View progressBar;
    private SearchView searchDoctor;

    public FindDoctorFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_doctor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFindDoctor = view.findViewById(R.id.rvFindDoctors);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmptyDoctorList = view.findViewById(R.id.txtEmptyDoctorList);
        searchDoctor = view.findViewById(R.id.searchDoctor);
        searchDoctor.clearFocus();


        rvFindDoctor.setLayoutManager(new LinearLayoutManager(getActivity()));

        userList = new ArrayList<>();
        findDoctorAdapter = new FindDoctorAdapter(getActivity(),userList);
        rvFindDoctor.setAdapter(findDoctorAdapter);

        databaseReference = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceDoctorRequest = FirebaseDatabase.getInstance("https://baitaplonandroid-8036d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Requests").child(currentUser.getUid());

        txtEmptyDoctorList.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        Query query = databaseReference.orderByChild("name");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for(DataSnapshot ds : snapshot.getChildren()){
                    String userID = ds.getKey();

                    if(userID.equals(currentUser.getUid()))  return;

                    if(ds.child("name").getValue()!=null && ds.child("Role").getValue().toString().equals("Bacsi"))
                    {
                         String fullName = ds.child("name").getValue().toString();
                         String photoName = ds.child("photo").getValue().toString();

                         databaseReferenceDoctorRequest.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 if(snapshot.exists()){
                                     String requestStatus = snapshot.child("Request status").getValue().toString();
                                     if(requestStatus.equals("sent")){
                                         userList.add(new User(fullName,photoName,userID,true));
                                         findDoctorAdapter.notifyDataSetChanged();
                                     }
                                 }else{
                                     userList.add(new User(fullName,photoName,userID,false));
                                     findDoctorAdapter.notifyDataSetChanged();
                                 }
                             }
                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {
                                 progressBar.setVisibility(View.GONE);
                             }
                         });
                         txtEmptyDoctorList.setVisibility(View.GONE);
                         progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getContext().getString(R.string.failed_to_fetch_doctors,error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

        searchDoctor.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                filterList(text);
                return false;
            }
        });
    }
    private void filterList(String text) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList){
            if (user.getName().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(user);
            }
        }
        if (filteredList.isEmpty()){
            Toast.makeText(getActivity(), "Can't find doctor", Toast.LENGTH_SHORT).show();
        } else {
            findDoctorAdapter.setFilteredList(filteredList);
        }
    }
}