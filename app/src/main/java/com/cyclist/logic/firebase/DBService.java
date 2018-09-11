package com.cyclist.logic.firebase;

import android.util.Log;

import com.cyclist.logic.user.User;
import com.cyclist.logic.user.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.facebook.login.widget.ProfilePictureView.TAG;

public class DBService{

    private static DBService instance;

    private FirebaseAuth mAuth;

    private DBService() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static DBService getInstance() {
        if (instance == null){
            instance = new DBService();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void save(Object obj, String bucketName){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(bucketName + "/" + mAuth.getUid());
        myRef.setValue(obj);
    }

    public void saveNewUser(final User user){
        String uid = mAuth.getUid();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Query uidQuery = myRef.child(UserService.USERS_BUCKET).child(uid);
        uidQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    save(user, UserService.USERS_BUCKET);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "failed", databaseError.toException());
            }
        });


//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference(bucketName + "/" + mAuth.getUid());
//        myRef.setValue(user);
    }

    public FirebaseAuth getMAuth() {
        return mAuth;
    }
}
