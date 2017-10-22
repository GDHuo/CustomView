package huo.com.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Lenovo on 2017/10/20.
 */

public class CustomView extends View{

    private final int mTitleTextSize;
    private final String mTitleText;
    private final int mTextColor;
    private final Paint mPaint;
    private final Rect mTextBound;
    private final Bitmap mImage;
    private final int mImageScale;
    private final Rect rect;
    private int mWidth;
    private int mHeight;
    private static final int IMAGE_SCALE_FITXY = 0;

    public CustomView(Context context) {
        this(context,null);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomTitleView, defStyleAttr, 0);
        mTitleText = a.getString(a.getIndex(R.styleable.CustomTitleView_titleText));
        mTextColor = a.getColor(a.getIndex(R.styleable.CustomTitleView_titleTextColor), Color.BLACK);
        mTitleTextSize = a.getDimensionPixelSize(a.getIndex(R.styleable.CustomTitleView_titleTextSize),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,16,getResources().getDisplayMetrics()));
        mImage = BitmapFactory.decodeResource(getResources(),a.getResourceId(a.getIndex(R.styleable.CustomTitleView_image),0));
        mImageScale = a.getInt(a.getIndex(R.styleable.CustomTitleView_imageScaleType),0);
        a.recycle();
        rect = new Rect();
        mPaint = new Paint();
        mPaint.setTextSize(mTitleTextSize);
        mTextBound = new Rect();
        mPaint.getTextBounds(mTitleText,0,mTitleText.length(), mTextBound);//获得文字的宽高信息
    }

//    MeasureSpec的specMode,一共三种类型：
//    EXACTLY：一般是设置了明确的值或者是MATCH_PARENT
//    AT_MOST：表示子布局限制在一个最大值内，一般为WARP_CONTENT
//    UNSPECIFIED：表示子布局想要多大就多大，很少使用
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            //由图片决定的宽
            int desireByImg = getPaddingLeft() + getPaddingRight() + mImage.getWidth();
            //由文字决定的宽
            int desireByTitle = getPaddingLeft() + getPaddingRight() + mTextBound.width();
            if(widthMode == MeasureSpec.AT_MOST) {//wrap_content
                int desire = Math.max(desireByImg, desireByTitle);
                mWidth = Math.min(desire, widthSize);
            }
        }

        if(heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
            int desired = getPaddingTop() + getPaddingBottom() + mImage.getHeight() + mTextBound.height();
            if(heightMode == MeasureSpec.AT_MOST) {//wrap_content
                mHeight = Math.min(desired, heightSize);
            }
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);//空心
        mPaint.setColor(Color.CYAN);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);

        rect.left = getPaddingLeft();
        rect.right = mWidth - getPaddingLeft();
        rect.top = getPaddingTop();
        rect.bottom = mHeight - getPaddingBottom();

        mPaint.setColor(mTextColor);
        mPaint.setStyle(Paint.Style.FILL);

        //当前设置的宽度小于字体需要的宽度，将字体改为XXX...
        if(mTextBound.width() > mWidth) {
            TextPaint paint = new TextPaint(mPaint);
            String msg = TextUtils.ellipsize(mTitleText, paint, (float)mWidth - getPaddingLeft() - getPaddingRight(),
                    TextUtils.TruncateAt.END).toString();
            canvas.drawText(msg, getPaddingLeft(), mHeight - getPaddingBottom() - (Math.abs(mTextBound.top) - mTextBound.bottom) / 2, mPaint);
        } else {
            //正常情况，将字体居中
            canvas.drawText(mTitleText, mWidth/2 - mTextBound.width() * 1.0f / 2,
                    mHeight - getPaddingBottom() - (Math.abs(mTextBound.top) - mTextBound.bottom) / 2, mPaint);
        }

        //取消使用掉的块
        rect.bottom -= mTextBound.height();
        if(mImageScale == IMAGE_SCALE_FITXY) {
            canvas.drawBitmap(mImage, null, rect, mPaint);
        } else {
            rect.left = mWidth / 2 - mImage.getWidth() / 2;
            rect.right = mWidth / 2 + mImage.getWidth() / 2;
            rect.top = (mHeight - mTextBound.height()) / 2 - mImage.getHeight()/2;
            rect.bottom = (mHeight - mTextBound.height()) / 2 + mImage.getHeight() / 2;

            canvas.drawBitmap(mImage, null, rect, mPaint);//图片为盒子模型
        }
    }
}
