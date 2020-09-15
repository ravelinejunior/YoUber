package br.com.ravelineUber.activities.mains;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import br.com.ravelineUber.R;
import br.com.ravelineUber.model.Driver;
import br.com.ravelineUber.utils.Common;
import br.com.ravelineUber.utils.UserUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171;

    @BindView(R.id.progressBar_SplashScreen)
    ProgressBar progressBarSplash;

    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onStart() {

        super.onStart();
        auth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (auth != null && listener != null)
            auth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {


        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Common.DRIVER_INFO_REFERENCE);

        ButterKnife.bind(this);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null) {

                //update token
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnFailureListener(e ->
                                Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())

                        .addOnSuccessListener(instanceIdResult ->{
                                UserUtils.updateToken(SplashScreenActivity.this, instanceIdResult.getToken());
                            Log.d("TOKEN",instanceIdResult.getToken());
                        });


                checkUserFromFirebase();
            } else
                showLoginLayout();
        };
    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout =
                new AuthMethodPickerLayout.Builder(R.layout.layout_sign_in)
                        .setPhoneButtonId(R.id.button_sign_in_phone)
                        .setGoogleButtonId(R.id.button_sign_in_google)
                        .build();
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAuthMethodPickerLayout(authMethodPickerLayout)
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .build(), LOGIN_REQUEST_CODE
        );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {


                checkUserFromFirebase();

            } else {
                Toast.makeText(this, "Falha ao logar: " + Objects.requireNonNull(response.getError()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkUserFromFirebase() {
        databaseReference.child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Toast.makeText(SplashScreenActivity.this, "Usuário já existe na base de dados!", Toast.LENGTH_SHORT).show();
                            Driver driver = snapshot.getValue(Driver.class);
                            goToHomeActivity(driver);
                        } else {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashScreenActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToHomeActivity(Driver driver) {
        Common.currentUser = driver; // valor inicial
        startActivity(new Intent(SplashScreenActivity.this, DriverHomeActivity.class));
        finish();
    }

    private void showRegisterLayout() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);

        TextInputEditText firstName = itemView.findViewById(R.id.first_name_Register);
        TextInputEditText lastName = itemView.findViewById(R.id.last_name_Register);
        TextInputEditText phoneNumber = itemView.findViewById(R.id.phone_number_Register);

        Button buttonRegister = itemView.findViewById(R.id.button_register_Register);

        //set data
        if (auth.getCurrentUser().getPhoneNumber() != null && !TextUtils.isEmpty(auth.getCurrentUser().getPhoneNumber()))
            phoneNumber.setText(auth.getCurrentUser().getPhoneNumber());

        //set view
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();

        buttonRegister.setOnClickListener(view -> {
            if (TextUtils.isEmpty(firstName.getText().toString())) {
                Snackbar.make(itemView, "Digite seu primeiro nome", Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
                return;
            } else if (TextUtils.isEmpty(lastName.getText().toString())) {
                Snackbar.make(itemView, "Digite seu ultimo nome", Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
                return;
            } else if (TextUtils.isEmpty(phoneNumber.getText().toString())) {
                Snackbar.make(itemView, "Digite seu telefone", Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
                return;
            } else {

                progressBarSplash.setVisibility(View.VISIBLE);
                Driver driver = new Driver();
                driver.setFirstName(firstName.getText().toString());
                driver.setLastName(lastName.getText().toString());
                driver.setPhoneNumber(phoneNumber.getText().toString());
                driver.setRating(0.0);

                databaseReference.child(auth.getCurrentUser().getUid())
                        .setValue(driver)
                        .addOnFailureListener(e -> {
                            Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                        })
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Cadastro realizado com sucesso.", Toast.LENGTH_SHORT).show();
                            Completable.timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                                    .subscribe(() -> progressBarSplash.setVisibility(View.GONE));
                            dialog.dismiss();
                            goToHomeActivity(driver);
                        });
            }

        });

    }
}