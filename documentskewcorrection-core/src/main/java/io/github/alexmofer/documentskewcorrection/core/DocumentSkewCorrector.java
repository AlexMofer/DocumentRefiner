package io.github.alexmofer.documentskewcorrection.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * 文档校正器
 * Created by Alex on 2025/5/20.
 */
public final class DocumentSkewCorrector {

    private final long mNativePrt;
    private final Bitmap mImage;
    private final boolean mRecycleImage;
    private boolean mReleased = false;

    private DocumentSkewCorrector(long nativePrt, Bitmap image, boolean recycleImage) {
        mNativePrt = nativePrt;
        mImage = image;
        mRecycleImage = recycleImage;
    }

    private static native long DR_DocumentSkewCorrector_create(Object image);

    /**
     * 释放
     */
    public void release() {
        if (mReleased) {
            return;
        }
        mReleased = true;
        DR_DocumentSkewCorrector_release(mNativePrt, mImage);
        if (mRecycleImage) {
            mImage.recycle();
        }
    }

    /**
     * 获取位图宽度
     *
     * @return 位图宽度
     */
    public int getWidth() {
        return mImage.getWidth();
    }

    /**
     * 获取位图高度
     *
     * @return 位图高度
     */
    public int getHeight() {
        return mImage.getHeight();
    }

    /**
     * 校正（此处不进行点的位置校验，请确保点不交叉）
     *
     * @param ltx 左上X
     * @param lty 左上Y
     * @param rtx 右上X
     * @param rty 右上Y
     * @param lbx 左下X
     * @param lby 左下Y
     * @param rbx 右下X
     * @param rby 右下Y
     * @return 校正后的位图，校正失败时返回空
     */
    @Nullable
    public Bitmap correct(float ltx, float lty, float rtx, float rty,
                         float lbx, float lby, float rbx, float rby) {
        if (mReleased) {
            return null;
        }
        final int width = (int) Math.round(
                (Utils.calculatePointToPoint(ltx, lty, rtx, rty)
                        + Utils.calculatePointToPoint(lbx, lby, rbx, rby)) * 0.5f);
        final int height = (int) Math.round(
                (Utils.calculatePointToPoint(ltx, lty, lbx, lby)
                        + Utils.calculatePointToPoint(rtx, rty, rbx, rby)) * 0.5f);
        if (width <= 0 || height <= 0) {
            return null;
        }
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPremultiplied(false);
        final float[] points = new float[8];
        points[0] = ltx;
        points[1] = lty;
        points[2] = rtx;
        points[3] = rty;
        points[4] = lbx;
        points[5] = lby;
        points[6] = rbx;
        points[7] = rby;
        if (DR_DocumentSkewCorrector_correct(mNativePrt, points, bitmap)) {
            return bitmap;
        }
        bitmap.recycle();
        return null;
    }

    private native void DR_DocumentSkewCorrector_release(long nativePrt, Object image);

    private native boolean DR_DocumentSkewCorrector_correct(long nativePrt, float[] points, Object image);

    /**
     * 构建器
     */
    public static class Builder {

        private Bitmap mImage;
        private boolean mRecycleImage;

        public Builder() {
            if (Core.getInstance() == null) {
                throw new RuntimeException("Core disable.");
            }
        }

        /**
         * 设置位图
         *
         * @param image        文档图片，必须为 ARGB_8888 格式，DocumentSkewCorrector 会对位图持有。
         * @param recycleImage 释放时是否主动销毁位图
         * @return 构建器
         */
        public Builder setImage(Bitmap image, boolean recycleImage) {
            if (image == null) {
                throw new RuntimeException("Image is null.");
            }
            if (image.isRecycled()) {
                throw new RuntimeException("Image is recycled.");
            }
            if (image.getConfig() != Bitmap.Config.ARGB_8888) {
                // 请使用 ARGB_8888 格式
                // 如果有透明度，请外部将该位图转为未预乘的 ARGB_8888
                // 此处不做预乘限制是因为，从相机获取的位图，虽然预乘，但其实没有透明度，可作为未预乘的位图处理。
                throw new RuntimeException("Image is not ARGB_8888.");
            }
            if (mImage != null) {
                if (mRecycleImage) {
                    mImage.recycle();
                }
            }
            mImage = image;
            mRecycleImage = recycleImage;
            return this;
        }

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
                        Bitmap.Config.ARGB_8888, false);
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
        public DocumentSkewCorrector build() throws Exception {
            if (mImage == null) {
                throw new Exception("Image is null.");
            }
            if (mImage.isRecycled()) {
                throw new Exception("Image is recycled.");
            }
            final long nativePrt = DR_DocumentSkewCorrector_create(mImage);
            if (nativePrt == 0) {
                throw new Exception("Create fail.");
            }
            try {
                return new DocumentSkewCorrector(nativePrt, mImage, mRecycleImage);
            } catch (Exception e) {
                if (mRecycleImage) {
                    mImage.recycle();
                }
                throw e;
            }
        }
    }
}
