package com.cyclist.logic.firebase;

import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import lombok.Getter;

public class DBService{

    @Getter
    private FirebaseAuth mAuth;
    private static DBService instance;

    public static DBService getInstance() {
        if (instance == null){
            instance = new DBService();
        }
        return instance;
    }

    private DBService(){
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void save(Object obj, String bucketName){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(bucketName + "/" + mAuth.getUid());

        myRef.setValue(obj);
    }
}
