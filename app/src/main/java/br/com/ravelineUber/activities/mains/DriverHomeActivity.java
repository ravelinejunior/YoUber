package br.com.ravelineUber.activities.mains;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import br.com.ravelineUber.R;
import br.com.ravelineUber.utils.Common;
import br.com.ravelineUber.utils.UserUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class DriverHomeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1010;
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private ImageView imageProfile;

    //firebase
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    private AlertDialog alertDialog;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = findViewById(R.id.toolbar_maps);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //iniciando dados usuario
        init();

    }

    private void init() {

        //photo
        alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Waiting...")
                .create();

        storageRef = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        //set data for user
        View headerView = navigationView.getHeaderView(0);
        TextView txt_name = headerView.findViewById(R.id.text_header_name_id_drawer);
        TextView txt_phone = headerView.findViewById(R.id.text_phone_header_id_drawer);
        TextView txt_star = headerView.findViewById(R.id.header_star_id_drawer);
        imageProfile = headerView.findViewById(R.id.profile_image_header);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phone.setText(Common.currentUser != null ? Common.currentUser.getPhoneNumber() : "");
        txt_star.setText(Common.currentUser != null ? String.valueOf(Common.currentUser.getRating()) : "0.0");



        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_header_signout) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
                builder.setTitle("Sign Out")
                        .setMessage("Você realmente deseja realmente sair?")
                        .setNegativeButton("Não", (dialogInterface, i) ->
                                dialogInterface.dismiss())
                        .setPositiveButton("Sim", (dialogInterface, i) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(DriverHomeActivity.this, SplashScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });

                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialogInterface -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(this,android.R.color.holo_red_dark));

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(this,R.color.colorAccent));
                });
                dialog.show();
            }
            return false;
        });


        imageProfile.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);

        });

        if (Common.currentUser != null && Common.currentUser.getUrlProfileImage() != null
                && !TextUtils.isEmpty(Common.currentUser.getUrlProfileImage())) {
            Glide.with(this)
                    .load(Common.currentUser.getUrlProfileImage())
                    .placeholder(R.drawable.fui_ic_github_white_24dp)
                    .into(imageProfile);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data.getData();
            imageProfile.setImageURI(imageUri);

            showDialogUp();
        }
    }

    private void showDialogUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
        builder.setTitle("Photo")
                .setMessage("Deseja alterar imagem de perfil?")
                .setNegativeButton("Não", (dialogInterface, i) ->
                        dialogInterface.dismiss())
                .setPositiveButton("Sim", (dialogInterface, i) -> {
                    if (imageUri != null) {
                        alertDialog.setMessage("Uploading...");
                        alertDialog.show();

                        String uid_name = auth.getCurrentUser().getUid();
                        StorageReference imageFolder = storageRef.child("Profile_Images/" + uid_name);

                        imageFolder.putFile(imageUri)
                                .addOnFailureListener(e -> {
                                   alertDialog.dismiss();
                                    Snackbar.make(drawer,e.getMessage(),Snackbar.LENGTH_SHORT).
                                            setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
                                })
                                .addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                            Map<String,Object> updateData = new HashMap();
                                            updateData.put("profileImage",uri.toString());

                                            UserUtils.updateUser(drawer,updateData);
                                        });
                                    }
                                    alertDialog.dismiss();
                                }).addOnProgressListener(snapshot -> {
                                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                    alertDialog.setMessage(
                                            new StringBuilder("Uploading: ")
                                            .append(progress)
                                            .append("%")
                                    );
                                });

                    }

                });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.colorAccent));
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}