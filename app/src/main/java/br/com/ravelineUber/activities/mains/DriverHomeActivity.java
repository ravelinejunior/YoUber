package br.com.ravelineUber.activities.mains;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import br.com.ravelineUber.R;
import br.com.ravelineUber.utils.Common;

public class DriverHomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private  NavigationView navigationView;
    private NavController navController;

    //firebase


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

        //set data for user
        View headerView = navigationView.getHeaderView(0);
        TextView txt_name = headerView.findViewById(R.id.text_header_name_id_drawer);
        TextView txt_phone = headerView.findViewById(R.id.text_phone_header_id_drawer);
        TextView txt_star = headerView.findViewById(R.id.header_star_id_drawer);
        navigationView.setNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_header_signout){
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
                builder.setTitle("Sign Out")
                        .setMessage("Você realmente deseja realmente sair?")
                        .setNegativeButton("Não", (dialogInterface, i) ->
                                dialogInterface.dismiss())
                        .setPositiveButton("Sim", (dialogInterface, i) -> {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(DriverHomeActivity.this,SplashScreenActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                });

                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                .setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                });
                dialog.show();
            }
            return false;
        });

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phone.setText(Common.currentUser != null ? Common.currentUser.getPhoneNumber() : "");
        txt_star.setText(Common.currentUser != null ? String.valueOf(Common.currentUser.getRating()) : "0.0");

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