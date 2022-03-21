package YAO.GreenLife.core;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.greenlife.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {


    private BottomNavigationView navigationView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = findViewById(R.id.nav_bottom);
        navigationView.setOnNavigationItemSelectedListener(this);
        setMain();


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragmentTransaction.replace(R.id.main_body, new TipsFragment()).commit();
                return true;
            case R.id.navigation_dashboard:
                fragmentTransaction.replace(R.id.main_body, new IdentifyFragment()).commit();
                return true;
            case R.id.navigation_notifications:
                fragmentTransaction.replace(R.id.main_body, new HistoryFragment()).commit();
                return true;
        }
        return true;
    }

    //用于打开初始页面
    private void setMain() {
        //getSupportFragmentManager() -> beginTransaction() -> add -> (R.id.main_boy, new Fragment()
        this.getSupportFragmentManager().beginTransaction().add(R.id.main_body, new TipsFragment()).commit();
    }
}