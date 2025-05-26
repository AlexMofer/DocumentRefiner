package io.github.alexmofer.documentskewcorrection.app.activities.main.core;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

/**
 * 处理中
 * Created by Alex on 2025/5/26.
 */
public class MainCoreProcessingDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setCancelable(false);
        return new AlertDialog.Builder(requireContext())
                .setMessage("正在处理图片，请稍候...")
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final MainCoreViewModel viewModel =
                new ViewModelProvider(requireParentFragment()).get(MainCoreViewModel.class);
        viewModel.getProcessing().observe(getViewLifecycleOwner(), value -> {
            if (!Boolean.TRUE.equals(value)) {
                dismiss();
            }
        });
        return new FrameLayout(requireContext());// 如果返回 null， getViewLifecycleOwner 会报错。
    }
}
