package lx.own.view.online;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Looper;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorInt;
import androidx.annotation.MainThread;

import java.util.List;

import lx.own.R;
import lx.own.view.online.entity.LyricRows;
import lx.own.view.online.entity.LyricWord;


/**
 * 歌词控件
 * Created By Lei.X on 2019/05/14
 */
public class LyricView2 extends View {

    public static final int TOP = 1;
    public static final int CENTER = 2;
    public static final int BOTTOM = 3;

    private final int FLAG_OUT_OF_WORK = 1;
    private final int FLAG_IS_SING_FINISHED_STATUS = 1 << 1;//是否处于演唱完成状态
    private final int FLAG_DISPLAY_SING_FINISHED = 1 << 2;//是否要展示演唱完成
    private final int FLAG_SAFE_EDGE = 1 << 3;//安全边界(歌词到顶部或底部的时候不再滚动)
    private final int FLAG_SIGN_USED_LYRIC = 1 << 4;//标记已使用过的歌词
    private final int FLAG_OVER_DRAW = 1 << 5;//超边界绘制（不因为自身高度而限制歌词行数）
    private final int FLAG_CONTENT_FORCE_CENTER = 1 << 6;//内容强行居中

    private int mFlags;

    private final int[] mWH;

    private final TextPaint mTextPaint, //歌词画笔
            mUsedTextPaint; //已唱过的歌词画笔
    @ColorInt
    private int mUnusedTextColor,//小于position的歌词颜色
            mUsedTextColor;//大于position的歌词颜色
    private int mTextSize;//字体大小
    private boolean mUnusedTextBold,
            mUsedTextBold;
    private final Paint.FontMetrics mFontMetrics;
    private float mRowHeight, //行高度
            mTextDrawX,//绘制文本的X值
            mTextDrawY,//绘制文本的Y值
            mTextDrawYScrollOffset,//动画开始前绘制文本的YOffset，动画完成后置为0
            mScrollingOffsetY = 0,//单词动画内已滚动的offsetY
            mTargetScrollOffsetY = 0;//单次动画内要滚动的目标offsetY

    private int mUnderwayRowGravity;//正在进行的行的对齐方式
    private int mLinePadding;
    private int mMaxRowCount,//最大行数
            mDrawRowCount,
            mUnderwayRowIndex,//当前行
            mConfiguredStartRow,//配置过的起始行
            mAbovePreviewCount,//上方的预览行数
            mBelowPreviewCount;//下方的预览行数

    private List<LyricRows> mLrcRows; //歌词
    private String mEmptyText,//无歌词默认文本
            mSingFinishedText;
    private long mProgress,//进度
            mDuration,//总时长
            mConfiguredStartTime;//配置过的起始时间
    private int mSingEndInterval;//判断为演唱完成的时间间隔(距离duration多少开始判断)

    private ValueAnimator mScrollAnimator;
    private long mScrollDuration;
    private long mOffsetTime;

    public LyricView2(Context context) {
        this(context, null);
    }

    public LyricView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mWH = new int[2];
        this.mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        this.mUsedTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        this.mFontMetrics = new Paint.FontMetrics();
        this.init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.mUnusedTextColor = 0xFF000000;
        this.mUsedTextColor = 0xFF444444;
        this.mTextSize = 20;
        this.mLinePadding = 0;
        this.mUnderwayRowGravity = CENTER;
        this.mMaxRowCount = 5;
        this.mUnusedTextBold = false;
        this.mUsedTextBold = false;
        this.mScrollDuration = 500L;

        final TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.LyricView2);
        if (t != null) {
            this.mTextSize = t.getDimensionPixelSize(R.styleable.LyricView2_lyricView2_textSize, mTextSize);
            this.mUnusedTextColor = t.getColor(R.styleable.LyricView2_lyricView2_textColor, mUnusedTextColor);
            this.mUsedTextColor = t.getColor(R.styleable.LyricView2_lyricView2_usedTextColor, mUsedTextColor);
            this.mLinePadding = t.getDimensionPixelSize(R.styleable.LyricView2_lyricView2_linePadding, mLinePadding);
            this.mUnderwayRowGravity = t.getInteger(R.styleable.LyricView2_lyricView2_underwayLineGravity, mUnderwayRowGravity);
            this.mMaxRowCount = t.getInteger(R.styleable.LyricView2_lyricView2_maxLines, mMaxRowCount);
            this.mUnusedTextBold = t.getBoolean(R.styleable.LyricView2_lyricView2_textBold, mUnusedTextBold);
            this.mUsedTextBold = t.getBoolean(R.styleable.LyricView2_lyricView2_usedTextBold, mUsedTextBold);
            this.mScrollDuration = t.getInteger(R.styleable.LyricView2_lyricView2_scrollDuration, 500);

            final boolean signUsedText = t.getBoolean(R.styleable.LyricView2_lyricView2_signUsedText, false);
            if (signUsedText)
                mFlags |= FLAG_SIGN_USED_LYRIC;
            final boolean safeEdge = t.getBoolean(R.styleable.LyricView2_lyricView2_safeEdge, false);
            if (safeEdge)
                mFlags |= FLAG_SAFE_EDGE;
            final boolean showFinished = t.getBoolean(R.styleable.LyricView2_lyricView2_showFinished, false);
            if (showFinished)
                mFlags |= FLAG_DISPLAY_SING_FINISHED;
            final boolean overDraw = t.getBoolean(R.styleable.LyricView2_lyricView2_overDraw, false);
            if (overDraw)
                mFlags |= FLAG_OVER_DRAW;
            final boolean contentForceCenter = t.getBoolean(R.styleable.LyricView2_lyricView2_contentForceCenter, false);
            if (contentForceCenter)
                mFlags |= FLAG_CONTENT_FORCE_CENTER;

            this.mSingFinishedText = t.getString(R.styleable.LyricView2_lyricView2_finishedText);
            this.mEmptyText = t.getString(R.styleable.LyricView2_lyricView2_emptyText);
            t.recycle();
        }
        this.mTextPaint.setColor(this.mUnusedTextColor);
        this.mTextPaint.setTextSize(this.mTextSize);
        this.mTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mTextPaint.setFakeBoldText(mUnusedTextBold);
        this.mUsedTextPaint.setColor(this.mUsedTextColor);
        this.mUsedTextPaint.setTextSize(this.mTextSize);
        this.mUsedTextPaint.setStyle(Paint.Style.FILL);
        this.mUsedTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mUsedTextPaint.setFakeBoldText(mUsedTextBold);
        this.mTextPaint.getFontMetrics(mFontMetrics);
        this.mRowHeight = mFontMetrics.bottom - mFontMetrics.top + mLinePadding;
    }

    /**
     * 配置开始位置
     *
     * @param startTime 起始时间
     * @param startRow  起始行
     */
    public void configureStartSite(long startTime, int startRow) {
        if (mConfiguredStartTime != startTime || mConfiguredStartRow != startRow) {
            this.mConfiguredStartTime = startTime;
            this.mConfiguredStartRow = startRow;
            updateProgress(mProgress, true);
        }
    }

    /**
     * 重置开始位置的配置
     */
    public void resetStartSiteConfiguration() {
        if (mConfiguredStartTime != 0 || mConfiguredStartRow != 0) {
            this.mConfiguredStartTime = 0;
            this.mConfiguredStartRow = 0;
            updateProgress(mProgress, true);
        }
    }

    /**
     * 设置是否展示演唱完成
     *
     * @param show     是否展示
     * @param interval 最后多久开始展示 ，时间单位对应position
     */
    public void setShowSingFinished(boolean show, int interval) {
        final boolean preShow = (mFlags & FLAG_DISPLAY_SING_FINISHED) != 0;
        if (show) {
            if (!preShow || mSingEndInterval != interval) {
                mFlags |= FLAG_DISPLAY_SING_FINISHED;
                mSingEndInterval = interval;
                updateProgress(mProgress, true);
            }
        } else {
            if (preShow) {
                mFlags &= ~FLAG_DISPLAY_SING_FINISHED;
                mSingEndInterval = 0;
                updateProgress(mProgress, true);
            }
        }
    }

    /**
     * 是否已经演唱完成(根据配置的参数返回不同结果)
     *
     * @return true 是 | false 否
     * @see #setShowSingFinished(boolean, int)
     */
    public boolean isSingingEnd() {
        return (mFlags & FLAG_IS_SING_FINISHED_STATUS) != 0;
    }

    /**
     * 是否展示演唱完成
     */
    public boolean isShowSingingEnd() {
        return (mFlags & FLAG_DISPLAY_SING_FINISHED) != 0;
    }

    public void reset(List<LyricRows> lrcRows, long progress, long duration, int offsetTime) {
        if (mScrollAnimator != null) {
            mScrollAnimator.removeAllListeners();
            mScrollAnimator.removeAllUpdateListeners();
            mScrollAnimator.end();
            mScrollAnimator = null;
        }
        mUnderwayRowIndex = 0;
        mTextDrawYScrollOffset = 0;
        mScrollingOffsetY = 0f;
        mTargetScrollOffsetY = 0f;
        mLrcRows = lrcRows;
        if ((mFlags & FLAG_CONTENT_FORCE_CENTER) != 0) {//强行内容居中
            calculateRanges();
        }
        this.mOffsetTime = offsetTime;
        this.mDuration = duration;
        updateProgress(progress, true);
    }

    public void reset() {
        if (mScrollAnimator != null) {
            mScrollAnimator.removeAllListeners();
            mScrollAnimator.removeAllUpdateListeners();
            mScrollAnimator.end();
            mScrollAnimator = null;
        }
        mUnderwayRowIndex = 0;
        mConfiguredStartRow = 0;
        mConfiguredStartTime = 0;
        mTextDrawYScrollOffset = 0;
        mScrollingOffsetY = 0f;
        mTargetScrollOffsetY = 0f;
        mFlags = 0;
        mOffsetTime = 0;
        mProgress = 0;
        mDuration = 0;
        mLrcRows = null;
        postInvalidate();
    }

    /**
     * 设置歌词文件
     * ##WARNING 如果未调用过reset，可能会残留上一首的duration和progress，请注意
     *
     * @param lrcRows 歌词文件
     */
    public void setLrcRows(List<LyricRows> lrcRows) {
        if (mLrcRows != lrcRows) {
            mLrcRows = lrcRows;
            if ((mFlags & FLAG_CONTENT_FORCE_CENTER) != 0) {//强行内容居中
                calculateRanges();
            }
            updateProgress(mProgress, true);
        }
    }

    /**
     * 更新进度
     *
     * @param progress 进度，>=0
     */
    @AnyThread
    public void updateProgress(long progress) {
        updateProgress(progress, false);
    }

    public long getProgress() {
        return mProgress;
    }

    /**
     * 更新总时长
     *
     * @param duration 时长，>=0
     */
    public void updateDuration(long duration) {
        if (mDuration != duration) {
            this.mDuration = duration;
            updateProgress(mProgress, true);
        }
    }

    public long getDuration() {
        return mDuration;
    }

    /**
     * 是否下岗(不绘制任何东西)
     *
     * @param out true 下岗 | false 上岗
     */
    public void outOfWork(boolean out) {
        boolean changed = out != ((mFlags & FLAG_OUT_OF_WORK) == 0);
        if (out) {
            mFlags |= FLAG_OUT_OF_WORK;
        } else {
            mFlags &= ~FLAG_OUT_OF_WORK;
        }
        if (changed)
            postInvalidate();
    }

    public void setOffsetTime(int offsetTime) {
        if (mOffsetTime != offsetTime) {
            mOffsetTime = offsetTime;
            if (mLrcRows != null && mLrcRows.size() > 0)
                updateProgress(mProgress, true);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWH[0] = w;
        mWH[1] = h;
        calculateRanges();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            if ((mFlags & FLAG_OUT_OF_WORK) != 0) {
                //已下岗，不动作
            } else {
                if (this.mLrcRows != null && this.mLrcRows.size() > 0) {
                    if ((mFlags & FLAG_IS_SING_FINISHED_STATUS) != 0) {
                        drawSingleText(canvas, mSingFinishedText);
                    } else {
                        drawCurrentLine(canvas);
                        drawAboveLines(canvas);
                        drawBelowLines(canvas);
                    }
                } else {
                    drawSingleText(canvas, mEmptyText);
                }
            }
        }
    }

    private void drawCurrentLine(Canvas canvas) {
        //取出当前行数据
        try {
            LyricRows currentRow = null;
            if (mLrcRows != null && mUnderwayRowIndex < mLrcRows.size())
                currentRow = mLrcRows.get(mUnderwayRowIndex);
            if (currentRow == null) return;
            final String content = currentRow.getContent();
            if (TextUtils.isEmpty(content)) return;
            final float totalDrawY = mTextDrawY + mTextDrawYScrollOffset + mScrollingOffsetY + getModeSafeOffsetY();
            final long availableProgress = mProgress + mOffsetTime;
            if ((mFlags & FLAG_SIGN_USED_LYRIC) != 0 && availableProgress >= currentRow.getStartTime()) {//需要标记使用过的歌词 && 当前行已经开始唱了
                //当前行高亮
                //当前行的歌词总长度
                final float currentRowContentWidth = mUsedTextPaint.measureText(content);
                //当前行已进行的歌词长度
                float wordWidth = 0.0F;
                List<LyricWord> currRowLrcWords = currentRow.getLrcWords();
                LyricWord word;
                for (int j = 0; j < currRowLrcWords.size(); j++) {
                    word = currRowLrcWords.get(j);
                    if (word == null) continue;
                    final int startTime = word.getStartTime();
                    final int endTime = word.getEndTime();
                    String wordContent = word.getWordContent();
                    float contentWidth = mUsedTextPaint.measureText(wordContent);
                    if (availableProgress >= startTime && availableProgress <= endTime && !TextUtils.isEmpty(wordContent)) {
                        float percent = (float) (availableProgress - startTime) / (float) word.getTotalTime();
                        wordWidth += percent * contentWidth;
                        break;
                    } else {
                        wordWidth += contentWidth;
                    }
                }
                //裁剪画板绘制一次高亮歌词
                final float centerOffset = currentRowContentWidth / 2.0f - wordWidth;
                final float spaceLineX = mTextDrawX - centerOffset;
                canvas.save();
                canvas.clipRect(0.0F, 0.0F, spaceLineX, canvas.getHeight());
                canvas.drawText(content, mTextDrawX, totalDrawY, mUsedTextPaint);
                canvas.restore();
                //再裁剪画板绘制未演唱的歌词(为了处理高亮歌词只是更改了透明度的逻辑，不能叠加绘制)
                canvas.save();
                canvas.clipRect(spaceLineX, 0.0F, canvas.getWidth(), canvas.getHeight());
                canvas.drawText(content, mTextDrawX, totalDrawY, mTextPaint);
                canvas.restore();
            } else {//不需要标记使用过的歌词 || 当前行还没开始唱(高亮内容宽度为0)
                canvas.drawText(content, mTextDrawX, totalDrawY, mTextPaint);
            }
        } catch (Exception ignore) {

        }
    }

    private void drawAboveLines(Canvas canvas) {
        if (mAbovePreviewCount <= 0) return;
        //绘制当前行上面的歌词
        final TextPaint paint = (mFlags & FLAG_SIGN_USED_LYRIC) != 0 ? mUsedTextPaint : mTextPaint;
        final float totalDrawY = mTextDrawY + mTextDrawYScrollOffset + mScrollingOffsetY + getModeSafeOffsetY();
        LyricRows row;
        String rowContent;
        for (int i = mUnderwayRowIndex - 1, count = 1; i >= 0; i--, count++) {
            if (count > mAbovePreviewCount) break;
            row = mLrcRows.get(i);
            rowContent = row.getContent();
            if (!TextUtils.isEmpty(rowContent)) {
                canvas.drawText(rowContent, mTextDrawX, totalDrawY - (mRowHeight * count), paint);
            }
        }
    }

    private void drawBelowLines(Canvas canvas) {
        final int safeBelowCount = mBelowPreviewCount + getModeSafeBelowRowCount();
        if (safeBelowCount <= 0) return;
        //绘制当前行下面的歌词
        final float totalDrawY = mTextDrawY + mTextDrawYScrollOffset + mScrollingOffsetY + getModeSafeOffsetY();
        LyricRows row;
        String rowContent;
        for (int i = mUnderwayRowIndex + 1, count = 1; i < mLrcRows.size(); i++, count++) {
            if (count > safeBelowCount) break;
            row = mLrcRows.get(i);
            rowContent = row.getContent();
            if (!TextUtils.isEmpty(rowContent)) {
                canvas.drawText(rowContent, mTextDrawX, totalDrawY + (mRowHeight * count), mTextPaint);
            }
        }
    }

    /**
     * 绘制一个单独的文本
     *
     * @param canvas  画板
     * @param content 文本
     */
    private void drawSingleText(Canvas canvas, String content) {
        if (content != null)
            canvas.drawText(content, mTextDrawX, mTextDrawY, mTextPaint);
    }

    private void calculateRanges() {
        mTextDrawX = mWH[0] / 2.0f;
        if ((mFlags & FLAG_OVER_DRAW) == 0) {
            final int availableRowCount = (int) ((mWH[1] - getPaddingTop() - getPaddingBottom()) / mRowHeight);
            mDrawRowCount = mMaxRowCount < availableRowCount ? mMaxRowCount : availableRowCount;
        } else {
            mDrawRowCount = mMaxRowCount;
        }
        mTextPaint.getFontMetrics(mFontMetrics);
        //每行文字的绘制偏移(绘制是对齐的baseline，需要一个偏移让其相对于line居中，直接使用line的中心文字会偏上)
        final float lineOffset = ((-mTextPaint.ascent() - mTextPaint.descent()) / 2.0f);
        switch (mUnderwayRowGravity) {
            case TOP:
                //内容是否强行居中
                if (mLrcRows != null && mLrcRows.size() <= mDrawRowCount && (mFlags & FLAG_CONTENT_FORCE_CENTER) != 0) {
                    final int rowCount = mLrcRows.size();
                    //因为内容要强行居中，当前行的绘制y坐标只能根据歌词行数从正中心偏移，这里根据歌词行数计算要偏移多少行(偶数行为 x.5行，奇数行为 x.0行)
                    float rowCountOffset = (rowCount >> 1) - (rowCount % 2 == 0 ? 0.5f : 0f);
                    if (rowCountOffset < 0f)
                        rowCountOffset = 0f;
                    mTextDrawY = mWH[1] / 2.0f - rowCountOffset * mRowHeight + lineOffset;
                } else {
                    //第一行的中心 + 单行文字baseline偏移
                    mTextDrawY = getPaddingTop() + (mRowHeight / 2.0f) + lineOffset;
                }
                break;
            case BOTTOM:
                //内容是否强行居中
                if (mLrcRows != null && mLrcRows.size() <= mDrawRowCount && (mFlags & FLAG_CONTENT_FORCE_CENTER) != 0) {
                    final int rowCount = mLrcRows.size();
                    //因为内容要强行居中，当前行的绘制y坐标只能根据歌词行数从正中心偏移，这里根据歌词行数计算要偏移多少行(偶数行为 x.5行，奇数行为 x.0行)
                    float rowCountOffset = (rowCount >> 1) - (rowCount % 2 == 0 ? 0.5f : 0f);
                    if (rowCountOffset < 0f)
                        rowCountOffset = 0f;
                    mTextDrawY = mWH[1] / 2.0f - rowCountOffset * mRowHeight + lineOffset;
                } else {
                    //最后一行的中心 + 单行文字baseline偏移
                    mTextDrawY = getPaddingBottom() - (mRowHeight / 2.0f) + lineOffset;
                }
                break;
            case CENTER:
                if (mDrawRowCount > 0 && mDrawRowCount % 2 == 0)//center模式maxCount必须为单数
                    mDrawRowCount -= 1;
                //中心 + 单行文字baseline偏移
                mTextDrawY = mWH[1] / 2.0f + lineOffset;
                break;
        }
        calculateScrollLyric(mUnderwayRowIndex);
    }

    /**
     * 计算安全绘制偏移
     * 在安全边界的模式下，如果歌词是底部或者中间对齐，可能这时候还在进行金曲的最前面几句，导致顶部会出现空白
     */
    @AnyThread
    private float getModeSafeOffsetY() {
        if ((mFlags & FLAG_SAFE_EDGE) == 0) return 0f;
        float offset = 0f;
        if (mUnderwayRowGravity == BOTTOM) {
            final int aboveCount = mDrawRowCount - 1;
            if (mUnderwayRowIndex < aboveCount)//当前的行数小于 处于底部时候的above行数，需要强行从往上面绘制一点
                offset = (aboveCount - mUnderwayRowIndex) * -mRowHeight;
        } else if (mUnderwayRowGravity == CENTER) {
            final int aboveCount = mDrawRowCount >> 1;
            if (mUnderwayRowIndex < aboveCount)//当前的行数小于 处于中间时候的above行数，需要强行从往上面绘制一点
                offset = (aboveCount - mUnderwayRowIndex) * -mRowHeight;
        }
        return offset;
    }


    /**
     * 计算安全补齐行数
     * 在安全边界的模式下，如果歌词是底部或者中间对齐，可能这时候还在进行金曲的最前面几句，导致顶部会出现空白
     * 在有了安全绘制偏移的情况下，底部歌词可能无法填满maxRowCount，所以计算一下安全补齐行数
     */
    private int getModeSafeBelowRowCount() {
        if ((mFlags & FLAG_SAFE_EDGE) == 0) return 0;
        int count = 0;
        if (mUnderwayRowGravity == BOTTOM) {
            final int aboveCount = mDrawRowCount - 1;
            if (mUnderwayRowIndex < aboveCount)//当前的行数小于 处于底部时候的above行数，将多余的above行数挪动至底部
                count = aboveCount - mUnderwayRowIndex;
        } else if (mUnderwayRowGravity == CENTER) {
            final int aboveCount = mDrawRowCount >> 1;
            if (mUnderwayRowIndex < aboveCount)//当前的行数小于 处于中间时候的above行数，将多余的above行数挪动至底部
                count = aboveCount - mUnderwayRowIndex;
        }
        return count;
    }

    @AnyThread
    private void updateProgress(long progress, boolean forceUpdate) {
        if (mProgress == progress && !forceUpdate) return;
        mProgress = progress;
        final long availableProgress = progress + mOffsetTime;
        if (mLrcRows != null && mLrcRows.size() > 0) {
            if (availableProgress > 0 && mDuration > 0 && availableProgress >= mDuration - mSingEndInterval && (mFlags & FLAG_DISPLAY_SING_FINISHED) != 0) {
                if (forceUpdate || (mFlags & FLAG_IS_SING_FINISHED_STATUS) == 0) {//没有处于演唱完成状态，设置并重绘
                    mFlags |= FLAG_IS_SING_FINISHED_STATUS;
                    postInvalidate();
                }
            } else {
                boolean needInvalidate = forceUpdate;//是否需要重绘
                if ((mFlags & FLAG_IS_SING_FINISHED_STATUS) != 0) {//处于演唱完成状态，设置并标记需要重绘
                    mFlags &= ~FLAG_IS_SING_FINISHED_STATUS;
                    needInvalidate = true;
                }
                if (availableProgress < mConfiguredStartTime) {//如果进度小于歌词配置的开始时间，直接滚动到配置的startRow
                    if (mUnderwayRowIndex != mConfiguredStartRow) {//当前行不是起始行才需要动作
                        calculateScrollLyric(mConfiguredStartRow);
                        mUnderwayRowIndex = mConfiguredStartRow;
                        needInvalidate = true;
                    }
                } else {
                    //其他情况下无论如何都需要重绘，因为progress改变了
                    needInvalidate = true;
                    LyricRows row;
                    //歌词文件最后一行的索引
                    final int lyricRowLastIndex = mLrcRows.size() - 1;
                    for (int i = lyricRowLastIndex; i >= 0; --i) { //倒序遍历歌词,查找progress对应的歌词行
                        row = mLrcRows.get(i);
                        if (row == null || (availableProgress < row.getStartTime() && i > 0))//行为空，或者行不是progress对应的行但是行不是第一行，进行下一次遍历查找上一行
                            continue;
                        if (mUnderwayRowIndex == i && !forceUpdate) break;//如果当前行是新的progress的行,直接中断
                        calculateScrollLyric(i);
                        //停止遍历
                        break;
                    }
                }
                if (needInvalidate)
                    postInvalidate();
            }
        } else {
            if ((mFlags & FLAG_IS_SING_FINISHED_STATUS) != 0) {//处于演唱完成状态，设置并重绘(没有歌词需要展示空)
                mFlags &= ~FLAG_IS_SING_FINISHED_STATUS;
                postInvalidate();
            }
        }
    }

    @AnyThread
    private void calculateScrollLyric(int targetRowIndex) {
        //是否有安全边界设置
        final int lyricRowLastIndex = mLrcRows == null ? 0 : mLrcRows.size() - 1;
        if (lyricRowLastIndex > 0) {
            final boolean safeEdge = (mFlags & FLAG_SAFE_EDGE) != 0;
            if (safeEdge) {//如果正处于安全模式中，不需要滚动
                final float modeSafeOffsetY = getModeSafeOffsetY();
                if (modeSafeOffsetY != 0) {
                    mUnderwayRowIndex = targetRowIndex;
                    mAbovePreviewCount = targetRowIndex;
                    //Top不会有安全offset
                    mBelowPreviewCount = mUnderwayRowGravity == CENTER ? mDrawRowCount >> 1 : 0;
                    return;
                }
            }
            final int needScrollRowCount = targetRowIndex - mUnderwayRowIndex;
            mTextDrawYScrollOffset += needScrollRowCount * mRowHeight;
            switch (mUnderwayRowGravity) {
                case TOP://歌词的对齐方式是top
                    //i必然是>=0的，所以这里不需要考虑处理顶部边界问题
                    if (!safeEdge || targetRowIndex + (mDrawRowCount - 1) <= lyricRowLastIndex) {//非安全边界 或 想滚动至的行数 + 展示的最大行数 小于等于 最后一行
                        mAbovePreviewCount = 0;
                        mBelowPreviewCount = mDrawRowCount - 1;
                        scrollLyric(needScrollRowCount);
                    } else {
                        mBelowPreviewCount = lyricRowLastIndex - targetRowIndex;
                        mAbovePreviewCount = mDrawRowCount - 1 - mBelowPreviewCount;
                        if (mUnderwayRowIndex + mDrawRowCount - 1 < lyricRowLastIndex)//虽然因为新progress的行数 + 展示的最大行数超过了末尾，不能滚动至那一行，但是判断一下是否需要滚到歌词底部(这里肯定是安全边界设定)
                            scrollLyric((lyricRowLastIndex - mDrawRowCount + 1) - mUnderwayRowIndex);//[（最后一行index - 最大展示行数） = 能滚动至的最后一行 ] - 当前行 = 需要滚动的行数
                    }
                    break;
                case CENTER://歌词的对齐方式是center
                    final int halfMaxRowCount = mDrawRowCount >> 1;
                    if (!safeEdge || ((targetRowIndex + halfMaxRowCount <= lyricRowLastIndex) && (targetRowIndex - halfMaxRowCount >= 0))) {//非安全边界 或 想滚动至的行数 + 展示的最大行数的一半 小于等于 最后一行 且大于等于第0行
                        mAbovePreviewCount = mBelowPreviewCount = mDrawRowCount / 2;
                        scrollLyric(needScrollRowCount);
                    } else if (needScrollRowCount > 0) {//新progress的行数 + 展示的最大行数的一半超过了末尾，不能滚动至那一行
                        mBelowPreviewCount = (lyricRowLastIndex - targetRowIndex);
                        mAbovePreviewCount = mDrawRowCount - 1 - mBelowPreviewCount;
                        if (mUnderwayRowIndex + halfMaxRowCount <= lyricRowLastIndex)//虽然因为新progress的行数 + 展示的最大行数的一半超过了末尾，不能滚动至那一行，但是判断一下是否需要滚到歌词底部(这里肯定是安全边界设定)
                            scrollLyric((lyricRowLastIndex - halfMaxRowCount) - mUnderwayRowIndex);
                    } else if (needScrollRowCount < 0) {
                        mAbovePreviewCount = targetRowIndex;
                        mBelowPreviewCount = mDrawRowCount - 1 - mBelowPreviewCount;
                        if (mUnderwayRowIndex - halfMaxRowCount >= 0)//虽然因为新progress的行数+ 展示的最大行数的一半小于了0，不能滚动至那一行，但是判断一下是否需要滚到歌词顶部(这里肯定是安全边界设定)
                            scrollLyric(halfMaxRowCount - mUnderwayRowIndex);
                    } else {
                        mAbovePreviewCount = mBelowPreviewCount = mDrawRowCount / 2;
                    }
                    break;
                case BOTTOM://歌词的对齐方式是bottom
                    if (!safeEdge || targetRowIndex - (mDrawRowCount - 1) >= 0) {//非安全边界 或 想滚动至的行数 - 展示的最大行数 大于等于 0
                        mAbovePreviewCount = mDrawRowCount - 1;
                        mBelowPreviewCount = 0;
                        scrollLyric(needScrollRowCount);
                    } else {//新progress的行数 + 展示的最大行数超小于了0，不能滚动至那一行
                        mAbovePreviewCount = targetRowIndex;
                        mBelowPreviewCount = mDrawRowCount - 1 - mBelowPreviewCount;
                        if (mUnderwayRowIndex - (mDrawRowCount - 1) > 0)//虽然因为新progress的行数 + 展示的最大行数超小于了0，不能滚动至那一行，但是判断一下是否需要滚到歌词顶部(这里肯定是安全边界设定)
                            scrollLyric((mDrawRowCount - 1) - mUnderwayRowIndex);
                    }
                    break;
            }
            mUnderwayRowIndex = targetRowIndex;
        }
    }

    /**
     * 滚动歌词
     */
    @SuppressLint("WrongThread")
    @AnyThread
    private void scrollLyric(final int rowCount) {
        if (rowCount == 0) return;

        if (Looper.getMainLooper() == Looper.myLooper()) {
            scrollLyricAnimator(rowCount);
        } else {
            this.post(new Runnable() {
                @Override
                public void run() {
                    scrollLyricAnimator(rowCount);
                }
            });
        }
    }

    @MainThread
    private void scrollLyricAnimator(final int rowCount) {
        mTargetScrollOffsetY += -rowCount * mRowHeight;
        if (mScrollAnimator == null) {
            mScrollAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            mScrollAnimator.setDuration(mScrollDuration);
            mScrollAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mTextDrawYScrollOffset += mTargetScrollOffsetY;
                    mScrollingOffsetY = 0f;
                    mTargetScrollOffsetY = 0f;
                    mScrollAnimator = null;
                    postInvalidate();
                }
            });
            mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mScrollingOffsetY = animation.getAnimatedFraction() * mTargetScrollOffsetY;
                    postInvalidate();
                }
            });
            mScrollAnimator.start();
        }
    }
}
