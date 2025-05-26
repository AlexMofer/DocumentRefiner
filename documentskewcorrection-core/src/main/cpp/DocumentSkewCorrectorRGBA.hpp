// 文档校正器（RGBA未预乘）
// Created by Alex on 2025/5/20.
//

#ifndef DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTORRGBA_HPP
#define DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTORRGBA_HPP

#include "DocumentSkewCorrector.hpp"

namespace DR {

    class DocumentSkewCorrectorRGBA final : public DocumentSkewCorrector {

    public:
        DocumentSkewCorrectorRGBA(int width, int height, void *RGBA) {
            mImage = cv::Mat(height, width, CV_8UC4, RGBA);
        }

        ~DocumentSkewCorrectorRGBA() override { mImage.release(); }

        void correct(float ltx, float lty, float rtx, float rty,
                    float lbx, float lby, float rbx, float rby, cv::Mat dst) const override {
            DocumentSkewCorrector::correct(mImage, dst, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
        }

    private:
        cv::Mat mImage;
    };
}

#endif //DOCUMENTSKEWCORRECTION_DOCUMENTSKEWCORRECTORRGBA_HPP
