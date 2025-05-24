// 文档校正器（BGR565）
// Created by Alex on 2025/5/24.
//

#ifndef DOCUMENTREFINER_DOCUMENTREFINERRGB565_H
#define DOCUMENTREFINER_DOCUMENTREFINERRGB565_H

#include "DocumentRefiner.hpp"

namespace DR {

    class DocumentRefinerRGB565 final : public DocumentRefiner {

    public:
        DocumentRefinerRGB565(int width, int height, void *BGR565) {
            mImage = cv::Mat(height, width, CV_8UC2, BGR565);
        }

        ~DocumentRefinerRGB565() override { mImage.release(); }

        void refine(float ltx, float lty, float rtx, float rty,
                    float lbx, float lby, float rbx, float rby, cv::Mat dst) const override {
            DocumentRefiner::refine(mImage, dst, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
        }

    private:
        cv::Mat mImage;
    };
}

#endif //DOCUMENTREFINER_DOCUMENTREFINERRGB565_H
