package io.github.alexmofer.documentskewcorrection.app.activities.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    }
}
