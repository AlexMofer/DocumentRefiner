# DocumentSkewCorrection

从 [SmartCropper](https://github.com/pqpo/SmartCamera) fork而来，主要优化以下几个点：

1. 拆分核心功能与UI功能。
2. 边框识别算法优化，避免不必要的多次颜色转换。
3. 位图矩形透视算法优化，避免不必要的多次颜色转换。
4. OpenCV 的 CMake导入方式优化。
5. UI功能使用项目基本都会使用的Glide库实现位图预览。
6. 代码发布到 Maven Central。

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




