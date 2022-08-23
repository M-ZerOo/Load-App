package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    // Setting custom attributes and values for the button
    private val attributes = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)

    private val backgroundColorOnStart =
        attributes.getColor(
            R.styleable.LoadingButton_backgroundColor,
            context.getColor(R.color.colorPrimary)
        )
    private val backgroundColorOnLoading = attributes.getColor(
        R.styleable.LoadingButton_backgroundColor,
        context.getColor(R.color.colorPrimaryDark)
    )

    private val circleColor = attributes.getColor(
        R.styleable.LoadingButton_circleColor,
        context.getColor(R.color.colorAccent)
    )

    private val textColor =
        attributes.getColor(R.styleable.LoadingButton_textColor, context.getColor(R.color.white))

    // Setting up text
    private var text = context.getString(R.string.button_name)

    // Storing progress of animation
    private var completed = 0.0f
    private var sweepAngle = 0.0f

    private var circleRect = RectF()
    private var progressRect = RectF()


    private val valueAnimator = ValueAnimator.ofFloat(0f, 100f).apply {
        duration = 1500
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateInterpolator()
        addUpdateListener {
            completed = animatedFraction
            sweepAngle = 360f * completed
            progressRect.right = widthSize * completed
            if (buttonState == ButtonState.Completed) {
                cancel()
            }
            super.invalidate()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        color = backgroundColorOnStart
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = circleColor
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        color = textColor
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                text = context.getString(R.string.button_name)
            }

            ButtonState.Loading -> {
                text = context.getString(R.string.button_loading)
                val textRect = Rect()
                textPaint.getTextBounds(text, 0, text.length, textRect)
                circleRect.set(
                    widthSize / 2f + textRect.width() / 2f + textRect.height() / 2f,
                    heightSize / 2f - textRect.height() / 2f,
                    widthSize / 2f + textRect.width() / 2f + textRect.height() * 1.5f,
                    heightSize / 2f + textRect.height() / 2f
                )
                valueAnimator.start()
            }

            ButtonState.Completed -> {
                text = context.getString(R.string.button_name)
                valueAnimator.end()
                valueAnimator.setCurrentFraction(0f)
            }
        }
    }


    init {
        isClickable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthSize = w
        heightSize = h

        progressRect.bottom = heightSize.toFloat()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColorOnStart)

        canvas.drawText(
            context.getString(R.string.button_name),
            (widthSize / 2).toFloat(),
            (heightSize / 2).toFloat(),
            textPaint
        )

        if (buttonState == ButtonState.Loading) {
            canvas.drawColor(backgroundColorOnLoading)
            canvas.drawRect(progressRect, paint)
            canvas.drawArc(circleRect, 270f, sweepAngle, true, circlePaint)
            canvas.drawText(
                context.getString(R.string.button_loading),
                (widthSize / 2).toFloat(), (heightSize / 2).toFloat(), textPaint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            View.MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return buttonState != ButtonState.Loading
    }

    // To change the state of the button from the MainActivity
    fun changeState(state: ButtonState) {
        buttonState = state
    }
}