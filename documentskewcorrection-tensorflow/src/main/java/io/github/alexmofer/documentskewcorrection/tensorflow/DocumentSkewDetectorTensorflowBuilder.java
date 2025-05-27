package io.github.alexmofer.documentskewcorrection.tensorflow;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import io.github.alexmofer.documentskewcorrection.core.DocumentSkewDetectorDelegated;

/**
 * 使用 tensorflow 代理 Canny 算法
 * Created by Alex on 2025/5/27.
 */
public class DocumentSkewDetectorTensorflowBuilder extends DocumentSkewDetectorDelegated.Builder {
    private static Interpreter sDefaultInterpreter;
    private final Interpreter mInterpreter;

    public DocumentSkewDetectorTensorflowBuilder(Interpreter interpreter) {
        mInterpreter = interpreter;
    }

    public DocumentSkewDetectorTensorflowBuilder(Context context) throws IOException {
        this(getInterpreter(context));
    }

    /**
     * 新建 Interpreter
     *
     * @param manager  AssetManager
     * @param fileName 文件名
     * @return Interpreter
     * @throws IOException 文件读写异常
     */
    public static Interpreter newInterpreter(AssetManager manager, String fileName) throws IOException {
        try (final AssetFileDescriptor fd = manager.openFd(fileName)) {
            return new Interpreter(fd.createInputStream().getChannel().map(
                    FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getLength()));
        }
    }

    /**
     * 获取默认 Interpreter
     *
     * @param context Context
     * @return 默认 Interpreter
     */
    public static Interpreter getInterpreter(Context context) throws IOException {
        if (sDefaultInterpreter == null) {
            sDefaultInterpreter = newInterpreter(context.getAssets(),
                    "hed_lite_model_quantize.tflite");
        }
        return sDefaultInterpreter;
    }

    @Override
    public DocumentSkewDetectorTensorflowBuilder setImage(Bitmap image, boolean recycleImage) {
        // 该模型强制要求输入位图为256*256的位图，其他尺寸均报：java.lang.IllegalArgumentException: Cannot copy to a TensorFlowLite tensor (hed_input) with 786432 bytes from a Java Buffer with *** bytes.
        final int width = 256;
        final int height = 256;
        final Bitmap scaled = Bitmap.createScaledBitmap(image, width, height, true);
        // 创建像素值
        final int[] pixels = new int[width * height];
        // 读取位图像素值
        scaled.getPixels(pixels, 0, width, 0, 0, width, height);
        if (scaled != image) {
            scaled.recycle();
        }
        // 创建输入
        final ByteBuffer input = ByteBuffer.allocateDirect(width * height * 3 * Float.SIZE / Byte.SIZE);
        input.order(ByteOrder.nativeOrder());
        input.clear();
        input.rewind();
        // 写入输入
        for (int pixel : pixels) {
            input.putFloat(((pixel >> 16) & 0xFF));
            input.putFloat(((pixel >> 8) & 0xFF));
            input.putFloat((pixel & 0xFF));
        }
        // 创建输出
        final ByteBuffer output = ByteBuffer.allocateDirect(width * height * Float.SIZE / Byte.SIZE);
        output.order(ByteOrder.nativeOrder());
        output.clear();
        // 处理
        mInterpreter.run(input, output);
        // 读取输出，并进行二值化
        output.rewind();
        for (int i = 0; i < pixels.length; i++) {
            if (output.getFloat() > 0.2) {
                pixels[i] = 0xFFFFFFFF;
            } else {
                pixels[i] = 0xFF000000;
            }
        }
        mImage = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.RGB_565);
        mRecycleImage = true;
        if (recycleImage) {
            image.recycle();
        }
        return this;
    }

    /**
     * 获取 Interpreter
     *
     * @return Interpreter
     */
    public Interpreter getInterpreter() {
        return mInterpreter;
    }
}
