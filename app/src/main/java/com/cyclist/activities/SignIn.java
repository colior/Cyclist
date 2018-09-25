package com.cyclist.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.models.User;
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

import java.util.Date;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;

    private LogicManager logicManager;
    private EditText username;
    private EditText password;
    private Button signUpButton;
    private Button signInButton;
    private LoginButton facebookLoginButton;
    private CallbackManager facebookCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_singin);
        enableLoadingBar();
        initializeComponents();
    }

    private void enableLoadingBar() {
        //TODO: check
        //TODO: check if need to hide the main LinearLayout
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Welcome To Cyclist!");
        loadingBar.setMessage("Please wait while we check your details");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
    }

    public void disableProgressBar() {
        loadingBar.hide();
    }

    public void onUserSignedIn(){
        goToMainActivity();
    }

    private void initializeComponents() {
        logicManager = new LogicManager(this);
        username = findViewById(R.id.usernameEditText);
        password = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signInButton = findViewById(R.id.signInButton);
        facebookLoginButton = findViewById(R.id.facebook_login_button);

        initializeListeners();
        initializeFacebookSingIn();
        initializeGoogleSingIn();
    }

    private void initializeListeners() {
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
                            logicManager.connect();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            updateFailure(getResources().getString(R.string.error_sign_in_failed));
                        }
                    }
                });
    }

    private void updateFailure(String errorMsg) {
        Toast.makeText(SignIn.this, errorMsg,
                Toast.LENGTH_SHORT).show();
    }

    private void goToMainActivity() {
            Intent mainActivityIntent = new Intent(SignIn.this, MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
    }

    public void onSignInButtonClick(View view) {
        if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
            signIn(username.getText().toString(), password.getText().toString());
        }
    }

    private User createUserFromFacebook(AuthResult result){
        Map<String, Object> profile = result.getAdditionalUserInfo().getProfile();
        User user = new User();
        user.setBirthday(stringToDate((String) profile.get("birthday")));
        user.setEmail((String) profile.get("email"));
        user.setFName((String) profile.get("first_name"));
        user.setLName((String) profile.get("last_name"));
        user.setRideType(User.RideType.BIKE);
        return user;
    }

    private User createUserFromGoogle(AuthResult result){
        Map<String, Object> profile = result.getAdditionalUserInfo().getProfile();
        User user = new User();
        user.setEmail((String) profile.get("email"));
        user.setFName((String) profile.get("given_name"));
        user.setLName((String) profile.get("family_name"));
        user.setRideType(User.RideType.BIKE);
        return user;
    }

    private Date stringToDate(String birthday) {
        Date date = new Date();
        String[] parts = birthday.split("/");
        date.setMonth(Integer.parseInt(parts[0]) - 1);
        date.setDate(Integer.parseInt(parts[1]));
        date.setYear(Integer.parseInt(parts[2]));
        return date;
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
                updateFailure(getResources().getString(R.string.errorOccurred));
            }
        }
        else{
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initializeGoogleSingIn() {
        findViewById(R.id.google_login_button).setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        logicManager.setMGoogleSignInClient(mGoogleSignInClient);
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
                            logicManager.saveUser(createUserFromGoogle(task.getResult()));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateFailure(getResources().getString(R.string.errorOccurred));
                        }
                    }
                });
    }


    /******************************************* FACEBOOK *******************************************/

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
                updateFailure(getResources().getString(R.string.loginCanceled));
            }

            @Override
            public void onError(FacebookException exception) {
                updateFailure(getResources().getString(R.string.errorOccurred));
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
                            logicManager.saveUser(createUserFromFacebook(task.getResult()));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateFailure(getResources().getString(R.string.errorOccurred));
                        }
                    }
                });
    }


}
