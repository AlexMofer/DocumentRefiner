package io.github.alexmofer.documentskewcorrection.app.activities.main.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.github.alexmofer.android.support.other.StringResource;
import io.github.alexmofer.android.support.other.StringResourceException;
import io.github.alexmofer.android.support.utils.ContextUtils;
import io.github.alexmofer.documentskewcorrection.app.concurrent.ListenableFutureHelper;
import io.github.alexmofer.documentskewcorrection.app.utils.FileProviderUtils;
import io.github.alexmofer.documentskewcorrection.app.utils.StringResourceExceptionUtils;
import io.github.alexmofer.documentskewcorrection.core.DocumentSkewCorrector;
import io.github.alexmofer.documentskewcorrection.core.DocumentSkewDetector;
import io.github.alexmofer.documentskewcorrection.core.DocumentSkewDetectorCanny;

/**
 * ViewModel
 * Created by Alex on 2025/5/26.
 */
public class MainCoreViewModel extends ViewModel {
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
            // 检测
            final DocumentSkewDetector detector =
                    new DocumentSkewDetectorCanny.Builder()
                            .setImage(context, uri)
                            .build();
            final int detectorImageWidth = detector.getWidth();
            final int detectorImageHeight = detector.getHeight();
            final int[] points = detector.detect();
            detector.release();
            if (points == null) {
                throw new StringResourceException("检测不到文档边框，请选择其他图片");
            }
            final float[] ps = new float[8];
            ps[0] = points[0] * 1f / detectorImageWidth;
            ps[1] = points[1] * 1f / detectorImageHeight;
            ps[2] = points[2] * 1f / detectorImageWidth;
            ps[3] = points[3] * 1f / detectorImageHeight;
            ps[4] = points[4] * 1f / detectorImageWidth;
            ps[5] = points[5] * 1f / detectorImageHeight;
            ps[6] = points[6] * 1f / detectorImageWidth;
            ps[7] = points[7] * 1f / detectorImageHeight;
            // 校正
            final DocumentSkewCorrector corrector =
                    new DocumentSkewCorrector.Builder()
                            .setImage(context, uri)
                            .build();
            final int correctorImageWidth = corrector.getWidth();
            final int correctorImageHeight = corrector.getHeight();
            final Bitmap corrected = corrector.correct(
                    ps[0] * correctorImageWidth, ps[1] * correctorImageHeight,
                    ps[2] * correctorImageWidth, ps[3] * correctorImageHeight,
                    ps[4] * correctorImageWidth, ps[5] * correctorImageHeight,
                    ps[6] * correctorImageWidth, ps[7] * correctorImageHeight);
            corrector.release();
            if (corrected == null) {
                throw new StringResourceException("文档校正失败");
            }
            // 写入文件
            final File dir = ContextUtils.getExternalCacheDir(context, true);
            final File saved = new File(dir, "Corrected_" + UUID.randomUUID().toString());
            //noinspection IOStreamConstructor
            try (final OutputStream output = new FileOutputStream(saved)) {
                if (corrected.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    return FileProviderUtils.getUriForFile(context, saved);
                } else {
                    throw new StringResourceException("保存到文件失败");
                }
            } finally {
                corrected.recycle();
            }
        }, result -> {
            mProcessing.setValue(false);
            mCorrected.setValue(result);
        }, t -> {
            mProcessing.setValue(false);
            mFailure.setValue(StringResourceExceptionUtils.getMessage(t));
        });
    }
}
