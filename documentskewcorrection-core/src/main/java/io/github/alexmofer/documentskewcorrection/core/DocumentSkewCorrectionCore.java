package io.github.alexmofer.documentskewcorrection.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

/**
 * 文档校正
 * Created by Alex on 2025/5/26.
 */
public class DocumentSkewCorrectionCore {

    private DocumentSkewCorrectionCore() {
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
     * @param image 位图
     *              注意 ARGB_8888 格式的位图会强制作为未预乘的位图处理，传入带透明度的位图会检测不准确。
     *              如果确定位图不带透明度，那么预乘与不预乘是无区别的。
     *              因实际处理都会转为灰度图，如果位图带有透明度，请外部处理好是底部叠加黑色还是白色。
     *              此处不做预乘限制是因为，从相机获取的位图，虽然预乘，但其实没有透明度，可作为未预乘的位图处理。
     * @return 检测到的文档边框，返回空表示未检测到文档边框
     */
    @WorkerThread
    @Nullable
    public static float[] detect(Bitmap image) throws Exception {
        return detect(new DocumentSkewDetectorCanny.Builder()
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
        return detect(new DocumentSkewDetectorCanny.Builder()
                .setImage(context, uri)
                .build());
    }

    @WorkerThread
    @Nullable
    private static Bitmap correct(DocumentSkewCorrector corrector, float[] points) {
        final int width = corrector.getWidth();
        final int height = corrector.getHeight();
        final Bitmap corrected = corrector.correct(
                points[0] * width, points[1] * height,
                points[2] * width, points[3] * height,
                points[4] * width, points[5] * height,
                points[6] * width, points[7] * height);
        corrector.release();
        return corrected;
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
        return correct(new DocumentSkewCorrector.Builder()
                .setImage(image, false)
                .build(), points);
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
        return correct(new DocumentSkewCorrector.Builder()
                .setImage(context, uri)
                .build(), points);
    }
}
