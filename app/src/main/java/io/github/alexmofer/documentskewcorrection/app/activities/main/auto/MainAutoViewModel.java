package io.github.alexmofer.documentskewcorrection.app.activities.main.auto;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.UUID;

import io.github.alexmofer.android.support.other.StringResource;
import io.github.alexmofer.android.support.utils.ContextUtils;
import io.github.alexmofer.documentskewcorrection.app.concurrent.ListenableFutureHelper;
import io.github.alexmofer.documentskewcorrection.app.utils.FileProviderUtils;
import io.github.alexmofer.documentskewcorrection.app.utils.StringResourceExceptionUtils;

/**
 * ViewModel
 * Created by Alex on 2025/5/26.
 */
public abstract class MainAutoViewModel extends ViewModel {
    private final MutableLiveData<Uri> mOriginal = new MutableLiveData<>();
    private final MutableLiveData<Uri> mCorrected = new MutableLiveData<>();
    private final MutableLiveData<StringResource> mFailure = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mProcessing = new MutableLiveData<>(false);
    private Uri mTakePictureUri;

    LiveData<Uri> getOriginal() {
        return mOriginal;
    }

    LiveData<Uri> getCorrected() {
        return mCorrected;
    }

    LiveData<StringResource> getFailure() {
        return mFailure;
    }

    void clearFailure() {
        mFailure.setValue(null);
    }

    LiveData<Boolean> getProcessing() {
        return mProcessing;
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

    private void handleImage(Context context, Uri uri) {
        mOriginal.setValue(uri);
        mProcessing.setValue(true);
        ListenableFutureHelper.submit(() -> {
            return handleImageInBackground(context, uri);
        }, result -> {
            mProcessing.setValue(false);
            mCorrected.setValue(result);
        }, t -> {
            mProcessing.setValue(false);
            mFailure.setValue(StringResourceExceptionUtils.getMessage(t));
        });
    }

    /**
     * 处理图片
     *
     * @param context Context
     * @param uri     图片Uri
     * @return 处理后的图片Uri
     * @throws Exception 失败信息
     */
    @WorkerThread
    @NonNull
    protected abstract Uri handleImageInBackground(Context context, @NonNull Uri uri) throws Exception;
}
