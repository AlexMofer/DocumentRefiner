package io.github.alexmofer.documentskewcorrection.app.activities.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import io.github.alexmofer.documentskewcorrection.app.R;
import io.github.alexmofer.documentskewcorrection.app.databinding.ActivityMainBinding;

/**
 * 主页面
 * Created by Alex on 2025/5/26.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(ActivityMainBinding.inflate(getLayoutInflater()).getRoot());
        final NavController controller = Navigation.findNavController(this, R.id.main_nav_host);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                final NavDestination destination = controller.getCurrentDestination();
                if (destination != null) {
                    if (destination.getId() != R.id.main_navigation_root) {
                        controller.popBackStack();
                        return;
                    }
                }
                this.setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                this.setEnabled(true);
            }
        });
    }
}
