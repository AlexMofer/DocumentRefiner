package io.github.alexmofer.documentskewcorrection.app.utils;

import io.github.alexmofer.android.support.other.StringResource;
import io.github.alexmofer.android.support.other.StringResourceException;

/**
 * 资源形式的错误工具
 * Created by Alex on 2025/5/26.
 */
public class StringResourceExceptionUtils {

    private StringResourceExceptionUtils() {
        //no instance
    }

    /**
     * 获取消息
     *
     * @param t 异常
     * @return 消息
     */
    public static StringResource getMessage(Throwable t) {
        if (t instanceof StringResourceException) {
            return ((StringResourceException) t).getStringResource();
        }
        return new StringResource(t.getMessage());
    }
}
