package ru.netology.statsview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes
) {

    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5)
    private var colors = emptyList<Int>()
    private var colorBackgroundCircle = 0

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()

            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor()),
            )
            colorBackgroundCircle = getColor(R.styleable.StatsView_colorBackgroundCircle, 0xFFFFFF)
        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            invalidate()
        }
    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()
    private var circle = RectF()

    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG,
    ).apply {
        strokeWidth = lineWidth.toFloat()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG,
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }
    private val circlePaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        strokeWidth = lineWidth.toFloat()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = colorBackgroundCircle
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
        circle = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        var startAngle = -90F
        val sum = data.sum()
        var percent = 0F
        data.forEach { if(it != 0F) percent += (100F / (data.lastIndex + 1).toFloat()) }
        canvas.drawCircle(center.x, center.y, radius, circlePaint)
        data.forEachIndexed { index, datum ->
            val angle = (360F * (percent/100F)) * (datum / sum)
            paint.color = colors.getOrElse(index) { generateRandomColor() }
            canvas.drawArc(oval, startAngle, angle, false, paint)
            startAngle += angle
        }
        if (percent == 100F) {
            data.first().let { datum ->
                val angle = (360F * (percent / 100F)) * (datum / sum) / 360F
                paint.color = colors.firstOrNull() ?: generateRandomColor()
                canvas.drawArc(oval, startAngle, angle, false, paint)
            }
        }

        canvas.drawText(
            "%.2f%%".format(percent),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}

