package io.github.alexmofer.documentskewcorrection.app.activities.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.alexmofer.documentskewcorrection.app.activities.main.core.MainCoreFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.hms.MainHMSFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.tensorflow.MainTensorflowFragment;
import io.github.alexmofer.documentskewcorrection.app.databinding.FragmentMainRootBinding;
import io.github.alexmofer.documentskewcorrection.app.widgets.AvoidArea;

/**
 * 根页面
 * Created by Alex on 2025/5/26.
 */
public class MainRootFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final FragmentMainRootBinding binding =
                FragmentMainRootBinding.inflate(getLayoutInflater(), container, false);
        AvoidArea.paddingAll(binding.fmrVContent);
        binding.fmrVCore.setOnClickListener(v -> MainCoreFragment.navigate(this));
        binding.fmrVTensorflow.setOnClickListener(v -> MainTensorflowFragment.navigate(this));
        binding.fmrVHms.setOnClickListener(v -> MainHMSFragment.navigate(this));
        return binding.getRoot();
    }
}
