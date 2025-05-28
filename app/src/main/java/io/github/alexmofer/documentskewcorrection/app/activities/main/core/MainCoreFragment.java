package io.github.alexmofer.documentskewcorrection.app.activities.main.core;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import io.github.alexmofer.documentskewcorrection.app.R;
import io.github.alexmofer.documentskewcorrection.app.activities.main.auto.MainAutoFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.auto.MainAutoViewModel;

/**
 * Core 示范
 * Created by Alex on 2025/5/26.
 */
public class MainCoreFragment extends MainAutoFragment {

    public static void navigate(Fragment fragment) {
        final NavController controller;
        try {
            controller = NavHostFragment.findNavController(fragment);
        } catch (Exception e) {
            return;
        }
        controller.navigate(R.id.main_navigation_core);
    }

    @NonNull
    @Override
    protected MainAutoViewModel onCreateViewModel() {
        return new ViewModelProvider(this).get(MainCoreViewModel.class);
    }

    @Override
    protected CharSequence getTitle() {
        return "Core 示范";
    }
}