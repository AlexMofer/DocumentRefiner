package io.github.alexmofer.documentskewcorrection.app.activities.main.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

import io.github.alexmofer.android.support.widget.AvoidArea;
import io.github.alexmofer.documentskewcorrection.app.R;
import io.github.alexmofer.documentskewcorrection.app.databinding.FragmentMainCorrectedBinding;

/**
 * Created by Alex on 2025/5/28.
 */
public class MainCorrectedFragment extends Fragment {

    private static final String KEY_URI = "uri";

    public static void navigate(Fragment fragment, @NonNull Uri uri) {
        final NavController controller;
        try {
            controller = NavHostFragment.findNavController(fragment);
        } catch (Exception e) {
            return;
        }
        final Bundle args = new Bundle();
        args.putParcelable(KEY_URI, uri);
        controller.navigate(R.id.main_action_ui_to_corrected, args);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final FragmentMainCorrectedBinding binding =
                FragmentMainCorrectedBinding.inflate(getLayoutInflater(), container, false);
        AvoidArea.paddingIgnoreBottom(binding.fmcVToolbar);
        AvoidArea.paddingIgnoreTop(binding.fmcVContent);
        binding.fmcVToolbar.setNavigationOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        Glide.with(this)
                .load(BundleCompat.getParcelable(requireArguments(), KEY_URI, Uri.class))
                .into(binding.fmuVImage);
        return binding.getRoot();
    }
}
