package io.github.alexmofer.documentskewcorrection.ui;

import androidx.annotation.Nullable;

/**
 * 工具
 * 可使用以下工具库替换：
 * io.github.alexmofer.android.support.formulas.DistanceFormulas
 * io.github.alexmofer.android.support.formulas.IntersectFormulas
 * Created by Alex on 2025/5/28.
 */
final class Utils {

    private Utils() {
        //no instance
    }


    /**
     * 计算点位于直线的哪一边
     *
     * @param x  点X坐标
     * @param y  点Y坐标
     * @param x1 直线经过的点1的X轴坐标
     * @param y1 直线经过的点1的Y轴坐标
     * @param x2 直线经过的点2的X轴坐标
     * @param y2 直线经过的点2的Y轴坐标
     * @return 小于0：点在直线左侧；大于0：点在直线右侧；等于0：点在直线上。
     */
    public static double calculateSidePointToLine(double x, double y,
                                                  double x1, double y1, double x2, double y2) {
        // 直线方程：Ax + By + C = 0
        final double A = y2 - y1;
        final double B = x1 - x2;
        final double C = x2 * y1 - x1 * y2;
        return A * x + B * y + C;
    }

    /**
     * 判断点与矩形相交
     *
     * @param x      点X坐标
     * @param y      点Y坐标
     * @param left   矩形左边
     * @param top    矩形上边
     * @param right  矩形右边
     * @param bottom 矩形下边
     * @return 相交时返回true
     */
    public static boolean isIntersectPointToRect(double x, double y,
                                                 double left, double top,
                                                 double right, double bottom) {
        return left <= x && x <= right && top <= y && y <= bottom;
    }

    /**
     * 判断点与直线相交
     *
     * @param x  点X坐标
     * @param y  点Y坐标
     * @param x1 直线经过的点1的X轴坐标
     * @param y1 直线经过的点1的Y轴坐标
     * @param x2 直线经过的点2的X轴坐标
     * @param y2 直线经过的点2的Y轴坐标
     * @return 相交时返回true
     */
    public static boolean isIntersectPointToLine(double x, double y,
                                                 double x1, double y1, double x2, double y2) {
        return calculateSidePointToLine(x, y, x1, y1, x2, y2) == 0;
    }

    /**
     * 判断点与线段相交
     *
     * @param x  点X坐标
     * @param y  点Y坐标
     * @param x1 线段起点X坐标
     * @param y1 线段起点Y坐标
     * @param x2 线段终点X坐标
     * @param y2 线段终点Y坐标
     * @return 相交时返回true
     */
    public static boolean isIntersectPointToLineSegment(double x, double y,
                                                        double x1, double y1,
                                                        double x2, double y2) {
        if (isIntersectPointToLine(x, y, x1, y1, x2, y2)) {
            double l, t, r, b;
            l = r = x1;
            t = b = y1;
            l = Math.min(l, x2);
            t = Math.min(t, y2);
            r = Math.max(r, x2);
            b = Math.max(b, y2);
            return isIntersectPointToRect(x, y, l, t, r, b);
        }
        return false;
    }

    /**
     * 计算两条线段交点
     *
     * @param x1 线段1起点X轴坐标
     * @param y1 线段1起点Y轴坐标
     * @param x2 线段1终点X轴坐标
     * @param y2 线段1终点Y轴坐标
     * @param x3 线段2起点X轴坐标
     * @param y3 线段2起点Y轴坐标
     * @param x4 线段2终点X轴坐标
     * @param y4 线段2终点Y轴坐标
     * @return 交点，null：无焦点；length为2：单个交点
     */
    @Nullable
    public static double[] calculateIntersectionLineSegmentToLineSegment(double x1, double y1,
                                                                         double x2, double y2,
                                                                         double x3, double y3,
                                                                         double x4, double y4) {
        final double dx1 = x1 - x2;
        final double dx2 = x3 - x4;
        if (dx1 == 0 && dx2 == 0) {
            // 均垂直于X轴，平行线不相交
            return null;
        }
        if (dx1 == 0) {
            // 一条垂直一条不垂直，相交
            final double y;
            final double k2 = (y3 - y4) / dx2;
            if (k2 == 0) {
                y = y3;
            } else {
                y = k2 * (x1 - x4) + y4;
            }
            if (isIntersectPointToLineSegment(x1, y, x1, y1, x2, y2) &&
                    isIntersectPointToLineSegment(x1, y, x3, y3, x4, y4)) {
                return new double[]{x1, y};
            } else {
                return null;
            }
        }
        if (dx2 == 0) {
            // 一条垂直一条不垂直，相交
            final double y;
            final double k1 = (y1 - y2) / dx1;
            if (k1 == 0) {
                y = y1;
            } else {
                y = k1 * (x3 - x2) + y2;
            }
            if (isIntersectPointToLineSegment(x3, y, x1, y1, x2, y2) &&
                    isIntersectPointToLineSegment(x3, y, x3, y3, x4, y4)) {
                return new double[]{x3, y};
            } else {
                return null;
            }
        }
        final double k1 = (y1 - y2) / dx1;
        final double k2 = (y3 - y4) / dx2;
        if (k1 == k2) {
            // 相同斜率，不相交
            return null;
        }
        final double x;
        final double y;
        if (k1 == 0) {
            y = y1;
            x = (y - y4) / k2 + x4;
        } else if (k2 == 0) {
            y = y3;
            x = (y - y2) / k1 + x2;
        } else {
            x = (k1 * x2 - k2 * x4 + y4 - y2) / (k1 - k2);
            y = k1 * (x - x2) + y2;
        }
        if (isIntersectPointToLineSegment(x, y, x1, y1, x2, y2) &&
                isIntersectPointToLineSegment(x, y, x3, y3, x4, y4)) {
            return new double[]{x, y};
        } else {
            return null;
        }
    }


    /**
     * 计算点与点之间的距离
     *
     * @param x1 点1X轴坐标
     * @param y1 点1Y轴坐标
     * @param x2 点2X轴坐标
     * @param y2 点2Y轴坐标
     * @return 距离
     */
    public static double calculatePointToPoint(double x1, double y1, double x2, double y2) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
