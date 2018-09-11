package com.cyclist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.user.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static android.content.ContentValues.TAG;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;

    private LogicManager logicManager = LogicManager.getInstance();
    private EditText username;
    private EditText password;
    private LoginButton facebookLoginButton;
    private CallbackManager facebookCallbackManager;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singin);
        initializeComponents();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser;
        if ((currentUser = logicManager.getCurrentUser()) != null) {
            goToMainActivity(currentUser);
        }
        else{
            checkFacebookLoginStatus();
        }
    }

    private void initializeComponents() {
        username = findViewById(R.id.usernameEditText);
        password = findViewById(R.id.passwordEditText);
        Button signUpButton = findViewById(R.id.signUpButton);
        Button signInButton = findViewById(R.id.signInButton);
        facebookLoginButton = findViewById(R.id.facebook_login_button);
        username.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                onTextEditFocus(username, hasFocus);
            }
        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                onTextEditFocus(password, hasFocus);
            }
        });
        initializeFacebookSingIn();
        initializeGoogleSingIn();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInButtonClick(v);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpButtonClick(v);
            }
        });
    }

    private void onSignUpButtonClick(View v) {
        Intent signUpIntent = new Intent(SignIn.this, SignUp.class);
        startActivity(signUpIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void onTextEditFocus(EditText editText, boolean hasFocus) {
        if(hasFocus || !editText.getText().toString().isEmpty()){
            editText.setAlpha(1);
        }
        else{
            editText.setAlpha(0.5f);
        }
    }

    public void signIn(final String email, final String password) {
        logicManager.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignIn.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            goToMainActivity(logicManager.getAuth().getCurrentUser());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignIn.this, getResources().getString(R.string.error_sign_in_failed),
                                    Toast.LENGTH_SHORT).show();
                            updateFailure("");
                        }
                    }
                });
    }

    private void updateFailure(String errorMsg) {

    }

    private void goToMainActivity(FirebaseUser user) {
        Intent mainActivityIntent = new Intent(SignIn.this, MainActivity.class);
        startActivity(mainActivityIntent);
    }

    public void onSignInButtonClick(View view) {
        if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
            signIn(username.getText().toString(), password.getText().toString());
        }
    }

    private User createUser(){
        FirebaseUser firebaseUser = logicManager.getCurrentUser();
        User user = new User();
        user.setEmail(firebaseUser.getEmail());
        user.setFName(firebaseUser.getDisplayName());
        return user;

    }

/******************************************* GOOGLE *******************************************/

    @Override
    public void onClick(View v) {
        signIn();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateFailure("");
                // [END_EXCLUDE]
            }
        }
        else{
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkGoogleLoginStatus() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            firebaseAuthWithGoogle(account);
        }
    }

    private void initializeGoogleSingIn() {
        findViewById(R.id.google_login_button).setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        logicManager.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                           logicManager.saveUser(createUser());
                            goToMainActivity(logicManager.getAuth().getCurrentUser());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateFailure("");
                        }
                    }
                });
    }


    /******************************************* FACEBOOK *******************************************/

    private void checkFacebookLoginStatus() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            handleFacebookAccessToken(accessToken);
        }
        else {
            checkGoogleLoginStatus();
        }

    }

    private void initializeFacebookSingIn() {
        facebookCallbackManager = CallbackManager.Factory.create();
        setFacebookCallbackManager();
    }


    private void setFacebookCallbackManager() {
        facebookLoginButton.setReadPermissions("public_profile", "email", "user_birthday");
        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                updateFailure("");
            }

            @Override
            public void onError(FacebookException exception) {
                updateFailure("");
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        logicManager.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            logicManager.saveUser(createUser());
                            goToMainActivity(logicManager.getAuth().getCurrentUser());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignIn.this, getResources().getString(R.string.error_sign_in_already_exist),
                                    Toast.LENGTH_SHORT).show();
                            updateFailure("");
                        }
                    }
                });
    }
}
