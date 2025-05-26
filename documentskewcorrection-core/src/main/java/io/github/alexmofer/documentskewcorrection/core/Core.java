package io.github.alexmofer.documentskewcorrection.core;

import androidx.annotation.Nullable;

final class Core {

    private final static Core INSTANCE;

    static {
        Core core;
        try {
            System.loadLibrary("core");
            //noinspection InstantiationOfUtilityClass
            core = new Core();
        } catch (Throwable e) {
            core = null;
        }
        INSTANCE = core;
    }

    private Core() {
        //no instance
    }

    @Nullable
    public static Core getInstance() {
        return INSTANCE;
    }
}