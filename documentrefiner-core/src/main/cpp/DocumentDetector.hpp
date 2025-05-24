// 文档探测器（边框检测逻辑集中在此）
// Created by Alex on 2025/5/20.
//

#ifndef DOCUMENTREFINER_DOCUMENTDETECTOR_HPP
#define DOCUMENTREFINER_DOCUMENTDETECTOR_HPP

#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc.hpp>

namespace DR {

    class DocumentDetector {

    public:
        DocumentDetector() = default;

        virtual ~DocumentDetector() = default;

        /**
         * 检测边框
         * @param ltx 待输出的左上点X轴坐标
         * @param lty 待输出的左上点Y轴坐标
         * @param rtx 待输出的右上点X轴坐标
         * @param rty 待输出的右上点Y轴坐标
         * @param lbx 待输出的左下点X轴坐标
         * @param lby 待输出的左下点Y轴坐标
         * @param rbx 待输出的右下点X轴坐标
         * @param rby 待输出的右下点Y轴坐标
         * @return 检测成功时返回true
         */
        virtual bool detect(int &ltx, int &lty, int &rtx, int &rty,
                    int &lbx, int &lby, int &rbx, int &rby) const = 0;
    protected:
        /**
         * 检测边框
         * @param image 二值化图或者灰度图
         * @param ltx 待输出的左上点X轴坐标
         * @param lty 待输出的左上点Y轴坐标
         * @param rtx 待输出的右上点X轴坐标
         * @param rty 待输出的右上点Y轴坐标
         * @param lbx 待输出的左下点X轴坐标
         * @param lby 待输出的左下点Y轴坐标
         * @param rbx 待输出的右下点X轴坐标
         * @param rby 待输出的右下点Y轴坐标
         * @return 检测成功时返回true
         */
        static bool calculateBounds(const cv::Mat &image, int &ltx, int &lty, int &rtx, int &rty,
                                    int &lbx, int &lby, int &rbx, int &rby) {
            // 计算边框
            std::vector<cv::Point> result;
            std::vector<std::vector<cv::Point>> contours;
            //提取边框
            cv::findContours(image, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_NONE);
            //按面积排序
            std::sort(contours.begin(), contours.end(), sortByArea);
            if (contours.size() > 0) {
                std::vector<cv::Point> contour = contours[0];
                double arc = arcLength(contour, true);
                std::vector<cv::Point> outDP;
                //多变形逼近
                cv::approxPolyDP(cv::Mat(contour), outDP, 0.01 * arc, true);
                //筛选去除相近的点
                std::vector<cv::Point> selectedPoints = selectPoints(outDP);
                if (selectedPoints.size() != 4) {
                    //如果筛选出来之后不是四边形
                    return false;
                } else {
                    int widthMin = selectedPoints[0].x;
                    int widthMax = selectedPoints[0].x;
                    int heightMin = selectedPoints[0].y;
                    int heightMax = selectedPoints[0].y;
                    for (int k = 0; k < 4; k++) {
                        if (selectedPoints[k].x < widthMin) {
                            widthMin = selectedPoints[k].x;
                        }
                        if (selectedPoints[k].x > widthMax) {
                            widthMax = selectedPoints[k].x;
                        }
                        if (selectedPoints[k].y < heightMin) {
                            heightMin = selectedPoints[k].y;
                        }
                        if (selectedPoints[k].y > heightMax) {
                            heightMax = selectedPoints[k].y;
                        }
                    }
                    //选择区域外围矩形面积
                    int selectArea = (widthMax - widthMin) * (heightMax - heightMin);
                    int imageArea = image.cols * image.rows;
                    if (selectArea < (imageArea / 20)) {
                        result.clear();
                        //筛选出来的区域太小
                        return false;
                    } else {
                        result = selectedPoints;
                        if (result.size() != 4) {
                            cv::Point2f p[4];
                            p[0] = cv::Point2f(0, 0);
                            p[1] = cv::Point2f(image.cols, 0);
                            p[2] = cv::Point2f(image.cols, image.rows);
                            p[3] = cv::Point2f(0, image.rows);
                            result.push_back(p[0]);
                            result.push_back(p[1]);
                            result.push_back(p[2]);
                            result.push_back(p[3]);
                        }
                        // 按左上，右上，左上，右下排序
                        std::vector<cv::Point> points = sortPointClockwise(result);
                        ltx = points[0].x;
                        lty = points[0].y;
                        rtx = points[1].x;
                        rty = points[1].y;
                        rbx = points[2].x;
                        rby = points[2].y;
                        lbx = points[3].x;
                        lby = points[3].y;
                        return true;
                    }
                }
            }
            return false;
        }

    private:
        static bool sortByArea(const std::vector<cv::Point> &v1, const std::vector<cv::Point> &v2) {
            double v1Area = fabs(contourArea(cv::Mat(v1)));
            double v2Area = fabs(contourArea(cv::Mat(v2)));
            return v1Area > v2Area;
        }

        //type代表左上，左下，右上，右下等方位
        static cv::Point choosePoint(cv::Point center, std::vector<cv::Point> &points, int type) {
            int index = -1;
            int minDis = 0;
            //四个堆都是选择距离中心点较远的点
            if (type == 0) {
                for (int i = 0; i < points.size(); i++) {
                    if (points[i].x < center.x && points[i].y < center.y) {
                        int dis = static_cast<int>(sqrt(pow((points[i].x - center.x), 2) +
                                                        pow((points[i].y - center.y), 2)));
                        if (dis > minDis) {
                            index = i;
                            minDis = dis;
                        }
                    }
                }
            } else if (type == 1) {
                for (int i = 0; i < points.size(); i++) {
                    if (points[i].x < center.x && points[i].y > center.y) {
                        int dis = static_cast<int>(sqrt(pow((points[i].x - center.x), 2) +
                                                        pow((points[i].y - center.y), 2)));
                        if (dis > minDis) {
                            index = i;
                            minDis = dis;
                        }
                    }
                }
            } else if (type == 2) {
                for (int i = 0; i < points.size(); i++) {
                    if (points[i].x > center.x && points[i].y < center.y) {
                        int dis = static_cast<int>(sqrt(pow((points[i].x - center.x), 2) +
                                                        pow((points[i].y - center.y), 2)));
                        if (dis > minDis) {
                            index = i;
                            minDis = dis;
                        }
                    }
                }

            } else if (type == 3) {
                for (int i = 0; i < points.size(); i++) {
                    if (points[i].x > center.x && points[i].y > center.y) {
                        int dis = static_cast<int>(sqrt(pow((points[i].x - center.x), 2) +
                                                        pow((points[i].y - center.y), 2)));
                        if (dis > minDis) {
                            index = i;
                            minDis = dis;
                        }
                    }
                }
            }

            if (index != -1) {
                return cv::Point(points[index].x, points[index].y);
            }
            return cv::Point(0, 0);
        }

        static std::vector<cv::Point> selectPoints(std::vector<cv::Point> points) {
            if (points.size() > 4) {
                cv::Point &p = points[0];
                int minX = p.x;
                int maxX = p.x;
                int minY = p.y;
                int maxY = p.y;
                //得到一个矩形去包住所有点
                for (int i = 1; i < points.size(); i++) {
                    if (points[i].x < minX) {
                        minX = points[i].x;
                    }
                    if (points[i].x > maxX) {
                        maxX = points[i].x;
                    }
                    if (points[i].y < minY) {
                        minY = points[i].y;
                    }
                    if (points[i].y > maxY) {
                        maxY = points[i].y;
                    }
                }
                //矩形中心点
                cv::Point center = cv::Point((minX + maxX) / 2, (minY + maxY) / 2);
                //分别得出左上，左下，右上，右下四堆中的结果点
                cv::Point p0 = choosePoint(center, points, 0);
                cv::Point p1 = choosePoint(center, points, 1);
                cv::Point p2 = choosePoint(center, points, 2);
                cv::Point p3 = choosePoint(center, points, 3);
                points.clear();
                //如果得到的点不是０，即是得到的结果点
                if (!(p0.x == 0 && p0.y == 0)) {
                    points.push_back(p0);
                }
                if (!(p1.x == 0 && p1.y == 0)) {
                    points.push_back(p1);
                }
                if (!(p2.x == 0 && p2.y == 0)) {
                    points.push_back(p2);
                }
                if (!(p3.x == 0 && p3.y == 0)) {
                    points.push_back(p3);
                }
            }
            return points;
        }

        static long long pointSideLine(cv::Point &lineP1, cv::Point &lineP2, cv::Point &point) {
            long x1 = lineP1.x;
            long y1 = lineP1.y;
            long x2 = lineP2.x;
            long y2 = lineP2.y;
            long x = point.x;
            long y = point.y;
            return (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1);
        }

        static std::vector<cv::Point> sortPointClockwise(std::vector<cv::Point> points) {
            if (points.size() != 4) {
                return points;
            }

            cv::Point unFoundPoint;
            std::vector<cv::Point> result = {unFoundPoint, unFoundPoint, unFoundPoint,
                                             unFoundPoint};

            long minDistance = -1;
            for (cv::Point &point: points) {
                long distance = point.x * point.x + point.y * point.y;
                if (minDistance == -1 || distance < minDistance) {
                    result[0] = point;
                    minDistance = distance;
                }
            }
            if (result[0] != unFoundPoint) {
                cv::Point &leftTop = result[0];
                points.erase(std::remove(points.begin(), points.end(), leftTop));
                if ((pointSideLine(leftTop, points[0], points[1]) *
                     pointSideLine(leftTop, points[0], points[2])) < 0) {
                    result[2] = points[0];
                } else if ((pointSideLine(leftTop, points[1], points[0]) *
                            pointSideLine(leftTop, points[1], points[2])) < 0) {
                    result[2] = points[1];
                } else if ((pointSideLine(leftTop, points[2], points[0]) *
                            pointSideLine(leftTop, points[2], points[1])) < 0) {
                    result[2] = points[2];
                }
            }
            if (result[0] != unFoundPoint && result[2] != unFoundPoint) {
                cv::Point &leftTop = result[0];
                cv::Point &rightBottom = result[2];
                points.erase(std::remove(points.begin(), points.end(), rightBottom));
                if (pointSideLine(leftTop, rightBottom, points[0]) > 0) {
                    result[1] = points[0];
                    result[3] = points[1];
                } else {
                    result[1] = points[1];
                    result[3] = points[0];
                }
            }

            if (result[0] != unFoundPoint && result[1] != unFoundPoint &&
                result[2] != unFoundPoint && result[3] != unFoundPoint) {
                return result;
            }

            return points;
        }
    };
}

#endif //DOCUMENTREFINER_DOCUMENTDETECTOR_HPP
