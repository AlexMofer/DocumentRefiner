// 文档校正器（RGBA未预乘）
// Created by Alex on 2025/5/20.
//

#ifndef DOCUMENTREFINER_DOCUMENTREFINERRGBA_HPP
#define DOCUMENTREFINER_DOCUMENTREFINERRGBA_HPP

#include "DocumentRefiner.hpp"

namespace DR {

    class DocumentRefinerRGBA final : public DocumentRefiner {

    public:
        DocumentRefinerRGBA(int width, int height, void *RGBA) {
            mImage = cv::Mat(height, width, CV_8UC4, RGBA);
        }

        ~DocumentRefinerRGBA() override { mImage.release(); }

        void refine(float ltx, float lty, float rtx, float rty,
                    float lbx, float lby, float rbx, float rby, cv::Mat dst) const override {
            DocumentRefiner::refine(mImage, dst, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
        }

    private:
        cv::Mat mImage;
    };
}

#endif //DOCUMENTREFINER_DOCUMENTREFINERRGBA_HPP
