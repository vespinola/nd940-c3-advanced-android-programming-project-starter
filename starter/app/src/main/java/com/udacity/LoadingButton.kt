package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

private const val TEXT_PADDING = 250
private const val OVAL_WIDTH = 30

class LoadingButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    private val ovalSpace = RectF()

    //cached attrs
    private var bgColor = 0
    private var textColor = 0
    private var linearProgressBarColor = 0
    private var circleProgressBarColor = 0

    @Volatile
    private var currentProgress: Double = 0.0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private lateinit var buttonText: String
    private lateinit var valueAnimator: ValueAnimator

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { property, old, new ->
        when(new) {
            ButtonState.Clicked -> {
                buttonText = context.getString(R.string.button_name)
                isClickable = false
            }
            ButtonState.Loading -> {
                buttonText = context.getString(R.string.button_loading)
                isClickable = false
            }
            else -> {
                buttonText = context.getString(R.string.button_name)
                isClickable = true
            }
        }
    }


    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            bgColor = getColor(R.styleable.LoadingButton_bgColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            linearProgressBarColor = getColor(R.styleable.LoadingButton_linearProgressBarColor, 0)
            circleProgressBarColor = getColor(R.styleable.LoadingButton_circleProgressBarColor, 0)
        }

        configureAnimator()

        buttonState = ButtonState.Completed
    }

    private fun setSpace() {
        val horizontalCenter = (width.div(2)).toFloat()
        val verticalCenter = (height.div(2)).toFloat()
        val ovalSize = OVAL_WIDTH
        ovalSpace.set(
                horizontalCenter + TEXT_PADDING.toFloat() - ovalSize,
                verticalCenter - ovalSize,
                horizontalCenter + TEXT_PADDING.toFloat() + ovalSize,
                verticalCenter + ovalSize
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        setSpace()

        canvas?.let {
            val xPos = width / 2
            val yPos = (height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()

            paint.color = bgColor
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            paint.color = linearProgressBarColor
            canvas.drawRect(0f, 0f, (width * currentProgress / 100).toFloat(), height.toFloat(), paint)

            paint.color = Color.WHITE
            canvas.drawText(buttonText, xPos.toFloat(), yPos.toFloat(), paint)

            paint.color = circleProgressBarColor
            canvas.drawArc(
                    ovalSpace,
                    0f,
                    (360 * (currentProgress / 100)).toFloat(),
                    true,
                    paint
            )
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
                MeasureSpec.getSize(w),
                heightMeasureSpec,
                0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun configureAnimator() {
        valueAnimator = ValueAnimator.ofFloat(0f, 100f)

        valueAnimator.apply {
            duration = 2500
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                currentProgress = (it.animatedValue as Float).toDouble()
                invalidate()
            }
        }
    }

    fun loadingComplete() {
        currentProgress = 0.0

        valueAnimator.cancel()
        buttonState = ButtonState.Completed
        invalidate()
        requestLayout()
    }

    fun loading(progress: Int = 0) {
        currentProgress = progress.toDouble()
    }

    override fun performClick(): Boolean {
        super.performClick()

        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Loading
        }

        valueAnimator.start()

        return true
    }

}