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
 * 速度快一些，但精度丢失明显，适合速度要求高于精度要求的识别场景。
 * Created by Alex on 2025/5/27.
 */
public class DocumentSkewDetectorTensorflowBuilder extends DocumentSkewDetectorDelegated.Builder {
    private static Interpreter sDefaultInterpreter;
    private final Interpreter mInterpreter;
    private Bitmap mImage;
    private boolean mRecycleImage;

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
        if (image == null) {
            throw new RuntimeException("Image is null.");
        }
        if (image.isRecycled()) {
            throw new RuntimeException("Image is recycled.");
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

    @Override
    public DocumentSkewDetectorDelegated build() throws Exception {
        if (mImage == null) {
            throw new Exception("Image is null.");
        }
        if (mImage.isRecycled()) {
            throw new Exception("Image is recycled.");
        }
        try {
            // 该模型强制要求输入位图为256*256的位图，其他尺寸均报：java.lang.IllegalArgumentException: Cannot copy to a TensorFlowLite tensor (hed_input) with 786432 bytes from a Java Buffer with *** bytes.
            // 因此缺点也很明显：精度降低到256，且强行变形到256*256，输出要进行按比例变回去，其精度丢失明显大很多。
            final int width = 256;
            final int height = 256;
            final Bitmap scaled = Bitmap.createScaledBitmap(mImage, width, height, true);
            // 创建像素值
            final int[] pixels = new int[width * height];
            // 读取位图像素值
            scaled.getPixels(pixels, 0, width, 0, 0, width, height);
            if (scaled != mImage) {
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
            final byte[] ps = new byte[width * height];
            for (int i = 0; i < ps.length; i++) {
                if (output.getFloat() > 0.2) {
                    ps[i] = (byte) 255;
                } else {
                    ps[i] = 0;
                }
            }
            mWidth = width;
            mHeight = height;
            mPixels = ps;
            // 主动调用一下gc
            System.gc();
        } finally {
            if (mRecycleImage) {
                mImage.recycle();
            }
        }
        return super.build();
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
