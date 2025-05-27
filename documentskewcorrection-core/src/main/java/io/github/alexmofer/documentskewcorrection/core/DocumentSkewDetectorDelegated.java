package io.github.alexmofer.documentskewcorrection.core;

import android.graphics.Bitmap;

/**
 * 文档探测器
 * 将 RGB_565 原图转灰度图保存，检测边框时直接计算得到边框数值。
 * 位图处理算法已被外部代理
 * Created by Alex on 2025/5/20.
 */
public final class DocumentSkewDetectorDelegated extends DocumentSkewDetector {

    private DocumentSkewDetectorDelegated(long nativePrt, int width, int height) {
        super(nativePrt, width, height);
    }

    private static native long DR_DocumentSkewDetectorDelegated_create(Object image);

    /**
     * 构建器
     */
    public static abstract class Builder extends DocumentSkewDetector.Builder {

        protected Bitmap mImage;
        protected boolean mRecycleImage;

        @Override
        public DocumentSkewDetectorDelegated build() throws Exception {
            if (mImage == null) {
                throw new Exception("Image is null.");
            }
            if (mImage.isRecycled()) {
                throw new Exception("Image is recycled.");
            }
            try {
                final long nativePrt = DR_DocumentSkewDetectorDelegated_create(mImage);
                if (nativePrt == 0) {
                    throw new Exception("Create fail.");
                }
                return new DocumentSkewDetectorDelegated(nativePrt, mImage.getWidth(), mImage.getHeight());
            } finally {
                if (mRecycleImage) {
                    mImage.recycle();
                }
            }
        }
    }
}
