DocumentSkewCorrection-Fusion
=========

介绍
---

文档检测与校正融合库

使用
---

**引用:**
```
// 全使用：
dependencies {
    ...
    implementation 'androidx.annotation:annotation:1.9.1'
    implementation 'androidx.exifinterface:exifinterface:1.4.1'
    // 核心
    implementation 'io.github.alexmofer.documentskewcorrection:documentskewcorrection-core:4.11.0'
    // TensorFlow
    implementation 'org.tensorflow:tensorflow-lite:2.9.0'
    implementation 'io.github.alexmofer.documentskewcorrection:documentskewcorrection-tensorflow:1.0.0'
    // HMS
    implementation 'com.huawei.hms:ml-computer-vision-documentskew:3.11.0.301'
    implementation 'com.huawei.hms:ml-computer-vision-documentskew-model:3.7.0.301'
    implementation 'io.github.alexmofer.documentskewcorrection:documentskewcorrection-hms:1.0.0'
    // 融合
    implementation 'io.github.alexmofer.documentskewcorrection:documentskewcorrection-fusion:1.0.0'
    ...
}
// 使用 Core 与 HMS
dependencies {
    ...
    implementation 'androidx.annotation:annotation:1.9.1'
    implementation 'androidx.exifinterface:exifinterface:1.4.1'
    // 核心
    implementation 'io.github.alexmofer.documentskewcorrection:documentskewcorrection-core:4.11.0'
    // HMS
    implementation 'com.huawei.hms:ml-computer-vision-documentskew:3.11.0.301'
    implementation 'com.huawei.hms:ml-computer-vision-documentskew-model:3.7.0.301'
    implementation 'io.github.alexmofer.documentskewcorrection:documentskewcorrection-hms:1.0.0'
    // 融合
    implementation 'io.github.alexmofer.documentskewcorrection:documentskewcorrection-fusion:1.0.0'
    ...
}
```

支持
---

- Gmail: <mailto:moferalex@gmail.com>

许可
---

Copyright 2025 AlexMofer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.