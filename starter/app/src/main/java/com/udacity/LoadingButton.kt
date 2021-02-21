package com.udacity

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

private const val PADDING = 8
private const val CIRCLE_WIDTH = 30

class LoadingButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    //cached attrs
    private var bgColor = 0
    private var textColor = 0
    private var linearProgressBarColor = 0
    private var circleProgressBarColor = 0

    private var currentProgress = 0

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private lateinit var buttonText: String

    private val valueAnimator = ValueAnimator()

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

        buttonState = ButtonState.Completed

        valueAnimator.addUpdateListener {

        }


    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            val xPos = canvas.width / 2
            val yPos = (canvas.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2).toInt()

            canvas.drawColor(bgColor)

            //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
            textPaint.color = Color.WHITE
            canvas.drawText(buttonText, xPos.toFloat(), yPos.toFloat(), textPaint)
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

    fun loadingComplete() {
        buttonState = ButtonState.Completed
        currentProgress = 0
        invalidate()
    }

    fun loading(progress: Int = 0) {
        buttonState = ButtonState.Loading
        currentProgress = progress
        invalidate()
    }

}