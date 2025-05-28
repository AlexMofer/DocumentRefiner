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
// 文档校正器（透视输出）
// Created by Alex on 2025/5/20.
//

#ifndef DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTOR_HPP
#define DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTOR_HPP

#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc.hpp>

namespace DR {

    class DocumentSkewCorrector {

    public:
        DocumentSkewCorrector(int width, int height, void *pixels) {
            mImage = cv::Mat(height, width, CV_8UC4, pixels);
        };

        virtual ~DocumentSkewCorrector() {
            mImage.release();
        };

        /**
         * 校正输出
         * @param width 位图宽
         * @param height 位图高
         * @param pixels 像素点
         * @param ltx 左上点X轴坐标
         * @param lty 左上点Y轴坐标
         * @param rtx 右上点X轴坐标
         * @param rty 右上点Y轴坐标
         * @param lbx 左下点X轴坐标
         * @param lby 左下点Y轴坐标
         * @param rbx 右下点X轴坐标
         * @param rby 右下点Y轴坐标
         * @param dst 输出图
         * @return 检测成功时返回true
         */
        void correct(int width, int height, void *pixels,
                     float ltx, float lty, float rtx, float rty,
                     float lbx, float lby, float rbx, float rby) const {
            auto dst = cv::Mat(height, width, CV_8UC4, pixels);
            std::vector<cv::Point2f> srcRect;
            srcRect.emplace_back(ltx, lty);
            srcRect.emplace_back(rtx, rty);
            srcRect.emplace_back(lbx, lby);
            srcRect.emplace_back(rbx, rby);
            std::vector<cv::Point2f> dstRect;
            dstRect.emplace_back(0, 0);
            dstRect.emplace_back(dst.cols, 0);
            dstRect.emplace_back(0, dst.rows);
            dstRect.emplace_back(dst.cols, dst.rows);
            cv::warpPerspective(mImage, dst, cv::getPerspectiveTransform(srcRect, dstRect),
                                dst.size());
        }

    private:
        cv::Mat mImage;
    };
}

#endif //DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTOR_HPP
