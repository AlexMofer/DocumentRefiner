// 使用代理了 Canny 算法的检测器
// Created by Alex on 2025/5/24.
//

#ifndef DOCUMENTSKEWCORRECTION_DOCUMENTSKEWDETECTORDELEGATED_HPP
#define DOCUMENTSKEWCORRECTION_DOCUMENTSKEWDETECTORDELEGATED_HPP

#include "DocumentSkewDetector.hpp"

namespace DR {
    class DocumentSkewDetectorDelegated final : public DocumentSkewDetector {

    public:
        explicit DocumentSkewDetectorDelegated(int width, int height, void *BGR565) {
            cv::Mat image = cv::Mat(height, width, CV_8UC2, BGR565);
            mImage = cv::Mat(height, width, CV_8UC1);
            cv::cvtColor(image, mImage, cv::COLOR_BGR5652GRAY);
            image.release();
        }

        ~DocumentSkewDetectorDelegated() override { mImage.release(); }

        bool detect(int &ltx, int &lty, int &rtx, int &rty,
                    int &lbx, int &lby, int &rbx, int &rby) const override {
            return calculateBounds(mImage, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
        }

    private:
        cv::Mat mImage;
    };
}

#endif //DOCUMENTSKEWCORRECTION_DOCUMENTSKEWDETECTORDELEGATED_HPP
