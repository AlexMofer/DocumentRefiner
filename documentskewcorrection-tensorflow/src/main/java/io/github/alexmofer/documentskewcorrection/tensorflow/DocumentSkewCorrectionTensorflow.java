package io.github.alexmofer.documentskewcorrection.tensorflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import io.github.alexmofer.documentskewcorrection.core.DocumentSkewCorrectionCore;
import io.github.alexmofer.documentskewcorrection.core.DocumentSkewDetector;

/**
 * 文档校正
 * Created by Alex on 2025/5/26.
 */
public class DocumentSkewCorrectionTensorflow {

    private DocumentSkewCorrectionTensorflow() {
        //no instance
    }

    @WorkerThread
    @Nullable
    private static float[] detect(DocumentSkewDetector detector) {
        final int width = detector.getWidth();
        final int height = detector.getHeight();
        final int[] points = detector.detect();
        detector.release();
        if (points == null) {
            // 识别结束，未检测到文档
            return null;
        }
        final float[] ps = new float[8];
        ps[0] = points[0] * 1f / width;
        ps[1] = points[1] * 1f / height;
        ps[2] = points[2] * 1f / width;
        ps[3] = points[3] * 1f / height;
        ps[4] = points[4] * 1f / width;
        ps[5] = points[5] * 1f / height;
        ps[6] = points[6] * 1f / width;
        ps[7] = points[7] * 1f / height;
        return ps;
    }

    /**
     * 检测
     *
     * @param context Context
     * @param image   位图
     * @return 检测到的文档边框，返回空表示未检测到文档边框
     */
    @WorkerThread
    @Nullable
    public static float[] detect(Context context, Bitmap image) throws Exception {
        return detect(new DocumentSkewDetectorTensorflowBuilder(context)
                .setImage(image, false)
                .build());
    }

    /**
     * 检测
     *
     * @param context Context
     * @param uri     图片链接
     * @return 检测到的文档边框，返回空表示未检测到文档边框
     */
    @WorkerThread
    @Nullable
    public static float[] detect(Context context, Uri uri) throws Exception {
        return detect(new DocumentSkewDetectorTensorflowBuilder(context)
                .setImage(context, uri)
                .build());
    }

    /**
     * 校正文档
     *
     * @param image  位图
     *               请使用 ARGB_8888 格式
     *               如果有透明度，请外部将该位图转为未预乘的 ARGB_8888
     *               此处不做预乘限制是因为，从相机获取的位图，虽然预乘，但其实没有透明度，可作为未预乘的位图处理。
     * @param points 校正点
     * @return 校正后的位图
     */
    @WorkerThread
    @Nullable
    public static Bitmap correct(Bitmap image, float[] points) throws Exception {
        return DocumentSkewCorrectionCore.correct(image, points);
    }

    /**
     * 校正文档
     *
     * @param context Context
     * @param uri     图片链接
     * @param points  校正点
     * @return 校正后的位图
     */
    @WorkerThread
    @Nullable
    public static Bitmap correct(Context context, Uri uri, float[] points) throws Exception {
        return DocumentSkewCorrectionCore.correct(context, uri, points);
    }
}
