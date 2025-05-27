#include <jni.h>
#include <android/bitmap.h>
#include "DocumentSkewDetectorCanny.hpp"
#include "DocumentSkewDetectorDelegated.hpp"
#include "DocumentSkewCorrector.hpp"

static void DR_DocumentSkewDetector_release(JNIEnv */*env*/, jobject /*thiz*/, jlong native_prt) {
    delete ((DR::DocumentSkewDetectorCanny *) native_prt);
}

static jboolean DR_DocumentSkewDetector_detect(JNIEnv *env, jobject /*thiz*/,
                                               jlong native_prt, jintArray points) {
    int ltx, lty, rtx, rty, lbx, lby, rbx, rby;
    if (((DR::DocumentSkewDetectorCanny *) native_prt)->detect(ltx, lty, rtx, rty, lbx, lby, rbx,
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

jint DR_DocumentSkewDetector_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass(
            "io/github/alexmofer/documentskewcorrection/core/DocumentSkewDetector");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DR_DocumentSkewDetector_release", "(J)V",   (void *) (DR_DocumentSkewDetector_release)},
            {"DR_DocumentSkewDetector_detect",  "(J[I)Z", (void *) (DR_DocumentSkewDetector_detect)}
    };
    const jint result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(clazz);
    if (result != JNI_OK) {
        return result;
    }
    return JNI_OK;
}

static jlong DR_DocumentSkewDetectorCanny_create(JNIEnv *env, jclass /*clazz*/,
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
    auto created = new DR::DocumentSkewDetectorCanny((int) info.width, (int) info.height, pixels);
    AndroidBitmap_unlockPixels(env, image);
    return (jlong) created;
}

jint DR_DocumentSkewDetectorCanny_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass(
            "io/github/alexmofer/documentskewcorrection/core/DocumentSkewDetectorCanny");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DR_DocumentSkewDetectorCanny_create", "(Ljava/lang/Object;)J",
             (void *) (DR_DocumentSkewDetectorCanny_create)}
    };
    const jint result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(clazz);
    if (result != JNI_OK) {
        return result;
    }
    return JNI_OK;
}

static jlong DR_DocumentSkewDetectorDelegated_create(JNIEnv *env, jclass /*clazz*/,
                                                     jint width, jint height, jbyteArray pixels) {
    auto created = new DR::DocumentSkewDetectorDelegated(env, width, height, pixels);
    return (jlong) created;
}

jint DR_DocumentSkewDetectorDelegated_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass(
            "io/github/alexmofer/documentskewcorrection/core/DocumentSkewDetectorDelegated");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DR_DocumentSkewDetectorDelegated_create", "(II[B)J",
             (void *) (DR_DocumentSkewDetectorDelegated_create)}
    };
    const jint result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(clazz);
    if (result != JNI_OK) {
        return result;
    }
    return JNI_OK;
}

static jlong DR_DocumentSkewCorrector_create(JNIEnv *env, jclass /*clazz*/, jobject image) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, image, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        return 0;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return 0;
    }
    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, image, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        return 0;
    }
    auto created = new DR::DocumentSkewCorrector((int) info.width, (int) info.height, pixels);
    return (jlong) created;
}

static void DR_DocumentSkewCorrector_release(JNIEnv *env, jobject /*thiz*/,
                                             jlong native_prt, jobject image) {
    delete ((DR::DocumentSkewCorrector *) native_prt);
    AndroidBitmap_unlockPixels(env, image);
}

static jboolean DR_DocumentSkewCorrector_correct(JNIEnv *env, jobject /*thiz*/,
                                                 jlong native_prt, jfloatArray points,
                                                 jobject image) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, image, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        return JNI_FALSE;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        // 格式错误
        return JNI_FALSE;
    }
    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, image, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        return JNI_FALSE;
    }
    jfloat *ps = env->GetFloatArrayElements(points, JNI_FALSE);
    ((DR::DocumentSkewCorrector *) native_prt)->correct((int) info.width, (int) info.height, pixels,
                                                        ps[0], ps[1], ps[2], ps[3], ps[4], ps[5],
                                                        ps[6], ps[7]);
    env->ReleaseFloatArrayElements(points, ps, 0);
    if (AndroidBitmap_unlockPixels(env, image) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 解锁失败
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jint DR_DocumentSkewCorrector_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass(
            "io/github/alexmofer/documentskewcorrection/core/DocumentSkewCorrector");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DR_DocumentSkewCorrector_create",  "(Ljava/lang/Object;)J",    (void *) (DR_DocumentSkewCorrector_create)},
            {"DR_DocumentSkewCorrector_release", "(JLjava/lang/Object;)V",   (void *) (DR_DocumentSkewCorrector_release)},
            {"DR_DocumentSkewCorrector_correct", "(J[FLjava/lang/Object;)Z", (void *) (DR_DocumentSkewCorrector_correct)}
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
    auto register_result = DR_DocumentSkewDetector_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    register_result = DR_DocumentSkewDetectorCanny_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    register_result = DR_DocumentSkewDetectorDelegated_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    register_result = DR_DocumentSkewCorrector_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    return JNI_VERSION_1_6;
}