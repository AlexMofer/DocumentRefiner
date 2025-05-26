package io.github.alexmofer.documentskewcorrection.core;

import android.graphics.Bitmap;

/**
 * 文档探测器
 * 将 RGB_565 原图转灰度图保存，检测边框时先将灰度图高斯模糊再检测边框再二值化（该检测过程会使用不同的参数执行多次），然后计算得到边框数值。
 * 由于运算过程存在多次转换，因此不建议使用过大的位图
 * Created by Alex on 2025/5/20.
 */
public final class DocumentSkewDetectorCanny extends DocumentSkewDetector {

    private DocumentSkewDetectorCanny(long nativePrt, int width, int height) {
        super(nativePrt, width, height);
    }

    private static native long DR_DocumentSkewDetectorCanny_create(Object image);

    /**
     * 构建器
     */
    public static class Builder extends DocumentSkewDetector.Builder {

        @Override
        public DocumentSkewDetectorCanny build() throws Exception {
            if (mImage == null) {
                throw new Exception("Image is null.");
            }
            if (mImage.isRecycled()) {
                throw new Exception("Image is recycled.");
            }
            if (mImage.getConfig() != Bitmap.Config.RGB_565) {
                // 请使用 RGB_565 格式
                throw new Exception("Image is not RGB_565.");
            }
            final long nativePrt = DR_DocumentSkewDetectorCanny_create(mImage);
            if (nativePrt == 0) {
                throw new Exception("Create fail.");
            }
            try {
                return new DocumentSkewDetectorCanny(nativePrt, mImage.getWidth(), mImage.getHeight());
            } finally {
                if (mRecycleImage) {
                    mImage.recycle();
                }
            }
        }
    }
}
