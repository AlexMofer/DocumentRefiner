// 使用代理了 Canny 算法的检测器
// Created by Alex on 2025/5/24.
//

#ifndef DOCUMENTREFINER_DOCUMENTDETECTORDELEGATED_HPP
#define DOCUMENTREFINER_DOCUMENTDETECTORDELEGATED_HPP

#include "DocumentDetector.hpp"

namespace DR {
    class DocumentDetectorDelegated final : public DocumentDetector {

    public:
        explicit DocumentDetectorDelegated(int width, int height, void *BGR565) {
            cv::Mat image = cv::Mat(height, width, CV_8UC2, BGR565);
            mImage = cv::Mat(height, width, CV_8UC1);
            cv::cvtColor(image, mImage, cv::COLOR_BGR5652GRAY);
            image.release();
        }

        ~DocumentDetectorDelegated() override { mImage.release(); }

        bool detect(int &ltx, int &lty, int &rtx, int &rty,
                    int &lbx, int &lby, int &rbx, int &rby) const override {
            return calculateBounds(mImage, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
        }

    private:
        cv::Mat mImage;
    };
}

#endif //DOCUMENTREFINER_DOCUMENTDETECTORDELEGATED_HPP
