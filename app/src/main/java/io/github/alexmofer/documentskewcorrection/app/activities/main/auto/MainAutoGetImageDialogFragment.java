package io.github.alexmofer.documentskewcorrection.app.activities.main.auto;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import io.github.alexmofer.android.support.utils.FragmentUtils;

/**
 * 获取图片对话框
 * Created by Alex on 2025/5/26.
 */
public class MainAutoGetImageDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle("获取图片")
                .setMessage("选择一种获取图片的方式")
                .setNeutralButton("拍摄", this)
                .setPositiveButton("相册", this)
                .setNegativeButton("文件", this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final Callback callback = FragmentUtils.getCallback(this, Callback.class);
        dismiss();
        if (callback == null) {
            return;
        }
        if (which == DialogInterface.BUTTON_NEUTRAL) {
            callback.onTakePicture();
        } else if (which == DialogInterface.BUTTON_POSITIVE) {
            callback.onPickPhoto();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            callback.onPickFile();
        }
    }

    public interface Callback {
        void onTakePicture();

        void onPickPhoto();

        void onPickFile();
    }
}
