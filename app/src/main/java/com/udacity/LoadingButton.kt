package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private val arcMargin = 24F

    // Custom Attributes
    private var buttonBackgroundColor = 0
    private var buttonProgressColor = 0
    private var buttonTextColor = 0
    private var progress = 0F

    // The animator can be reused therefore I placed it here
    val valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
        duration = 2000
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { valueAnimator ->
            progress = valueAnimator.animatedFraction as Float
            invalidate()
        }
        addListener(
            onStart = {
                progress = 0F
                buttonState = ButtonState.Loading
            },
            onEnd = {
                buttonState = ButtonState.Completed
            }
        )
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, oldState, newState ->
        when (newState) {
            ButtonState.Completed -> {
                isClickable = true
                isFocusable = true
            }
            ButtonState.Clicked -> {
                // disable button click for a while
                isClickable = false
                isFocusable = false

                // Restart animator
                valueAnimator.cancel()
                valueAnimator.start()
            }
            ButtonState.Loading -> {
                // No other changes except calling invalidate to redraw a new button state
            }
        }
        invalidate()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        isClickable = true
        isFocusable = true
        buttonState = ButtonState.Completed

        // Custom Attributes
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_buttonBackgroundColor, 0)
            buttonProgressColor = getColor(R.styleable.LoadingButton_buttonProgressColor, 0)
            buttonTextColor = getColor(R.styleable.LoadingButton_buttonTextColor, 0)
        }
    }

    /**
     * Override performClick instead of onClickLister, is to leave this for developers to add
     * their own OnClickListener. performClick itself calls the onClickListener
     */
    override fun performClick(): Boolean {
        super.performClick()

        if (ButtonState.Completed == buttonState) {
            buttonState = ButtonState.Clicked
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (buttonState) {
            ButtonState.Completed -> onDrawReadyButton(canvas)
            ButtonState.Loading -> onDrawLoadingButton(canvas)
        }
    }

    private fun onDrawReadyButton(canvas: Canvas) {
        paint.color = buttonBackgroundColor
        canvas.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)

        paint.color = buttonTextColor

        // Horizontal center of the button
        val coordX = (widthSize / 2.0).toFloat()
        // Vertical center of the button - some text height
        val coordY = ((heightSize.toFloat() - paint.descent() - paint.ascent()) / 2.0).toFloat()

        // The button text is not hardcoded - using string resources
        val label = resources.getString(R.string.download)
        canvas.drawText(label, coordX, coordY, paint)
    }

    private fun onDrawLoadingButton(canvas: Canvas) {
        val progressWidth = widthSize * progress

        // Draw the completed portion
        paint.color = buttonProgressColor
        canvas.drawRect(0F, 0F, progressWidth, heightSize.toFloat(), paint)

        // Fill the rest with tbe background color
        paint.color = buttonBackgroundColor
        canvas.drawRect(progressWidth, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)

        paint.color = buttonTextColor

        // Horizontal center of the button
        val coordX = widthSize.toFloat() / 2.0F
        // Vertical center of the button - some text height
        val coordY = (heightSize.toFloat() - paint.descent() - paint.ascent()) / 2F

        // The button text is not hardcoded - using string resources
        val label = resources.getString(R.string.button_loading)
        canvas.drawText(label, coordX, coordY, paint)

        // To allow flexibility for the text length, I put the circle at the right side of the button
        // The height of the arc = height of the button - (margin x2)
        val arcLeft = widthSize.toFloat() - heightSize.toFloat() + arcMargin
        val arcRight = widthSize.toFloat() - arcMargin
        val arcTop = arcMargin
        val arcBottom = heightSize.toFloat() - arcMargin

        paint.color = resources.getColor(R.color.colorAccent, null)
        canvas.drawArc(arcLeft, arcTop, arcRight, arcBottom, 0.0F, progress * 360, true, paint)
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

}