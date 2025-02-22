package unics.okdroid.widget.text

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.activity.ComponentActivity
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import unics.okdroid.tools.graphics.fontHeight
import unics.okdroid.tools.graphics.textCenterVerticalBaseY
import unics.okdroid.R
import java.util.concurrent.locks.LockSupport
import kotlin.math.abs

/**
 * 基于[SurfaceView]实现的双缓冲跑马灯
 * 注意：如果该控件的Context是[androidx.activity.ComponentActivity]的子类，则默认会自动管理跑马灯的暂停和销毁，也可以用户自己管理生命周期，两者不冲突。
 *
 * @see setText 设置跑马灯内容 ；对应属性[R.styleable.MarqueeSurfaceView_android_text]
 * @see setTexts 设置多条文本（目前采用拼接成一个字符串绘制的形式：未来考虑采取一条一条绘制的模式）
 * @see setTextColor 设置文字颜色；对应属性[R.styleable.MarqueeSurfaceView_android_textColor]
 * @see setTextSize 设置文字大小：对应属性[R.styleable.MarqueeSurfaceView_android_textSize]
 * @see setShadowLayer 设置文字阴影  对应属性：
 * [R.styleable.MarqueeSurfaceView_android_shadowRadius]
 * [R.styleable.MarqueeSurfaceView_android_shadowDx]
 * [R.styleable.MarqueeSurfaceView_android_shadowDy]
 * [R.styleable.MarqueeSurfaceView_android_shadowColor]
 * @see setFPS 设置帧率 越大绘制越快（默认30帧，不建议过大）;对应属性[R.styleable.MarqueeSurfaceView_fps]
 * @see setScrollStep 设置滚动步长（单次滚动的长度，默认4px），值越大，单次滚动距离越长 ;对应属性[R.styleable.MarqueeSurfaceView_scrollStep]
 * @see startScroll 开始滚动（默认自动开始滚动） ;对应属性[R.styleable.MarqueeSurfaceView_autoStart]
 * @see stopScroll 停止滚动
 * @see onDestroy 销毁，释放资源
 * @see R.styleable.MarqueeSurfaceView_alignFrom 文本从左侧还是右侧开始滚动；对于左侧显示的文本，会在初次滚动的时候先等待3s再滚动
 */
class MarqueeSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs) {

    //surface 是否可用
    private var mSurfaceValid = false

    //是否调用开始滚动
    private var mStarted = true

    //当前设置的文本
    private var mText: String = ""

    @ColorInt
    private var mTextColor: Int = Color.BLACK
    private var mTextSize: Int

    private var mThread: Thread? = null
    private val mDrawRunnable: DrawRunnable = DrawRunnable()

    //字体垂直方向的基础线：提供该变量的目的是为了避免draw的过程中反复测量浪费性能和内存，该变量只会在字体大小发生变化时才会改变
    private var mFontBase: Float = 0f
    private val mTextPaint: Paint

    private var mFPS: Int = 30
        set(value) {
            field = value
            mPerFrameTimeMills = (1000 / value).toLong()
        }

    /**
     * 一帧时间
     */
    private var mPerFrameTimeMills: Long = (1000 / mFPS).toLong()

    /**
     * 滚动偏移步长
     */
    private var mScrollStep: Int = 4

    /**
     * 对齐位置
     */
    private var mAlignFrom: Int = ALIGN_FROM_START

    /**
     * 是否是内部暂停
     */
    private var mInternalPause: Boolean = false

    private val callback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            mSurfaceValid = true
            checkWorkThread()
//            log("SurfaceHolder:surfaceCreated")
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//            log("SurfaceHolder:surfaceChanged ${format} ${width} ${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            mSurfaceValid = false
//            log("SurfaceHolder:surfaceDestroyed")
        }
    }

    init {
        setZOrderOnTop(true)
        val surfaceHolder = holder
        surfaceHolder.addCallback(callback)
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.MarqueeSurfaceView)
        mTextColor = ta.getColor(R.styleable.MarqueeSurfaceView_android_textColor, mTextColor)
        mTextSize = ta.getDimensionPixelSize(
            R.styleable.MarqueeSurfaceView_android_textSize,
            spToPixel(context, 16f).toInt()
        )
        if (ta.hasValue(R.styleable.MarqueeSurfaceView_android_text)) {
            mText = ta.getString(R.styleable.MarqueeSurfaceView_android_text) ?: ""
        }
        mStarted = ta.getBoolean(R.styleable.MarqueeSurfaceView_autoStart, mStarted)

        mFPS = ta.getInt(R.styleable.MarqueeSurfaceView_fps, mFPS)
        mScrollStep = ta.getInt(R.styleable.MarqueeSurfaceView_scrollStep, mScrollStep)
        mAlignFrom = ta.getInt(R.styleable.MarqueeSurfaceView_alignFrom, mAlignFrom)

        val mShadowColor = ta.getColor(R.styleable.MarqueeSurfaceView_android_shadowColor, 0)
        val mShadowDx = ta.getFloat(R.styleable.MarqueeSurfaceView_android_shadowDx, 0f)
        val mShadowDy = ta.getFloat(R.styleable.MarqueeSurfaceView_android_shadowDy, 0f)
        val mShadowRadius = ta.getFloat(R.styleable.MarqueeSurfaceView_android_shadowRadius, 0f)
        ta.recycle()
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = mTextColor
            it.textSize = mTextSize.toFloat()
            it.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor)
        } //创建画笔
        updateFontBase()

        //自动管理生命周期，避免使用者漏掉stop方法的调用
        (scanForActivity(context) as? ComponentActivity)?.let { activity ->
//            log("Context是LifecycleOwner子类，绑定生命周期监听，自动销毁")
            val lifecycleObserver = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
//                    log("Context:onResume")
                    log("[bug] onResume1")
                    if (visibility == View.VISIBLE && mInternalPause) {
//                        log("Context:内部恢复")
                        log("[bug] onResume2")
                        mDrawRunnable.resume()
                        mInternalPause = false
                    }
                }

                override fun onPause(owner: LifecycleOwner) {
                    super.onPause(owner)
//                    log("Context:onPause")
                    log("[bug] onPause1")
                    if (mStarted) {//不可见的时候，如果使用者没有调用stop方法，则内部调用stop
//                        log("Context:内部暂停")
                        log("[bug] onPause2")
                        mDrawRunnable.pause()
                        mInternalPause = true
                    }
                }

                override fun onDestroy(owner: LifecycleOwner) {
//                    log("Context:onDestroy 执行线程释放")
                    log("[bug] onDestroy1")
                    activity.lifecycle.removeObserver(this)
                    super.onDestroy(owner)
                    onDestroy()
                }
            }
            activity.lifecycle.addObserver(lifecycleObserver)
        }
    }

    /**
     * 设置文本内容
     */
    fun setText(str: String?) {
        if (str == mText)
            return
        mText = str ?: ""
        mDrawRunnable.onTextChanged(mText)
    }

    /**
     * 设置多条文本（采用拼接的形式：本身可以采取一条一条绘制的模式）
     */
    fun setTexts(contents: List<String>?, separator: String = "                            ") {
        setText(contents?.filter {
            it.isNotEmpty()
        }?.joinToString(separator).orEmpty())
    }

    fun setTextColor(@ColorInt color: Int) {
        mTextColor = color
        mTextPaint.color = color
    }

    fun setTextSize(@Px size: Int) {
        mTextSize = size
        mTextPaint.textSize = size.toFloat()
        updateFontBase()
        requestLayout()
    }

    /**
     * 设置文字阴影
     */
    fun setShadowLayer(radius: Float, dx: Float, dy: Float, @ColorInt shadowColor: Int) {
        mTextPaint.setShadowLayer(radius, dx, dy, shadowColor)
    }

    /**
     * 设置帧率,值越大，绘制越快，，默认值24
     * 通常Android每秒满帧为60帧（现在也有120帧的设备了），但是这种需求一般有20帧的fps就足够了，默认24fps
     */
    fun setFPS(@IntRange(from = 1) fps: Int) {
        require(fps > 1) {
            "fps must be greater than 0."
        }
        mFPS = fps
    }

    /**
     * 设置滚动步长；值越大，单次滚动距离有越长
     */
    fun setScrollStep(@Px @IntRange(from = 1) value: Int) {
        require(value > 1) {
            "scroll step must be greater than 0."
        }
        mScrollStep = value
    }

    /**
     * 开始滚动
     */
    fun startScroll() {
        log("[bug] startScroll")
        mStarted = true
        mInternalPause = false
        mDrawRunnable.resume()
        checkWorkThread()
    }

    /**
     * 停止滚动
     */
    fun stopScroll() {
        log("[bug] stopScroll")
        mStarted = false
        mInternalPause = false
        mDrawRunnable.pause()
    }

    /**
     * 释放资源，并且控件无法再次使用
     */
    fun onDestroy() {
        log("[bug] onDestroy")
        mStarted = false
        mInternalPause = false
        mDrawRunnable.stop()
        mThread?.let {
            try {
                if (it.isAlive && !it.isInterrupted) {
                    it.interrupt()
                }
            } catch (e: Throwable) {
                mThread = null
            }
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        log("[bug] [onWindowVisibilityChanged(visibility:$visibility)]")
        handleVisibilityChanged(visibility == View.VISIBLE)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        log("[bug] [onVisibilityChanged(changedView:$changedView,visibility:$visibility)]")
        handleVisibilityChanged(visibility == View.VISIBLE)
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        log("[bug] [onVisibilityAggregated(isVisible:$isVisible)]")
        handleVisibilityChanged(isVisible)
    }

    private fun handleVisibilityChanged(isVisible: Boolean) {
        if (isVisible && mInternalPause) {
            mDrawRunnable.resume()
            mInternalPause = false
            log("[bug] [handleVisibilityChanged(resume)")
        } else {
            if (!isVisible && mStarted) {//不可见的时候，如果使用者没有调用stop方法，则内部调用stop
                mDrawRunnable.pause()
                mInternalPause = true
                log("[bug] [handleVisibilityChanged(pause)")
            }
        }
    }

    //检查工作线程
    private fun checkWorkThread() {
        //未调用开始或者surface当前不可用，则不处理
        if (!mStarted || !mSurfaceValid)
            return
        val thread = mThread
        //当前线程已运行
        if (thread != null && thread.isAlive && !thread.isInterrupted && mDrawRunnable.sEnable)
            return
        mThread = Thread(mDrawRunnable)
        mThread!!.start()
        log("[bug] [checkWorkThread(start)")
    }

    private fun updateFontBase() {
        mFontBase = mTextPaint.textCenterVerticalBaseY
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val preferredHeight =
            mTextPaint.fontHeight.toInt()// (mTextPaint.fontMetrics.bottom - mTextPaint.fontMetrics.top).toInt()// (mTextPaint.textSize * 1.5 + paddingTop + paddingBottom).toInt()
        val h = when (heightSpecMode) {
            MeasureSpec.EXACTLY -> {//match_parent or 具体数字
                MeasureSpec.getSize(heightMeasureSpec)
            }

            else -> {
                preferredHeight
            }
        }
        setMeasuredDimension(w, h)
//        //修正surfaceholder的大小：避免动态修改大小之后
        holder.setFixedSize(w, h)
    }

    internal inner class DrawRunnable : Runnable {
        private var sPause: Boolean = false
        var sEnable: Boolean = true

        /**
         * 文本宽度
         */
        private var sTextLength = 0f

        /**
         * X方向滚动之后的位置
         */
        private var sScrolledX = 0f

        /**
         * 已经绘制过的内容
         */
        private var sHasDrawnText: String? = null

        fun onTextChanged(newText: String) {
            if (sPause && newText != sHasDrawnText) {//已经处于暂停中，则绘制一次,否则不用额外处理，循环工作中会重新处理文本内容变化
                log("文本发生变化，并处于暂停状态，执行一次绘制")
                resetXScrollDistance()
                sTextLength = mTextPaint.measureText(newText)
                draw(newText)
                sHasDrawnText = newText
            }
        }

        private fun resetXScrollDistance() {
            sScrolledX = 0f

//            sScrolledX = if (mBeginPosition == POSITION_START) {
//                0f
//            } else {
//                try {
//                    val canvasWidth = holder.surfaceFrame.width().toFloat()
//                    if (canvasWidth > 0)
//                        canvasWidth
//                    else width.toFloat()
//                } catch (e: Throwable) {
//                    width.toFloat()
//                }
//            }
        }

        fun pause() {
            sPause = true
        }

        fun resume() {
            sPause = false
            sEnable = true
            mThread?.let {
                //避免因为内部等待，导致无法结束逻辑
                LockSupport.unpark(it)
            }
        }

        fun stop() {
            sEnable = false
            mThread?.let {
                //避免因为内部等待，导致无法结束逻辑
                LockSupport.unpark(it)
            }
        }

        override fun run() {
            try {
                while (sEnable) {//循环绘制
                    if (sPause) {
                        LockSupport.park()
                    }
                    if (!mSurfaceValid) {
                        log("[bug] surface未创建，不做任何处理")
                        safeSleep(200)
                        continue
                    }

                    val content = mText
                    if (content != sHasDrawnText) {
                        log("[bug] 当前内容与之前内容不一致，说明内容发生了变化，重置内容")
                        resetXScrollDistance()
                        sTextLength = mTextPaint.measureText(content)
                        draw(content)
                        sHasDrawnText = content
                        if (mAlignFrom == ALIGN_FROM_START) {
                            safeSleep(3000)
                            log("[bug] safeSleep(3000)")
                        } else {
                            sleepOneFrameTime()
                            log("[bug] sleepOneFrameTime")
                        }
                        continue
                    }

                    //用户没有调用滚动方法，此时进行等待
                    if (!mStarted) {
                        log("[bug] s用户未调用开始滚动的情况下，不做任何处理")
                        sleepWaitValidate()
                        continue
                    }

                    if (content.isEmpty()) {
                        log("[bug] 用户内容设置为空，不做任何处理")
                        sleepWaitValidate()
                        continue
                    }
                    log("[bug] 开始绘制")

                    val totalScrollDistance = sTextLength
                    val viewWidth = canvasWidth
                    if (mAlignFrom == ALIGN_FROM_END) {
                        totalScrollDistance + viewWidth
                    }

                    //偏移的距离大于目标距离时，说明当次已经滑动完成，重新开始
                    if (abs(sScrolledX) >= totalScrollDistance) {
                        //说明已经完整滚动过一次，此时让文本从末尾开始重新绘制
                        sScrolledX = if (mAlignFrom == ALIGN_FROM_START) {
                            viewWidth.toFloat()
                        } else {
                            0f
                        }
                    }
                    draw(content)
                    sleepOneFrameTime()
                }
                // reset
                log("[bug] 结束，重置")
                sScrolledX = 0f
                val content = mText
                draw(content)
                sHasDrawnText = content
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        private val canvasWidth: Int
            get() {
                val width = this@MarqueeSurfaceView.width
                return if (width > 0)
                    width
                else holder?.surfaceFrame?.width() ?: 0
            }

        private fun safeSleep(time: Long) {
            try {
                Thread.sleep(time)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        private fun sleepWaitValidate() {
            safeSleep(500)
        }

        private fun sleepOneFrameTime() {
            safeSleep(mPerFrameTimeMills)
        }

        private fun draw(text: String) {
            if (!mSurfaceValid) {
                return
            }

            holder?.let { holder ->
                var c: Canvas? = null
                try {
                    //锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
                    c = holder.lockCanvas() ?: return
                    drawImpl(c, holder, text)
                    //绘制完成之后继续偏移 -是往左偏移
                    sScrolledX -= mScrollStep
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c) //结束锁定画图，并提交改变。
                    }
                }
            }
        }

        private fun drawImpl(canvas: Canvas, surfaceHolder: SurfaceHolder, text: String) {
            //先清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            //使用surfaceFrame的height()来绘制，发现绘制位置不是很准确（不同的textsize才有可能）
            val preferredHeight = surfaceHolder.surfaceFrame.height()
//            if (preferredHeight == 0)
//                preferredHeight = surfaceHolder.surfaceFrame.height()
            val baselineY = preferredHeight / 2f + mFontBase
            val xOffset: Float = if (mAlignFrom == ALIGN_FROM_START) {
                0f
            } else {
                canvasWidth.toFloat()
            }
//            val baseLineY = height / 2 + mFontHeight / 2
            canvas.drawText(text, xOffset + sScrolledX, baselineY, mTextPaint)
        }
    }

    companion object {

        private const val ALIGN_FROM_START = 0
        private const val ALIGN_FROM_END = 1

        private inline fun log(msg: String) {
//            Log.d("双缓冲跑马灯", msg)
        }

//        @JvmStatic
//        private fun getFontHeight(paint: Paint): Float {
//            val bounds = Rect()
//            paint.getTextBounds("这", 0, 1, bounds)
//            return bounds.height().toFloat()
//        }
//
//        @JvmStatic
//        private fun pixelsToSp(context: Context, px: Float): Float {
//            val scaledDensity = context.resources.displayMetrics.scaledDensity
//            return px / scaledDensity
//        }

        @JvmStatic
        private fun spToPixel(context: Context, sp: Float): Float {
            val scaledDensity = context.resources.displayMetrics.scaledDensity
            return sp * scaledDensity
        }

        /**
         * 获取Activity
         */
        @JvmStatic
        private fun scanForActivity(context: Context?): Activity? {
            if (context == null) return null
            if (context is Activity) {
                return context
            } else if (context is ContextWrapper) {
                return scanForActivity(context.baseContext)
            }
            return null
        }
    }
}