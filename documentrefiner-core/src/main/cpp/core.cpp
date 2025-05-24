#include <jni.h>
#include <android/bitmap.h>
#include "DocumentDetectorCanny.hpp"
#include "DocumentDetectorDelegated.hpp"
#include "DocumentRefinerRGBA.hpp"
#include "DocumentRefinerRGB565.h"

static void DR_DocumentDetector_release(JNIEnv */*env*/, jobject /*thiz*/, jlong native_prt) {
    delete ((DR::DocumentDetectorCanny *) native_prt);
}

static jboolean DR_DocumentDetector_detect(JNIEnv *env, jobject /*thiz*/,
                                           jlong native_prt, jintArray points) {
    int ltx, lty, rtx, rty, lbx, lby, rbx, rby;
    if (((DR::DocumentDetectorCanny *) native_prt)->detect(ltx, lty, rtx, rty, lbx, lby, rbx,
                                                           rby)) {
        jint *ps = env->GetIntArrayElements(points, JNI_FALSE);
        ps[0] = ltx;
        ps[1] = lty;
        ps[2] = rtx;
        ps[3] = rty;
        ps[4] = lbx;
        ps[5] = lby;
        ps[6] = rbx;
        ps[7] = rby;
        env->ReleaseIntArrayElements(points, ps, 0);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

jint DR_DocumentDetector_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass("io/github/alexmofer/documentrefiner/core/DocumentDetector");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DR_DocumentDetector_release", "(J)V",   (void *) (DR_DocumentDetector_release)},
            {"DR_DocumentDetector_detect",  "(J[I)Z", (void *) (DR_DocumentDetector_detect)}
    };
    const jint result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(clazz);
    if (result != JNI_OK) {
        return result;
    }
    return JNI_OK;
}

static jlong DR_DocumentDetectorCanny_create(JNIEnv *env, jclass /*clazz*/,
                                             jobject image) {
    // 上层控制此处传入的是BGR565的位图（ANDROID_BITMAP_FORMAT_RGB_565 位图）
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, image, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        return 0;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        // 格式错误，此处不做格式转换。因为终究要转为灰度图，因此为提高效率，此处强制要求传入无透明通道的位图
        return 0;
    }
    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, image, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        return 0;
    }
    auto created = new DR::DocumentDetectorCanny((int) info.width, (int) info.height, pixels);
    AndroidBitmap_unlockPixels(env, image);
    return (jlong) created;
}

jint DR_DocumentDetectorCanny_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass("io/github/alexmofer/documentrefiner/core/DocumentDetectorCanny");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DR_DocumentDetectorCanny_create", "(Ljava/lang/Object;)J",
             (void *) (DR_DocumentDetectorCanny_create)}
    };
    const jint result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(clazz);
    if (result != JNI_OK) {
        return result;
    }
    return JNI_OK;
}

static jlong DR_DocumentDetectorDelegated_create(JNIEnv *env, jclass /*clazz*/,
                                                 jobject image) {
    // 上层控制此处传入的是BGR565的位图（ANDROID_BITMAP_FORMAT_RGB_565 位图）
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, image, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        return 0;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        // 格式错误，此处不做格式转换。因为终究要转为灰度图，因此为提高效率，此处强制要求传入无透明通道的位图
        return 0;
    }
    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, image, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        return 0;
    }
    auto created = new DR::DocumentDetectorDelegated((int) info.width, (int) info.height, pixels);
    AndroidBitmap_unlockPixels(env, image);
    return (jlong) created;
}

jint DR_DocumentDetectorDelegated_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass(
            "io/github/alexmofer/documentrefiner/core/DocumentDetectorDelegated");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DR_DocumentDetectorDelegated_create", "(Ljava/lang/Object;)J",
             (void *) (DR_DocumentDetectorDelegated_create)}
    };
    const jint result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(clazz);
    if (result != JNI_OK) {
        return result;
    }
    return JNI_OK;
}

static void DR_DocumentRefiner_release(JNIEnv *env, jobject /*thiz*/,
                                       jlong native_prt, jobject image) {
    delete ((DR::DocumentRefiner *) native_prt);
    AndroidBitmap_unlockPixels(env, image);
}

static jboolean DR_DocumentRefiner_refine(JNIEnv *env, jobject /*thiz*/,
                                          jlong native_prt, jfloatArray points, jobject image) {
    // 上层控制此处传入的是RGBA的位图（未预乘的 ANDROID_BITMAP_FORMAT_RGBA_8888 位图）
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, image, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        return JNI_FALSE;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return JNI_FALSE;
    }
    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, image, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        return JNI_FALSE;
    }
    cv::Mat RGBA = cv::Mat((int) info.height, (int) info.width, CV_8UC4, pixels);
    jfloat *ps = env->GetFloatArrayElements(points, JNI_FALSE);
    ((DR::DocumentRefiner *) native_prt)->refine(ps[0], ps[1], ps[2], ps[3], ps[4], ps[5],
                                                 ps[6], ps[7], RGBA);
    env->ReleaseFloatArrayElements(points, ps, 0);
    if (AndroidBitmap_unlockPixels(env, image) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 解锁失败
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static jlong DR_DocumentRefiner_create(JNIEnv *env, jclass /*clazz*/, jobject image) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, image, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        return 0;
    }
    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, image, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        return 0;
    }
    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        // 上层确保未预乘，否则透明度会出错
        auto created = new DR::DocumentRefinerRGBA((int) info.width, (int) info.height, pixels);
        return (jlong) created;
    }
    if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        auto created = new DR::DocumentRefinerRGB565((int) info.width, (int) info.height, pixels);
        return (jlong) created;
    }
    return 0;
}

jint DR_DocumentRefiner_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass("io/github/alexmofer/documentrefiner/core/DocumentRefiner");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DR_DocumentRefiner_release", "(JLjava/lang/Object;)V",   (void *) (DR_DocumentRefiner_release)},
            {"DR_DocumentRefiner_refine",  "(J[FLjava/lang/Object;)Z", (void *) (DR_DocumentRefiner_refine)},
            {"DR_DocumentRefiner_create",  "(Ljava/lang/Object;)J",    (void *) (DR_DocumentRefiner_create)}
    };
    const jint result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(clazz);
    if (result != JNI_OK) {
        return result;
    }
    return JNI_OK;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void */*reversed*/) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    auto register_result = DR_DocumentDetector_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    register_result = DR_DocumentDetectorCanny_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    register_result = DR_DocumentDetectorDelegated_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    register_result = DR_DocumentRefiner_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    return JNI_VERSION_1_6;
}