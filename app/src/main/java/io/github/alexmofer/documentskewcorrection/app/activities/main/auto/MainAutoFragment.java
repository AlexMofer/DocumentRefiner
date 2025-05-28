package io.github.alexmofer.documentskewcorrection.app.activities.main.auto;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import io.github.alexmofer.android.support.other.StringResource;
import io.github.alexmofer.documentskewcorrection.app.R;
import io.github.alexmofer.documentskewcorrection.app.databinding.FragmentMainAutoBinding;
import io.github.alexmofer.documentskewcorrection.app.widgets.AvoidArea;

/**
 * Core 功能 示范
 * Created by Alex on 2025/5/26.
 */
public abstract class MainAutoFragment extends Fragment implements
        MainAutoGetImageDialogFragment.Callback, MainAutoProcessingDialogFragment.Callback {

    private ActivityResultLauncher<PickVisualMediaRequest> mPickPhoto;
    private ActivityResultLauncher<Uri> mTakePicture;
    private ActivityResultLauncher<String[]> mPickFile;
    private MainAutoViewModel mViewModel;

    /**
     * 创建 ViewModel
     *
     * @return ViewModel
     */
    @NonNull
    protected abstract MainAutoViewModel onCreateViewModel();

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

    /**
     * 获取标题
     *
     * @return 标题
     */
    protected abstract CharSequence getTitle();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final FragmentMainAutoBinding binding =
                FragmentMainAutoBinding.inflate(getLayoutInflater(), container, false);
        AvoidArea.paddingIgnoreBottom(binding.fmcVToolbar);
        AvoidArea.paddingIgnoreTop(binding.fmcVContent);
        binding.fmcVToolbar.setTitle(getTitle());
        binding.fmcVToolbar.setNavigationOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.fmcVToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.fmc_menu_pick) {
                new MainAutoGetImageDialogFragment()
                        .show(getChildFragmentManager(), "Get_Image");
                return true;
            }
            return false;
        });

        final LifecycleOwner owner = getViewLifecycleOwner();
        final RequestManager manager = Glide.with(this);
        mViewModel.getInfo().observe(owner,
                value -> StringResource.setText(binding.fmcVInfo, value));
        mViewModel.getOriginal().observe(owner,
                value -> manager.load(value).into(binding.fmcVOriginal));
        mViewModel.getCorrected().observe(owner,
                value -> manager.load(value).into(binding.fmcVCorrected));
        mViewModel.getFailure().observe(owner, value -> {
            if (value != null) {
                StringResource.showToast(requireContext(), value);
                mViewModel.clearFailure();
            }
        });
        mViewModel.getProcessing().observe(owner, value -> {
            if (Boolean.TRUE.equals(value)) {
                new MainAutoProcessingDialogFragment()
                        .show(getChildFragmentManager(), "Get_Image");
            }
        });
        return binding.getRoot();
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
}
