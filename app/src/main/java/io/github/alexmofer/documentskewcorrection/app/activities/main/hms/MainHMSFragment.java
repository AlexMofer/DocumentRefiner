package io.github.alexmofer.documentskewcorrection.app.activities.main.hms;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import io.github.alexmofer.documentskewcorrection.app.R;
import io.github.alexmofer.documentskewcorrection.app.activities.main.auto.MainAutoFragment;
import io.github.alexmofer.documentskewcorrection.app.activities.main.auto.MainAutoViewModel;

/**
 * HMS 算法 示范
 * Created by Alex on 2025/5/27.
 */
public class MainHMSFragment extends MainAutoFragment {

    public static void navigate(Fragment fragment) {
        final NavController controller;
        try {
            controller = NavHostFragment.findNavController(fragment);
        } catch (Exception e) {
            return;
        }
        controller.navigate(R.id.pca_navigation_hms);
    }

    @NonNull
    @Override
    protected MainAutoViewModel onCreateViewModel() {
        return new ViewModelProvider(this).get(MainHMSViewModel.class);
    }

    @Override
    protected CharSequence getTitle() {
        return "HMS 算法 示范";
    }
}