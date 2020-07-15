package com.chandhu.firstapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private BookMarkFragment bookMarkFragment;
    private NotificationsFragment notificationsFragment;
    private AccountFragment accountFragment;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar ka code hai yee
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimaryDark));
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_nav_bar);

        if (mAuth.getCurrentUser() != null) {

            homeFragment = new HomeFragment();
            bookMarkFragment = new BookMarkFragment();
            notificationsFragment = new NotificationsFragment();
            accountFragment = new AccountFragment();


            initializeFragment();

            Objects.requireNonNull(getSupportActionBar()).setTitle("Blog");

            FloatingActionButton addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
                    startActivity(intent);
                }
            });
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.bottom_nav_bar);

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_nav:
                        replaceFragments(homeFragment,currentFragment);
                        return true;
                    case R.id.bookmarks_nav:
                        replaceFragments(bookMarkFragment,currentFragment);
                        return true;
                    case R.id.notifications_nav:
                        replaceFragments(notificationsFragment,currentFragment);
                        return true;
                    case R.id.account_nav:
                        replaceFragments(accountFragment,currentFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });


    }

    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container,homeFragment);
        fragmentTransaction.add(R.id.main_container,bookMarkFragment);
        fragmentTransaction.add(R.id.main_container,notificationsFragment);
        fragmentTransaction.add(R.id.main_container,accountFragment);

        fragmentTransaction.hide(bookMarkFragment);
        fragmentTransaction.hide(notificationsFragment);
        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.logout_btn:
                logOut();
                return true;
            case R.id.settings_btn:
                Intent intent = new Intent(getApplicationContext(),SetupActivity.class);
                startActivity(intent);
                finish();
            default:
                return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendToLogin();

        } else {

            String current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        if(!Objects.requireNonNull(task.getResult()).exists()){

                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }

                    } else {

                        String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void sendToLogin() {
        Intent intent = new Intent( MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void logOut() {
        mAuth.signOut();
        Intent LoginIntent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(LoginIntent);
        finish();
    }

    private void replaceFragments(Fragment fragment, Fragment currentFragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

       if (fragment == homeFragment){
           fragmentTransaction.hide(bookMarkFragment);
           fragmentTransaction.hide(notificationsFragment);
           fragmentTransaction.hide(accountFragment);
       }

        if (fragment == bookMarkFragment){
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationsFragment);
            fragmentTransaction.hide(accountFragment);
        }

        if (fragment == notificationsFragment){
            fragmentTransaction.hide(bookMarkFragment);
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);
        }

        if (fragment == accountFragment){
            fragmentTransaction.hide(bookMarkFragment);
            fragmentTransaction.hide(notificationsFragment);
            fragmentTransaction.hide(homeFragment);
        }


        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

}