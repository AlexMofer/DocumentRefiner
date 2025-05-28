package io.github.alexmofer.documentskewcorrection.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Magnifier;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.util.TypedValueCompat;

import java.util.Arrays;

/**
 * 校正点控制器
 * Created by Alex on 2025/5/28.
 */
public class DocumentSkewCorrectionView extends AppCompatImageView {
    private static final int POINT_NONE = 0;
    private static final int POINT_LT = 1;
    private static final int POINT_RT = 2;
    private static final int POINT_LB = 3;
    private static final int POINT_RB = 4;
    private final float[] tPoints = new float[8];
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();
    private final float[] mTouchDownPoints = new float[8];
    private int mStrokeColor = Color.BLUE;
    private int mFillColor = Color.WHITE;
    private float[] mPoints;
    private int mPointRadius;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private float mOffsetX;
    private float mOffsetY;
    private float mLTX;
    private float mLTY;
    private float mRTX;
    private float mRTY;
    private float mLBX;
    private float mLBY;
    private float mRBX;
    private float mRBY;
    private int mTouchPoint = POINT_NONE;
    private float mTouchOffsetX = 0;
    private float mTouchOffsetY = 0;
    private float mTouchSlop;
    private float mTouchPointX;
    private float mTouchPointY;
    private Object mMagnifier;
    private float mMagnifierOffset;

    public DocumentSkewCorrectionView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public DocumentSkewCorrectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public DocumentSkewCorrectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private static int adjustPoints(float[] points, int controlPoint, int w, int h,
                                    float ltx, float lty, float rtx, float rty,
                                    float lbx, float lby, float rbx, float rby) {
        if (controlPoint == POINT_LT) {
            // 变更左上点
            // 情况1：LT与LB连线与RT与RB连线出现焦点，LT与RT互换
            final double[] p1 = Utils.calculateIntersectionLineSegmentToLineSegment(
                    ltx, lty, lbx, lby, rtx, rty, rbx, rby);
            if (p1 != null) {
                // 交换
                points[0] = rtx;
                points[1] = rty;
                points[2] = ltx;
                points[3] = lty;
                points[4] = lbx;
                points[5] = lby;
                points[6] = rbx;
                points[7] = rby;
                return POINT_RT;
            }
            // 情况2：LT与RT连线与LB与RB连续出现焦点，LT与LB互换
            final double[] p2 = Utils.calculateIntersectionLineSegmentToLineSegment(
                    ltx, lty, rtx, rty, lbx, lby, rbx, rby);
            if (p2 != null) {
                // 交换
                points[0] = lbx;
                points[1] = lby;
                points[2] = rtx;
                points[3] = rty;
                points[4] = ltx;
                points[5] = lty;
                points[6] = rbx;
                points[7] = rby;
                return POINT_LB;
            }
            // 情况3：LT与(0,0)的距离大于RB与(0,0)的距离，LT与RB互换
            final double d1 = Utils.calculatePointToPoint(0, 0, ltx, lty);
            final double d2 = Utils.calculatePointToPoint(0, 0, rbx, rby);
            if (d1 > d2) {
                // 交换
                points[0] = rbx;
                points[1] = rby;
                points[2] = rtx;
                points[3] = rty;
                points[4] = lbx;
                points[5] = lby;
                points[6] = ltx;
                points[7] = lty;
                return POINT_RB;
            }
        } else if (controlPoint == POINT_RT) {
            // 变更右上点
            // 情况1：LT与LB连线与RT与RB连线出现焦点，RT与LT互换
            final double[] p1 = Utils.calculateIntersectionLineSegmentToLineSegment(
                    ltx, lty, lbx, lby, rtx, rty, rbx, rby);
            if (p1 != null) {
                // 交换
                points[0] = rtx;
                points[1] = rty;
                points[2] = ltx;
                points[3] = lty;
                points[4] = lbx;
                points[5] = lby;
                points[6] = rbx;
                points[7] = rby;
                return POINT_LT;
            }
            // 情况2：LT与RT连线与LB与RB连续出现焦点，RT与RB互换
            final double[] p2 = Utils.calculateIntersectionLineSegmentToLineSegment(
                    ltx, lty, rtx, rty, lbx, lby, rbx, rby);
            if (p2 != null) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = rbx;
                points[3] = rby;
                points[4] = lbx;
                points[5] = lby;
                points[6] = rtx;
                points[7] = rty;
                return POINT_RB;
            }
            // 情况3：RT与(w,0)的距离大于LB与(w,0)的距离，RT与LB互换
            final double d1 = Utils.calculatePointToPoint(w, 0, rtx, rty);
            final double d2 = Utils.calculatePointToPoint(w, 0, lbx, lby);
            if (d1 > d2) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = lbx;
                points[3] = lby;
                points[4] = rtx;
                points[5] = rty;
                points[6] = rbx;
                points[7] = rby;
                return POINT_LB;
            }
        } else if (controlPoint == POINT_LB) {
            // 变更左下点
            // 情况1：LT与LB连线与RT与RB连线出现焦点，LB与RB互换
            final double[] p1 = Utils.calculateIntersectionLineSegmentToLineSegment(
                    ltx, lty, lbx, lby, rtx, rty, rbx, rby);
            if (p1 != null) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = rtx;
                points[3] = rty;
                points[4] = rbx;
                points[5] = rby;
                points[6] = lbx;
                points[7] = lby;
                return POINT_RB;
            }
            // 情况2：LT与RT连线与LB与RB连续出现焦点，LB与LT互换
            final double[] p2 = Utils.calculateIntersectionLineSegmentToLineSegment(
                    ltx, lty, rtx, rty, lbx, lby, rbx, rby);
            if (p2 != null) {
                // 交换
                points[0] = lbx;
                points[1] = lby;
                points[2] = rtx;
                points[3] = rty;
                points[4] = ltx;
                points[5] = lty;
                points[6] = rbx;
                points[7] = rby;
                return POINT_LT;
            }
            // 情况3：LB与(0,h)的距离大于RT与(0,h)的距离，LB与RT互换
            final double d1 = Utils.calculatePointToPoint(0, h, lbx, lby);
            final double d2 = Utils.calculatePointToPoint(0, h, rtx, rty);
            if (d1 > d2) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = lbx;
                points[3] = lby;
                points[4] = rtx;
                points[5] = rty;
                points[6] = rbx;
                points[7] = rby;
                return POINT_RT;
            }
        } else if (controlPoint == POINT_RB) {
            // 变更右下点
            // 情况1：LT与LB连线与RT与RB连线出现焦点，RB与LB互换
            final double[] p1 = Utils.calculateIntersectionLineSegmentToLineSegment(
                    ltx, lty, lbx, lby, rtx, rty, rbx, rby);
            if (p1 != null) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = rtx;
                points[3] = rty;
                points[4] = rbx;
                points[5] = rby;
                points[6] = lbx;
                points[7] = lby;
                return POINT_LB;
            }
            // 情况2：LT与RT连线与LB与RB连续出现焦点，RB与RT互换
            final double[] p2 = Utils.calculateIntersectionLineSegmentToLineSegment(
                    ltx, lty, rtx, rty, lbx, lby, rbx, rby);
            if (p2 != null) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = rbx;
                points[3] = rby;
                points[4] = lbx;
                points[5] = lby;
                points[6] = rtx;
                points[7] = rty;
                return POINT_RT;
            }
            // 情况3：RB与(w,h)的距离大于LT与(w,h)的距离，RB与LT互换
            final double d1 = Utils.calculatePointToPoint(w, h, rbx, rby);
            final double d2 = Utils.calculatePointToPoint(w, h, ltx, lty);
            if (d1 > d2) {
                // 交换
                points[0] = rbx;
                points[1] = rby;
                points[2] = rtx;
                points[3] = rty;
                points[4] = lbx;
                points[5] = lby;
                points[6] = ltx;
                points[7] = lty;
                return POINT_LT;
            }
        }
        // 默认
        points[0] = ltx;
        points[1] = lty;
        points[2] = rtx;
        points[3] = rty;
        points[4] = lbx;
        points[5] = lby;
        points[6] = rbx;
        points[7] = rby;
        return controlPoint;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        float strokeWidth = TypedValueCompat.dpToPx(2, metrics);
        mPointRadius = Math.round(TypedValueCompat.dpToPx(8, metrics));
        mMagnifierOffset = TypedValueCompat.dpToPx(64, metrics);
        try (final TypedArray custom = context.obtainStyledAttributes(attrs,
                R.styleable.DocumentSkewCorrectionView, defStyleAttr, 0)) {
            mStrokeColor = custom.getColor(
                    R.styleable.DocumentSkewCorrectionView_android_strokeColor, mStrokeColor);
            mFillColor = custom.getColor(
                    R.styleable.DocumentSkewCorrectionView_android_fillColor, mFillColor);
            if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_android_lineHeight)) {
                strokeWidth = custom.getDimension(
                        R.styleable.DocumentSkewCorrectionView_android_lineHeight, strokeWidth);
            }
            if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_DSC_strokeWidth)) {
                strokeWidth = custom.getDimension(
                        R.styleable.DocumentSkewCorrectionView_DSC_strokeWidth, strokeWidth);
            }
            if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_android_innerRadius)) {
                mPointRadius = custom.getDimensionPixelOffset(
                        R.styleable.DocumentSkewCorrectionView_android_innerRadius, mPointRadius);
            }
            if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_DSC_pointRadius)) {
                mPointRadius = custom.getDimensionPixelOffset(
                        R.styleable.DocumentSkewCorrectionView_DSC_pointRadius, mPointRadius);
            }
            if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_DSC_magnifierOffset)) {
                mMagnifierOffset = custom.getDimension(
                        R.styleable.DocumentSkewCorrectionView_DSC_magnifierOffset, mMagnifierOffset);
            }
        }
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mMagnifier = new Magnifier(this);
        }
        mPaint.setStrokeWidth(strokeWidth);
        mTouchSlop = mPointRadius + ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Drawable src = getDrawable();
        if (src == null) {
            return;
        }
        refreshParams();
        canvas.save();
        canvas.translate(mOffsetX, mOffsetY);
        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mPath, mPaint);
        mPaint.setColor(mFillColor);
        mPaint.setStyle(Paint.Style.FILL);
        if (mTouchPoint != POINT_LT) {
            canvas.drawCircle(mLTX, mLTY, mPointRadius, mPaint);
        }
        if (mTouchPoint != POINT_RT) {
            canvas.drawCircle(mRTX, mRTY, mPointRadius, mPaint);
        }
        if (mTouchPoint != POINT_RB) {
            canvas.drawCircle(mRBX, mRBY, mPointRadius, mPaint);
        }
        if (mTouchPoint != POINT_LB) {
            canvas.drawCircle(mLBX, mLBY, mPointRadius, mPaint);
        }
        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Paint.Style.STROKE);
        if (mTouchPoint != POINT_LT) {
            canvas.drawCircle(mLTX, mLTY, mPointRadius, mPaint);
        }
        if (mTouchPoint != POINT_RT) {
            canvas.drawCircle(mRTX, mRTY, mPointRadius, mPaint);
        }
        if (mTouchPoint != POINT_RB) {
            canvas.drawCircle(mRBX, mRBY, mPointRadius, mPaint);
        }
        if (mTouchPoint != POINT_LB) {
            canvas.drawCircle(mLBX, mLBY, mPointRadius, mPaint);
        }
        canvas.restore();
    }

    private void refreshParams() {
        final Drawable src = getDrawable();
        if (src != null) {
            final int drawableWidth = src.getIntrinsicWidth();
            final int drawableHeight = src.getIntrinsicHeight();
            final int paddingLeft = getPaddingLeft();
            final int paddingRight = getPaddingRight();
            final int paddingTop = getPaddingTop();
            final int paddingBottom = getPaddingBottom();
            mOffsetX = paddingLeft + (getWidth() - paddingLeft - paddingRight) * 0.5f - drawableWidth * 0.5f;
            mOffsetY = paddingTop + (getHeight() - paddingTop - paddingBottom) * 0.5f - drawableHeight * 0.5f;
            if (drawableWidth != mDrawableWidth || drawableHeight != mDrawableHeight) {
                mDrawableWidth = drawableWidth;
                mDrawableHeight = drawableHeight;
                if (mPoints == null) {
                    mLTX = 0;
                    mLTY = 0;
                    mRTX = mDrawableWidth;
                    mRTY = 0;
                    mLBX = 0;
                    mLBY = mDrawableHeight;
                    mRBX = mDrawableWidth;
                    mRBY = mDrawableHeight;
                } else {
                    mLTX = mPoints[0] * mDrawableWidth;
                    mLTY = mPoints[1] * mDrawableHeight;
                    mRTX = mPoints[2] * mDrawableWidth;
                    mRTY = mPoints[3] * mDrawableHeight;
                    mLBX = mPoints[4] * mDrawableWidth;
                    mLBY = mPoints[5] * mDrawableHeight;
                    mRBX = mPoints[6] * mDrawableWidth;
                    mRBY = mPoints[7] * mDrawableHeight;
                }
                refreshPath();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final boolean superResult = super.onTouchEvent(event);
        final Drawable src = getDrawable();
        if (src == null) {
            return superResult;
        }
        final int action = event.getAction();
        final float x = event.getX() - mOffsetX;
        final float y = event.getY() - mOffsetY;
        if (action == MotionEvent.ACTION_DOWN) {
            // 确定当前触摸点为原始控制点的哪一个
            mTouchPoint = getTouchPoint(x, y);
            mTouchPointX = -1;
            mTouchPointY = -1;
            if (mTouchPoint == POINT_LT) {
                startTouch(x, y, mLTX, mLTY);
                invalidate();
                showMagnifier(event.getX(), event.getY());
                return true;
            } else if (mTouchPoint == POINT_RT) {
                startTouch(x, y, mRTX, mRTY);
                invalidate();
                showMagnifier(event.getX(), event.getY());
                return true;
            } else if (mTouchPoint == POINT_LB) {
                startTouch(x, y, mLBX, mLBY);
                invalidate();
                showMagnifier(event.getX(), event.getY());
                return true;
            } else if (mTouchPoint == POINT_RB) {
                startTouch(x, y, mRBX, mRBY);
                invalidate();
                showMagnifier(event.getX(), event.getY());
                return true;
            } else {
                return superResult;
            }
        }
        if (mTouchPoint == POINT_NONE) {
            return superResult;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            mLTX = mTouchDownPoints[0];
            mLTY = mTouchDownPoints[1];
            mRTX = mTouchDownPoints[2];
            mRTY = mTouchDownPoints[3];
            mLBX = mTouchDownPoints[4];
            mLBY = mTouchDownPoints[5];
            mRBX = mTouchDownPoints[6];
            mRBY = mTouchDownPoints[7];
            refreshPath();
            mTouchPoint = POINT_NONE;
            invalidate();
            dismissMagnifier();
            return superResult;
        }
        final float pointX = Math.max(0, Math.min(mDrawableWidth, (x + mTouchOffsetX)));
        final float pointY = Math.max(0, Math.min(mDrawableHeight, (y + mTouchOffsetY)));
        if (mTouchPointX != pointX || mTouchPointY != pointY) {
            mTouchPointX = pointX;
            mTouchPointY = pointY;
            final float[] points = tPoints;
            if (mTouchPoint == POINT_LT) {
                mTouchPoint = adjustPoints(points, mTouchPoint, mDrawableWidth, mDrawableHeight,
                        mTouchPointX, mTouchPointY, mRTX, mRTY, mLBX, mLBY, mRBX, mRBY);
            } else if (mTouchPoint == POINT_RT) {
                mTouchPoint = adjustPoints(points, mTouchPoint, mDrawableWidth, mDrawableHeight,
                        mLTX, mLTY, mTouchPointX, mTouchPointY, mLBX, mLBY, mRBX, mRBY);
            } else if (mTouchPoint == POINT_LB) {
                mTouchPoint = adjustPoints(points, mTouchPoint, mDrawableWidth, mDrawableHeight,
                        mLTX, mLTY, mRTX, mRTY, mTouchPointX, mTouchPointY, mRBX, mRBY);
            } else if (mTouchPoint == POINT_RB) {
                mTouchPoint = adjustPoints(points, mTouchPoint, mDrawableWidth, mDrawableHeight,
                        mLTX, mLTY, mRTX, mRTY, mLBX, mLBY, mTouchPointX, mTouchPointY);
            }
            mLTX = points[0];
            mLTY = points[1];
            mRTX = points[2];
            mRTY = points[3];
            mLBX = points[4];
            mLBY = points[5];
            mRBX = points[6];
            mRBY = points[7];
            refreshPath();
            invalidate();
        }
        if (action == MotionEvent.ACTION_UP) {
            mTouchPoint = POINT_NONE;
            invalidate();
            dismissMagnifier();
        } else {
            showMagnifier(event.getX(), event.getY());
        }
        return true;
    }

    /**
     * 获取校正点
     *
     * @return 校正点
     */
    @Nullable
    public float[] getPoints() {
        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            return mPoints;
        }
        final float[] points = new float[8];
        points[0] = mLTX / mDrawableWidth;
        points[1] = mLTY / mDrawableHeight;
        points[2] = mRTX / mDrawableWidth;
        points[3] = mRTY / mDrawableHeight;
        points[4] = mLBX / mDrawableWidth;
        points[5] = mLBY / mDrawableHeight;
        points[6] = mRBX / mDrawableWidth;
        points[7] = mRBY / mDrawableHeight;
        return points;
    }

    /**
     * 设置校正点
     *
     * @param points 校正点
     */
    public void setPoints(@Nullable float[] points) {
        if (Arrays.equals(mPoints, points)) {
            return;
        }
        mPoints = points;
        mTouchPoint = POINT_NONE;
        // 置0用于刷新参数
        mDrawableWidth = 0;
        mDrawableHeight = 0;
        invalidate();
    }

    private void refreshPath() {
        mPath.rewind();
        mPath.moveTo(mLTX, mLTY);
        mPath.lineTo(mRTX, mRTY);
        mPath.lineTo(mRBX, mRBY);
        mPath.lineTo(mLBX, mLBY);
        mPath.close();
    }

    private int getTouchPoint(float x, float y) {
        double distance = Utils.calculatePointToPoint(x, y, mLBX, mLBY);
        if (mTouchSlop > distance) {
            return POINT_LB;
        }
        distance = Utils.calculatePointToPoint(x, y, mRBX, mRBY);
        if (mTouchSlop > distance) {
            return POINT_RB;
        }
        distance = Utils.calculatePointToPoint(x, y, mRTX, mRTY);
        if (mTouchSlop > distance) {
            return POINT_RT;
        }
        distance = Utils.calculatePointToPoint(x, y, mLTX, mLTY);
        if (mTouchSlop > distance) {
            return POINT_LT;
        }
        return POINT_NONE;
    }

    private void startTouch(float touchX, float touchY, float pointX, float pointY) {
        mTouchDownPoints[0] = mLTX;
        mTouchDownPoints[1] = mLTY;
        mTouchDownPoints[2] = mRTX;
        mTouchDownPoints[3] = mRTY;
        mTouchDownPoints[4] = mLBX;
        mTouchDownPoints[5] = mLBY;
        mTouchDownPoints[6] = mRBX;
        mTouchDownPoints[7] = mRBY;
        mTouchOffsetX = pointX - touchX;
        mTouchOffsetY = pointY - touchY;
    }

    private void showMagnifier(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ((Magnifier) mMagnifier).show(x, y, x, y - mMagnifierOffset);
            return;
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            ((Magnifier) mMagnifier).show(x, y);
        }
    }

    private void dismissMagnifier() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ((Magnifier) mMagnifier).dismiss();
        }
    }
}
