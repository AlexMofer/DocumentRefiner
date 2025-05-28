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
package io.github.alexmofer.documentskewcorrection.hms;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionConstant;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult;

import java.util.ArrayList;

/**
 * 文档校正
 * Created by Alex on 2025/5/26.
 */
public class DocumentSkewCorrectionHMS {

    private static Boolean sEnable;

    private DocumentSkewCorrectionHMS() {
        //no instance
    }

    /**
     * 判断 HMS Core 是否可用
     *
     * @return HMS Core 可用时返回true
     */
    public static boolean isEnable(Context context) {
        if (sEnable == null) {
            sEnable = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context) == 0;
        }
        return sEnable;
    }

    /**
     * 检测
     *
     * @param image 位图
     * @return 检测到的文档边框，返回空表示未检测到文档边框
     */
    @WorkerThread
    @Nullable
    public static float[] detect(Bitmap image) throws Exception {
        Bitmap target = image;
        if (target.getWidth() > 1920 || target.getHeight() > 1920) {
            // 图片大小超限，缩小到限定尺寸
            final float scale = Math.min(1920f / target.getWidth(), 1920f / target.getHeight());
            target = Bitmap.createScaledBitmap(target,
                    Math.round(scale * target.getWidth()),
                    Math.round(scale * target.getHeight()), true);
        }
        final int width = target.getWidth();
        final int height = target.getHeight();
        final MLFrame frame = MLFrame.fromBitmap(target);
        final MLDocumentSkewCorrectionAnalyzer analyzer =
                MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
                        .getDocumentSkewCorrectionAnalyzer(
                                new MLDocumentSkewCorrectionAnalyzerSetting.Factory().create());
        final SparseArray<MLDocumentSkewDetectResult> result = analyzer.analyseFrame(frame);
        if (target != image) {
            target.recycle();
        }
        if (result == null || result.size() <= 0) {
            // 失败。
            analyzer.stop();
            return null;
        }
        final MLDocumentSkewDetectResult detected = result.get(0);
        if (detected == null ||
                detected.getResultCode() != MLDocumentSkewCorrectionConstant.SUCCESS) {
            // 失败。
            analyzer.stop();
            return null;
        }
        analyzer.stop();
        final Point lt = detected.getLeftTopPosition();
        final Point rt = detected.getRightTopPosition();
        final Point lb = detected.getLeftBottomPosition();
        final Point rb = detected.getRightBottomPosition();
        final float[] ps = new float[8];
        ps[0] = lt.x * 1f / width;
        ps[1] = lt.y * 1f / height;
        ps[2] = rt.x * 1f / width;
        ps[3] = rt.y * 1f / height;
        ps[4] = lb.x * 1f / width;
        ps[5] = lb.y * 1f / height;
        ps[6] = rb.x * 1f / width;
        ps[7] = rb.y * 1f / height;
        return ps;
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
        final Bitmap image = Utils.fromUri(context, uri, true,
                Bitmap.Config.RGB_565, false);
        try {
            return detect(image);
        } finally {
            image.recycle();
        }
    }

    /**
     * 校正文档
     *
     * @param image  位图
     * @param points 校正点
     * @return 校正后的位图
     */
    @WorkerThread
    @Nullable
    public static Bitmap correct(Bitmap image, float[] points) throws Exception {
        final int width = image.getWidth();
        final int height = image.getHeight();
        final MLFrame frame = MLFrame.fromBitmap(image);
        final ArrayList<Point> coordinates = new ArrayList<>();
        coordinates.add(new Point(Math.max(0, Math.min(width, Math.round(points[0] * width))),
                Math.max(0, Math.min(height, Math.round(points[1] * height)))));
        coordinates.add(new Point(Math.max(0, Math.min(width, Math.round(points[2] * width))),
                Math.max(0, Math.min(height, Math.round(points[3] * height)))));
        coordinates.add(new Point(Math.max(0, Math.min(width, Math.round(points[6] * width))),
                Math.max(0, Math.min(height, Math.round(points[7] * height)))));
        coordinates.add(new Point(Math.max(0, Math.min(width, Math.round(points[4] * width))),
                Math.max(0, Math.min(height, Math.round(points[5] * height)))));
        final MLDocumentSkewCorrectionAnalyzer analyzer =
                MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
                        .getDocumentSkewCorrectionAnalyzer(
                                new MLDocumentSkewCorrectionAnalyzerSetting.Factory().create());
        final SparseArray<MLDocumentSkewCorrectionResult> result =
                analyzer.syncDocumentSkewCorrect(frame,
                        new MLDocumentSkewCorrectionCoordinateInput(coordinates));
        if (result == null || result.size() <= 0) {
            // 失败。
            analyzer.stop();
            return null;
        }
        final MLDocumentSkewCorrectionResult corrected = result.get(0);
        if (corrected == null ||
                corrected.getResultCode() != MLDocumentSkewCorrectionConstant.SUCCESS) {
            // 失败。
            analyzer.stop();
            return null;
        }
        analyzer.stop();
        return corrected.getCorrected();
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
        final Bitmap image = Utils.fromUri(context, uri, true,
                Bitmap.Config.ARGB_8888, false);
        try {
            return correct(image, points);
        } finally {
            image.recycle();
        }
    }
}
