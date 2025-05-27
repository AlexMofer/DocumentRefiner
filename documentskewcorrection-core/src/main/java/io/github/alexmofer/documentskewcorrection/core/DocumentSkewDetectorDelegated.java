package io.github.alexmofer.documentskewcorrection.core;

/**
 * 文档探测器
 * 位图处理算法已被外部代理
 * Created by Alex on 2025/5/20.
 */
public final class DocumentSkewDetectorDelegated extends DocumentSkewDetector {

    private DocumentSkewDetectorDelegated(long nativePrt, int width, int height) {
        super(nativePrt, width, height);
    }

    private static native long DR_DocumentSkewDetectorDelegated_create(int width, int height, byte[] pixels);

    /**
     * 构建器
     */
    public static abstract class Builder extends DocumentSkewDetector.Builder {

        protected int mWidth;
        protected int mHeight;
        protected byte[] mPixels;

        @Override
        public DocumentSkewDetectorDelegated build() throws Exception {
            if (mWidth <= 0 || mHeight <= 0 || mPixels == null) {
                throw new IllegalArgumentException();
            }
            final long nativePrt = DR_DocumentSkewDetectorDelegated_create(mWidth, mHeight, mPixels);
            if (nativePrt == 0) {
                throw new Exception("Create fail.");
            }
            return new DocumentSkewDetectorDelegated(nativePrt, mWidth, mHeight);
        }
    }
}
