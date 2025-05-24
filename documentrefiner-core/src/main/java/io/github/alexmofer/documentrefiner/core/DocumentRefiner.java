package io.github.alexmofer.documentrefiner.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * 文档校正器
 * Created by Alex on 2025/5/20.
 */
public final class DocumentRefiner {

    private final long mNativePrt;
    private final Bitmap mImage;
    private final boolean mRecycleImage;
    private boolean mReleased = false;

    private DocumentRefiner(long nativePrt, Bitmap image, boolean recycleImage) {
        mNativePrt = nativePrt;
        mImage = image;
        mRecycleImage = recycleImage;
    }

    private static native long DR_DocumentRefiner_create(Object image);

    /**
     * 释放
     */
    public void release() {
        if (mReleased) {
            return;
        }
        mReleased = true;
        DR_DocumentRefiner_release(mNativePrt, mImage);
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
    public Bitmap refine(float ltx, float lty, float rtx, float rty,
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
        final Bitmap bitmap = createOutput(width, height, mImage);
        final float[] points = new float[8];
        points[0] = ltx;
        points[1] = lty;
        points[2] = rtx;
        points[3] = rty;
        points[4] = lbx;
        points[5] = lby;
        points[6] = rbx;
        points[7] = rby;
        if (DR_DocumentRefiner_refine(mNativePrt, points, bitmap)) {
            return bitmap;
        }
        bitmap.recycle();
        return null;
    }

    private Bitmap createOutput(int width, int height, Bitmap src) {
        if (src.getConfig() == Bitmap.Config.ARGB_8888) {
            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPremultiplied(false);
            return bitmap;
        } else {
            return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }
    }

    private native void DR_DocumentRefiner_release(long nativePrt, Object image);

    private native boolean DR_DocumentRefiner_refine(long nativePrt, float[] points, Object image);

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
         * @param image        文档图片，必须为未预乘的 ARGB_8888 格式，DocumentRefiner 会对位图持有。
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
            if (image.isPremultiplied() || image.getConfig() != Bitmap.Config.ARGB_8888) {
                // 请使用 ARGB_8888 格式
                throw new RuntimeException("Image is premultiplied or not ARGB_8888.");
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
        public DocumentRefiner build() throws Exception {
            if (mImage == null) {
                throw new Exception("Image is null.");
            }
            if (mImage.isRecycled()) {
                throw new Exception("Image is recycled.");
            }
            if (mImage.getConfig() == Bitmap.Config.ARGB_8888) {
                if (mImage.isPremultiplied()) {
                    // 请使用为预乘的 ARGB_8888 格式
                    throw new Exception("Image is premultiplied.");
                }
            }
            if (mImage.getConfig() != Bitmap.Config.ARGB_8888
                    && mImage.getConfig() == Bitmap.Config.RGB_565) {
                // 位图格式错误
                throw new Exception("Image is not ARGB_8888 or RGB_565.");
            }
            final long nativePrt = DR_DocumentRefiner_create(mImage);
            if (nativePrt == 0) {
                throw new Exception("Create fail.");
            }
            try {
                return new DocumentRefiner(nativePrt, mImage, mRecycleImage);
            } catch (Exception e) {
                if (mRecycleImage) {
                    mImage.recycle();
                }
                throw e;
            }
        }
    }
}
