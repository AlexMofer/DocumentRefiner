package io.github.alexmofer.documentskewcorrection.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * 文档探测器
 * Created by Alex on 2025/5/20.
 */
public abstract class DocumentSkewDetector {

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

    /**
     * 构建器
     */
    public static abstract class Builder {

        public Builder() {
            if (Core.getInstance() == null) {
                throw new RuntimeException("Core disable.");
            }
        }

        /**
         * 设置位图
         *
         * @param image        文档图片
         * @param recycleImage 构建后是否主动销毁位图
         * @return 构建器
         */
        public abstract Builder setImage(Bitmap image, boolean recycleImage);

        /**
         * 设置位图
         *
         * @param context Context
         * @param uri     图片Uri
         * @return 构建器
         */
        public Builder setImage(Context context, Uri uri) {
            final Bitmap image;
            try {
                image = Utils.fromUri(context, uri, false,
                        Bitmap.Config.RGB_565, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return setImage(image, true);
        }

        /**
         * 构建
         *
         * @return 文档矫正器
         * @throws Exception 失败信息
         */
        public abstract DocumentSkewDetector build() throws Exception;
    }
}
