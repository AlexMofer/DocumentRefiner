package io.github.alexmofer.documentskewcorrection.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

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
         * @param image        文档图片，必须为 RGB_565 格式，DocumentSkewDetectorDelegated 不对位图持有。
         * @param recycleImage 构建后是否主动销毁位图
         * @return 构建器
         */
        public Builder setImage(Bitmap image, boolean recycleImage) {
            if (image == null) {
                throw new RuntimeException("Image is null.");
            }
            if (image.isRecycled()) {
                throw new RuntimeException("Image is recycled.");
            }
            if (image.getConfig() != Bitmap.Config.RGB_565) {
                // 请使用 RGB_565 格式
                throw new RuntimeException("Image is not RGB_565.");
            }
            if (mImage != null) {
                if (mRecycleImage) {
                    mImage.recycle();
                }
            }
            if (image.getWidth() < MAX_SIZE && image.getHeight() < MAX_SIZE) {
                mImage = image;
                mRecycleImage = recycleImage;
                return this;
            }
            // 缩小位图到限定尺寸
            final float scale = Math.min(MAX_SIZE / image.getWidth(), MAX_SIZE / image.getHeight());
            mImage = Bitmap.createScaledBitmap(image,
                    Math.round(scale * image.getWidth()),
                    Math.round(scale * image.getHeight()), true);
            mRecycleImage = true;
            if (recycleImage) {
                image.recycle();
            }
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
        public DocumentSkewDetectorDelegated build() throws Exception {
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
            final long nativePrt = DR_DocumentSkewDetectorDelegated_create(mImage);
            if (nativePrt == 0) {
                throw new Exception("Create fail.");
            }
            try {
                return new DocumentSkewDetectorDelegated(nativePrt, mImage.getWidth(), mImage.getHeight());
            } finally {
                if (mRecycleImage) {
                    mImage.recycle();
                }
            }
        }
    }
}
