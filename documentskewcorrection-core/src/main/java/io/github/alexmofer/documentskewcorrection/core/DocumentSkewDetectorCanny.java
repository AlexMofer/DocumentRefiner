/*
 * Copyright (C) 2025 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.alexmofer.documentskewcorrection.core;

import android.graphics.Bitmap;

import androidx.annotation.Keep;

/**
 * 文档探测器
 * 将 RGB_565 原图转灰度图保存，检测边框时先将灰度图高斯模糊再检测边框再二值化（该检测过程会使用不同的参数执行多次），然后计算得到边框数值。
 * 由于运算过程存在多次转换，因此不建议使用过大的位图，默认限定最大尺寸为 500。
 * Created by Alex on 2025/5/20.
 */
@Keep
public final class DocumentSkewDetectorCanny extends DocumentSkewDetector {

    private DocumentSkewDetectorCanny(long nativePrt, int width, int height) {
        super(nativePrt, width, height);
    }

    private static native long DR_DocumentSkewDetectorCanny_create(Object image);

    /**
     * 构建器
     */
    public static class Builder extends DocumentSkewDetector.Builder {

        private final float mMaxSize;
        private Bitmap mImage;
        private boolean mRecycleImage;

        public Builder(int maxSize) {
            mMaxSize = maxSize;
        }

        public Builder() {
            this(500);
        }

        @Override
        public Builder setImage(Bitmap image, boolean recycleImage) {
            if (image == null) {
                throw new RuntimeException("Image is null.");
            }
            if (image.isRecycled()) {
                throw new RuntimeException("Image is recycled.");
            }
            if (image.getConfig() != Bitmap.Config.ARGB_8888
                    && image.getConfig() != Bitmap.Config.RGB_565) {
                // 注意 ARGB_8888 格式的位图会强制作为未预乘的位图处理，传入带透明度的位图会检测不准确。
                // 如果确定位图不带透明度，那么预乘与不预乘是无区别的。
                // 因实际处理都会转为灰度图，如果位图带有透明度，请外部处理好是底部叠加黑色还是白色。
                // 此处不做预乘限制是因为，从相机获取的位图，虽然预乘，但其实没有透明度，可作为未预乘的位图处理。
                throw new RuntimeException("Image format is not support.");
            }
            if (mImage != null) {
                if (mRecycleImage) {
                    mImage.recycle();
                }
            }
            if (image.getWidth() < mMaxSize && image.getHeight() < mMaxSize) {
                mImage = image;
                mRecycleImage = recycleImage;
                return this;
            }
            // 缩小位图到限定尺寸
            final float scale = Math.min(mMaxSize / image.getWidth(), mMaxSize / image.getHeight());
            mImage = Bitmap.createScaledBitmap(image,
                    Math.round(scale * image.getWidth()),
                    Math.round(scale * image.getHeight()), true);
            mRecycleImage = true;
            if (recycleImage) {
                image.recycle();
            }
            return this;
        }

        @Override
        public DocumentSkewDetectorCanny build() throws Exception {
            if (mImage == null) {
                throw new Exception("Image is null.");
            }
            if (mImage.isRecycled()) {
                throw new Exception("Image is recycled.");
            }
            try {
                final long nativePrt = DR_DocumentSkewDetectorCanny_create(mImage);
                if (nativePrt == 0) {
                    throw new Exception("Create fail.");
                }
                return new DocumentSkewDetectorCanny(nativePrt, mImage.getWidth(), mImage.getHeight());
            } finally {
                if (mRecycleImage) {
                    mImage.recycle();
                }
            }
        }
    }
}
