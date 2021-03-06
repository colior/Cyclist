package com.cyclist.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cyclist.R;
import com.cyclist.logic.LogicManager;
import com.cyclist.logic.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.Calendar;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUp";
    private LogicManager logicManager = LogicManager.getInstance();
    private Calendar myCalendar = Calendar.getInstance();
    private EditText email;
    private EditText password;
    private EditText verifyPassword;
    private EditText fname;
    private EditText lname;
    private Button registerButton;
    private TextView datePickerTextView;
    private Spinner rides;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_signup);
        initializeComponents();
    }

    private void initializeComponents(){

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        verifyPassword = findViewById(R.id.verifyPassword);
        fname = findViewById(R.id.firstName);
        lname = findViewById(R.id.lastName);
        rides = findViewById(R.id.rideSpinner);
        datePickerTextView = findViewById(R.id.datePickerTextView);
        registerButton = findViewById(R.id.registerButton);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,R.layout.spinner_item, getResources().getStringArray(R.array.rides));
        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        rides.setAdapter(spinnerArrayAdapter);

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                updateDate(year, month, dayOfMonth);
            }
        };
        datePickerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerTextView.setTextColor(Color.WHITE);
                new DatePickerDialog(SignUp.this, dateSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableLoadingBar();
                register();
            }
        });

        addOnChangeEvent(email);
        addOnChangeEvent(fname);
        addOnChangeEvent(lname);
        addOnChangeEvent(password);
        addOnChangeEvent(verifyPassword);

    }

    private void addOnChangeEvent(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                editText.setTextColor(Color.WHITE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setTextColor(Color.WHITE);
                editText.setBackground(getResources().getDrawable(R.drawable.back));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void register() {
        if(verifyFields()) {
            signUp(email.getText().toString(), password.getText().toString());
        }
        disableProgressBar();
    }

    private void updateDate(int year, int month, int dayOfMonth) {
        int maxYear, maxMonth, maxDay;
        maxYear = myCalendar.get(Calendar.YEAR);
        maxMonth = myCalendar.get(Calendar.MONTH);
        maxDay = myCalendar.get(Calendar.DAY_OF_MONTH);
        if((year < maxYear) || (year == maxYear && month < maxMonth) || (year == maxYear && month == maxMonth && dayOfMonth <= maxDay)) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            datePickerTextView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void signUp(String email, String password) {
        logicManager.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            User user = createUser();
                            logicManager.saveNewUser(user);
                            Log.d(TAG, "Register:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private User createUser() {
        User user = new User();
        user.setEmail(email.getText().toString());
        user.setFName(fname.getText().toString());
        user.setLName(lname.getText().toString());
        user.setBirthday(myCalendar.getTime());
        user.setRideType(User.RideType.getByPosition(rides.getSelectedItemPosition()));
        return user;
    }

    /******************************************* VALIDATION *******************************************/

    //TODO: fields verification
    private boolean verifyFields() {
        boolean verify = true;
        String emailField = email.getText().toString();
        String passwordField = password.getText().toString();
        String verifyPasswordField = verifyPassword.getText().toString();
        String fnameField = fname.getText().toString();
        String lnameField = lname.getText().toString();
        String datePickerTextViewField = datePickerTextView.getText().toString();

        if(emailField.isEmpty() || !isValidEmailAddress(emailField)){
            signError(email);
            verify = false;
        }

        if(passwordField.isEmpty() || passwordField.length() < 8){
            signError(password);
            verify = false;
        }

        if(verifyPasswordField.isEmpty() || !verifyPasswordField.equals(passwordField)){
            signError(verifyPassword);
            verify = false;
        }

        if(fnameField.isEmpty() || !isValidName(fnameField)){
            signError(fname);
            verify = false;
        }

        if(lnameField.isEmpty() || !isValidName(lnameField)){
            signError(lname);
            verify = false;
        }

        if(datePickerTextViewField.isEmpty()){
            datePickerTextView.setHintTextColor(Color.RED);
            verify = false;
        }

        if(verify){
            registerButton.setEnabled(true);
        }
        return verify;
    }

    private void signError(EditText editText) {
        editText.setTextColor(Color.RED);
        editText.setBackground(getResources().getDrawable(R.drawable.back_error));
    }

    private boolean isValidName(String name) {
        name = name.toLowerCase();
        for(char ch : name.toCharArray()){
            if(!(ch >= getResources().getString(R.string.startChar).toCharArray()[0] && ch <= getResources().getString(R.string.endChar).toCharArray()[0])){
                return false;
            }
        }

        return true;
    }

    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    private void enableLoadingBar() {
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle(R.string.welcome_to_cyclict);
        loadingBar.setMessage(getString(R.string.welcome_to_cyclict));
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
    }

    public void disableProgressBar() {
        loadingBar.hide();
    }
}
