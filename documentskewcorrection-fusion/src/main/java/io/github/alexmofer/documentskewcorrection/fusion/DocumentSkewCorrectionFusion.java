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
package io.github.alexmofer.documentskewcorrection.fusion;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;

import io.github.alexmofer.documentskewcorrection.core.DocumentSkewCorrectionCore;
import io.github.alexmofer.documentskewcorrection.hms.DocumentSkewCorrectionHMS;
import io.github.alexmofer.documentskewcorrection.tensorflow.DocumentSkewCorrectionTensorflow;

/**
 * 文档校正
 * Created by Alex on 2025/5/28.
 */
public class DocumentSkewCorrectionFusion {

    public static final int PLAN_CORE = 0;// OpenCV方案，一般用作保底方案。
    public static final int PLAN_TENSORFLOW = 1;// Tensorflow 代理位图处理，识别与校正依然是OpenCV方案。
    public static final int PLAN_HMS = 2;// 带 HMS 设备的端侧能力，不是所有设备都支持。
    private static final ArrayList<Integer> PLANS = new ArrayList<>();

    static {
        PLANS.add(PLAN_HMS);
        PLANS.add(PLAN_CORE);
    }

    private DocumentSkewCorrectionFusion() {
        //no instance
    }

    /**
     * 设置方案
     *
     * @param plans 方案
     */
    public static void setPlans(int... plans) {
        PLANS.clear();
        for (int plan : plans) {
            PLANS.add(plan);
        }
    }

    @Nullable
    private static <T> T executeSequence(ThrowableFunction<Integer, T> function) throws Exception {
        final int count = PLANS.size();
        for (int i = 0; i < count; i++) {
            final int plan = PLANS.get(i);
            if (i == count - 1) {
                return function.apply(plan);
            } else {
                try {
                    return function.apply(plan);
                } catch (Throwable t) {
                    // ignore
                }
            }
        }
        return null;
    }

    @WorkerThread
    @Nullable
    private static float[] detect(int plan, Context context, Bitmap image) throws Exception {
        if (plan == PLAN_CORE) {
            return DocumentSkewCorrectionCore.detect(image);
        }
        if (plan == PLAN_TENSORFLOW) {
            return DocumentSkewCorrectionTensorflow.detect(context, image);
        }
        if (plan == PLAN_HMS) {
            return DocumentSkewCorrectionHMS.detect(image);
        }
        throw new Exception("Not supported plan:" + plan);
    }

    /**
     * 检测
     *
     * @param context Context
     * @param image   位图
     *                注意：不同的方案对位图有不同要求。
     * @return 检测到的文档边框，返回空表示未检测到文档边框
     */
    @WorkerThread
    @Nullable
    public static float[] detect(Context context, Bitmap image) throws Exception {
        return executeSequence(plan -> detect(plan, context, image));
    }

    @WorkerThread
    @Nullable
    private static float[] detect(int plan, Context context, Uri uri) throws Exception {
        if (plan == PLAN_CORE) {
            return DocumentSkewCorrectionCore.detect(context, uri);
        }
        if (plan == PLAN_TENSORFLOW) {
            return DocumentSkewCorrectionTensorflow.detect(context, uri);
        }
        if (plan == PLAN_HMS) {
            return DocumentSkewCorrectionHMS.detect(context, uri);
        }
        throw new Exception("Not supported plan:" + plan);
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
        return executeSequence(plan -> detect(plan, context, uri));
    }

    @WorkerThread
    @Nullable
    private static Bitmap correct(int plan, Bitmap image, float[] points) throws Exception {
        if (plan == PLAN_CORE) {
            return DocumentSkewCorrectionCore.correct(image, points);
        }
        if (plan == PLAN_TENSORFLOW) {
            return DocumentSkewCorrectionTensorflow.correct(image, points);
        }
        if (plan == PLAN_HMS) {
            return DocumentSkewCorrectionHMS.correct(image, points);
        }
        throw new Exception("Not supported plan:" + plan);
    }

    /**
     * 校正文档
     *
     * @param image  位图
     *               请使用 ARGB_8888 格式
     *               如果有透明度，请外部将该位图转为未预乘的 ARGB_8888
     *               此处不做预乘限制是因为，从相机获取的位图，虽然预乘，但其实没有透明度，可作为未预乘的位图处理。
     * @param points 校正点
     * @return 校正后的位图
     */
    @WorkerThread
    @Nullable
    public static Bitmap correct(Bitmap image, float[] points) throws Exception {
        return executeSequence(plan -> correct(plan, image, points));
    }

    @WorkerThread
    @Nullable
    private static Bitmap correct(int plan, Context context, Uri uri, float[] points) throws Exception {
        if (plan == PLAN_CORE) {
            return DocumentSkewCorrectionCore.correct(context, uri, points);
        }
        if (plan == PLAN_TENSORFLOW) {
            return DocumentSkewCorrectionTensorflow.correct(context, uri, points);
        }
        if (plan == PLAN_HMS) {
            return DocumentSkewCorrectionHMS.correct(context, uri, points);
        }
        throw new Exception("Not supported plan:" + plan);
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
        return executeSequence(plan -> correct(plan, context, uri, points));
    }

    @FunctionalInterface
    interface ThrowableFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
