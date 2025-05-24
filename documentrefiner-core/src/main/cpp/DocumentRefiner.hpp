// 文档校正器（透视输出）
// Created by Alex on 2025/5/20.
//

#ifndef DOCUMENTREFINER_DOCUMENTREFINER_HPP
#define DOCUMENTREFINER_DOCUMENTREFINER_HPP

#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc.hpp>

namespace DR {

    class DocumentRefiner {

    public:
        DocumentRefiner() = default;

        virtual ~DocumentRefiner() = default;

        /**
         * 校正输出
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
        virtual void refine(float ltx, float lty, float rtx, float rty,
                            float lbx, float lby, float rbx, float rby, cv::Mat dst) const = 0;

    protected:
        static void refine(const cv::Mat &src, cv::Mat dst,
                           float ltx, float lty, float rtx, float rty,
                           float lbx, float lby, float rbx, float rby) {
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
            cv::warpPerspective(src, dst, cv::getPerspectiveTransform(srcRect, dstRect),
                                dst.size());
        }
    };
}

#endif //DOCUMENTREFINER_DOCUMENTREFINER_HPP
