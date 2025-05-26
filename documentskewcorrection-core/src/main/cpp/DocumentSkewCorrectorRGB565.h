// 文档校正器（BGR565）
// Created by Alex on 2025/5/24.
//

#ifndef DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTORRGB565_H
#define DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTORRGB565_H

#include "DocumentSkewCorrector.hpp"

namespace DR {

    class DocumentSkewCorrectorRGB565 final : public DocumentSkewCorrector {

    public:
        DocumentSkewCorrectorRGB565(int width, int height, void *BGR565) {
            mImage = cv::Mat(height, width, CV_8UC2, BGR565);
        }

        ~DocumentSkewCorrectorRGB565() override { mImage.release(); }

        void correct(float ltx, float lty, float rtx, float rty,
                    float lbx, float lby, float rbx, float rby, cv::Mat dst) const override {
            DocumentSkewCorrector::correct(mImage, dst, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
        }

    private:
        cv::Mat mImage;
    };
}

#endif //DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTORRGB565_H
