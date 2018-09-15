package com.cyclist.logic.firebase;

import android.util.Log;

import com.cyclist.logic.LogicManager;
import com.cyclist.logic.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.cyclist.logic.common.Constants.USERS_BUCKET;
import static com.facebook.login.widget.ProfilePictureView.TAG;

public class DBService{

    private final LogicManager logicManager;
    private FirebaseAuth mAuth;

    public DBService(LogicManager logicManager) {
        mAuth = FirebaseAuth.getInstance();
        this.logicManager = logicManager;
        getUser();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void save(Object obj, String bucketName){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(bucketName + "/" + mAuth.getUid());
        myRef.setValue(obj);
    }

    public void saveNewUser(final User userToSave){
        String uid = mAuth.getUid();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Query uidQuery = myRef.child(USERS_BUCKET).child(uid);
        uidQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user == null){
                    save(userToSave, USERS_BUCKET);
                    logicManager.setUser(userToSave);
                }
                else{
                    logicManager.setUser(user);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "failed", databaseError.toException());
            }
        });
    }

    public void getUser(){
        String uid = mAuth.getUid();
        if(uid != null) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            Query uidQuery = myRef.child(USERS_BUCKET).child(uid);
            uidQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        logicManager.setUser(user);
                    } else {
                        logicManager.setUser(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "failed", databaseError.toException());
                }
            });
        }
    }

    public FirebaseAuth getMAuth() {
        return mAuth;
    }
}
