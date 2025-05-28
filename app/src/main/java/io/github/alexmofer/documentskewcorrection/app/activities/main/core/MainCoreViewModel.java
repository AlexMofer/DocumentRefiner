package io.github.alexmofer.documentskewcorrection.app.activities.main.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.github.alexmofer.android.support.other.StringResourceException;
import io.github.alexmofer.android.support.utils.ContextUtils;
import io.github.alexmofer.documentskewcorrection.app.activities.main.auto.MainAutoViewModel;
import io.github.alexmofer.documentskewcorrection.app.utils.FileProviderUtils;
import io.github.alexmofer.documentskewcorrection.core.DocumentSkewCorrectionCore;

/**
 * ViewModel
 * Created by Alex on 2025/5/26.
 */
public class MainCoreViewModel extends MainAutoViewModel {

    @Override
    @NonNull
    protected Uri handleImageInBackground(Context context, @NonNull Uri uri) throws Exception {
        // 检测
        this.notifyDetectStart();
        final float[] points = DocumentSkewCorrectionCore.detect(context, uri);
        this.notifyDetectEnd();
        if (points == null) {
            throw new StringResourceException("检测不到文档边框，请选择其他图片");
        }
        // 校正
        this.notifyCorrectStart();
        final Bitmap corrected = DocumentSkewCorrectionCore.correct(context, uri, points);
        this.notifyCorrectEnd();
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
    }
}
