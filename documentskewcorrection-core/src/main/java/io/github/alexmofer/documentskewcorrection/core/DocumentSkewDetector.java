package io.github.alexmofer.documentskewcorrection.core;

import androidx.annotation.Nullable;

/**
 * 文档探测器
 * Created by Alex on 2025/5/20.
 */
public abstract class DocumentSkewDetector {

    protected static final float MAX_SIZE = 500f;// 图片最大尺寸
    private final long mNativePrt;
    private final int mImageWidth;
    private final int mImageHeight;
    private boolean mReleased = false;

    protected DocumentSkewDetector(long nativePrt, int width, int height) {
        mNativePrt = nativePrt;
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * 释放
     */
    public void release() {
        if (mReleased) {
            return;
        }
        mReleased = true;
        DR_DocumentSkewDetector_release(mNativePrt);
    }

    /**
     * 获取位图宽度
     *
     * @return 位图宽度
     */
    public int getWidth() {
        return mImageWidth;
    }

    /**
     * 获取位图高度
     *
     * @return 位图高度
     */
    public int getHeight() {
        return mImageHeight;
    }

    /**
     * 检测
     *
     * @return 检测到边框时返回边框四个点（左上、右上、左下、右下），未检测到时返回 null
     */
    @Nullable
    public int[] detect() {
        if (mReleased) {
            return null;
        }
        final int[] points = new int[8];
        if (DR_DocumentSkewDetector_detect(mNativePrt, points)) {
            return points;
        }
        return null;
    }

    private native void DR_DocumentSkewDetector_release(long nativePrt);

    private native boolean DR_DocumentSkewDetector_detect(long nativePrt, int[] points);
}
