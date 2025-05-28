package io.github.alexmofer.documentskewcorrection.app.activities.main.common;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.UUID;

import io.github.alexmofer.android.support.utils.ContextUtils;
import io.github.alexmofer.documentskewcorrection.app.utils.FileProviderUtils;

/**
 * ViewModel
 * Created by Alex on 2025/5/26.
 */
public abstract class MainCommonViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mProcessing = new MutableLiveData<>(false);
    private Uri mTakePictureUri;

    LiveData<Boolean> getProcessing() {
        return mProcessing;
    }

    /**
     * 设置是否处理中
     *
     * @param processing 是否处理中
     */
    protected final void setProcessing(boolean processing) {
        mProcessing.setValue(processing);
    }

    void handlePickImage(Context context, Uri uri) {
        handleImage(context.getApplicationContext(), uri);
    }

    @NonNull
    Uri generateTakePictureUri(Context context) {
        final File dir = ContextUtils.getExternalCacheDir(context, true);
        final File file = new File(dir, "Temp " + UUID.randomUUID().toString());
        mTakePictureUri = FileProviderUtils.getUriForFile(context, file);
        return mTakePictureUri;
    }

    void handleTakePicture(Context context) {
        if (mTakePictureUri == null) {
            return;
        }
        handleImage(context.getApplicationContext(), mTakePictureUri);
        mTakePictureUri = null;
    }

    /**
     * 处理图片
     *
     * @param context Context
     * @param uri     图片链接
     */
    protected abstract void handleImage(Context context, Uri uri);
}
