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