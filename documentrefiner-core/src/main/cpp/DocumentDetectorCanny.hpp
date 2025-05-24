// 使用 Canny 算法的检测器（位图处理逻辑）
// Created by Alex on 2025/5/24.
//

#ifndef DOCUMENTREFINER_DOCUMENTDETECTORCANNY_HPP
#define DOCUMENTREFINER_DOCUMENTDETECTORCANNY_HPP

#include "DocumentDetector.hpp"

namespace DR {
    class DocumentDetectorCanny final : public DocumentDetector {

    public:
        explicit DocumentDetectorCanny(int width, int height, void *BGR565) {
            cv::Mat image = cv::Mat(height, width, CV_8UC2, BGR565);
            mImage = cv::Mat(height, width, CV_8UC1);
            cv::cvtColor(image, mImage, cv::COLOR_BGR5652GRAY);
            image.release();
        }

        ~DocumentDetectorCanny() override { mImage.release(); }

        bool detect(int &ltx, int &lty, int &rtx, int &rty,
                    int &lbx, int &lby, int &rbx, int &rby) const override {
            if (detect(mImage, ltx, lty, rtx, rty, lbx, lby, rbx, rby)) {
                return true;
            }
            // 对灰度图做一次直方图均衡化增强对比度再进行一轮检测
            cv::Mat enhanced;
            cv::equalizeHist(mImage, enhanced);
            return detect(enhanced, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
        }

    private:
        cv::Mat mImage;

        static cv::Mat handleImage(const cv::Mat &image, int blurSize, int cannyThreshold2) {
            // 第一步高斯模糊
            cv::Mat blur;
            cv::GaussianBlur(image, blur, cv::Size(blurSize, blurSize), 0);
            // 第二步边缘检测
            cv::Mat canny;
            cv::Canny(blur, canny, 50, cannyThreshold2);
            // 第三步二值化
            cv::Mat threshold;
            cv::threshold(canny, threshold, 0, 255, cv::THRESH_OTSU);
            return threshold;
        }

        static bool detect(const cv::Mat &image, int &ltx, int &lty, int &rtx, int &rty,
                           int &lbx, int &lby, int &rbx, int &rby) {
            // 使用不同的高斯模糊大小与边框检测第二阈值进行边框检测
            const int cannyThreshold2s[] = {100, 150, 300};
            const int blurSizes[] = {3, 7, 11, 15};
            for (int cannyThreshold2: cannyThreshold2s) {
                for (int blurSize: blurSizes) {
                    if (calculateBounds(handleImage(image, blurSize, cannyThreshold2),
                                        ltx, lty, rtx, rty, lbx, lby, rbx, rby)) {
                        return true;
                    }
                }
            }
            return false;
        }
    };
}

#endif //DOCUMENTREFINER_DOCUMENTDETECTORCANNY_HPP
