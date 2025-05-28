package io.github.alexmofer.documentskewcorrection.app.activities.main.common;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import io.github.alexmofer.documentskewcorrection.app.R;

/**
 * Core 功能 示范
 * Created by Alex on 2025/5/26.
 */
public abstract class MainCommonFragment<T extends MainCommonViewModel> extends Fragment implements
        MainCommonGetImageDialogFragment.Callback, MainCommonProcessingDialogFragment.Callback {

    private ActivityResultLauncher<PickVisualMediaRequest> mPickPhoto;
    private ActivityResultLauncher<Uri> mTakePicture;
    private ActivityResultLauncher<String[]> mPickFile;
    protected T mViewModel;

    /**
     * 创建 ViewModel
     *
     * @return ViewModel
     */
    @NonNull
    protected abstract T onCreateViewModel();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = onCreateViewModel();
        mPickPhoto = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        mViewModel.handlePickImage(requireContext(), uri);
                    }
                });
        mTakePicture = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                done -> {
                    if (Boolean.TRUE.equals(done)) {
                        mViewModel.handleTakePicture(requireContext());
                    }
                });
        mPickFile = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        mViewModel.handlePickImage(requireContext(), uri);
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getProcessing().observe(getViewLifecycleOwner(), value -> {
            if (Boolean.TRUE.equals(value)) {
                new MainCommonProcessingDialogFragment()
                        .show(getChildFragmentManager(), "Processing");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPickPhoto.unregister();
        mTakePicture.unregister();
        mPickFile.unregister();
    }

    @Override
    public void onTakePicture() {
        mTakePicture.launch(mViewModel.generateTakePictureUri(requireContext()));
    }

    @Override
    public void onPickPhoto() {
        mPickPhoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    @Override
    public void onPickFile() {
        mPickFile.launch(new String[]{"image/*"});
    }

    @Override
    public LiveData<Boolean> getProcessing() {
        return mViewModel.getProcessing();
    }

    protected Toolbar.OnMenuItemClickListener newOnMenuItemClickListener() {
        return item -> {
            if (item.getItemId() == R.id.fmc_menu_pick) {
                new MainCommonGetImageDialogFragment()
                        .show(getChildFragmentManager(), "Get_Image");
                return true;
            }
            return false;
        };
    }
}
