package ru.netology.statsview.ui

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
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
    private var dataRealisation = 0

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()
            dataRealisation = getInteger(R.styleable.StatsView_data_realization, 0)

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
            update()
        }
    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()
    private var circle = RectF()
    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

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

    //    override fun onDraw(canvas: Canvas) {
//        if (data.isEmpty()) {
//            return
//        }
//
//        val sum = data.sum()
//        var percent = 0F
//        data.forEach { if (it != 0F) percent += (100F / (data.lastIndex + 1).toFloat()) } // Соотношение данных в процентах без учета нулевых значений
//        canvas.drawCircle(center.x, center.y, radius, circlePaint)
//        data.forEachIndexed { index, datum ->
//            val angle = (360F * (percent / 100F)) * (datum / sum) // Получаем угол дуги от той части окружности, которая должна быть заполнена
//            paint.color = colors.getOrElse(index) { generateRandomColor() }
//            canvas.drawArc(oval, startAngle, angle * progress, false, paint)
//            startAngle += angle
//        }
//        if (percent == 100F) {
//            data.first().let { datum ->
//                val angle = (360F * (percent / 100F)) * (datum / sum) / 360F
//                paint.color = colors.firstOrNull() ?: generateRandomColor()
//                canvas.drawArc(oval, startAngle, angle, false, paint)
//            }
//        }
//
//        canvas.drawText(
//            "%.2f%%".format(percent),
//            center.x,
//            center.y + textPaint.textSize / 4,
//            textPaint
//        )
//    }
    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        val sum = data.sum()
        var startAngle = -90F
        var percent = 0F
        data.forEach { if (it != 0F) percent += (100F / (data.lastIndex + 1).toFloat()) } // Соотношение данных в процентах без учета нулевых значений
        var filled = 0F
        val max = 360F * (percent / 100F)
        val progressAngle = min(360F * progress, max)

        canvas.drawText(
            "%.2f%%".format(percent),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )

        canvas.drawCircle(center.x, center.y, radius, circlePaint)

        when(dataRealisation) {
            0 -> {
                data.forEachIndexed { index, datum ->
                    val angle =
                        (360F * (percent / 100F)) * (datum / sum) // Получаем угол дуги от той части окружности, которая должна быть заполнена
                    paint.color = colors.getOrElse(index) { generateRandomColor() }
                    canvas.drawArc(oval, startAngle, angle * progress, false, paint)
                    startAngle += angle
                }
                if (percent == 100F) {
                    data.first().let { datum ->
                        val angle = (360F * (percent / 100F)) * (datum / sum) / 360F
                        paint.color = colors.firstOrNull() ?: generateRandomColor()
                        canvas.drawArc(oval, startAngle, angle, false, paint)
                    }
                }
            }

            1 -> {
                data.forEachIndexed { index, datum ->
                    val angle =
                        max * (datum / sum) // Получаем угол дуги от той части окружности, которая должна быть заполнена
                    val sweepAngle = progressAngle - filled
                    paint.color = colors.getOrElse(index) { generateRandomColor() }
                    canvas.drawArc(
                        oval,
                        startAngle,
                        if (sweepAngle > angle) angle else sweepAngle,
                        false,
                        paint
                    )
                    startAngle += angle
                    filled += angle
                    if (filled > progressAngle) return
                }
                if (percent == 100F) {
                    data.first().let { datum ->
                        val angle = (360F * (percent / 100F)) * (datum / sum) / 360F
                        paint.color = colors.firstOrNull() ?: generateRandomColor()
                        canvas.drawArc(oval, startAngle, angle, false, paint)
                    }
                }
            }
            2 -> {
                data.forEachIndexed { index, datum ->
                    val angle =
                        (360F * (percent / 100F)) * (datum / sum) // Получаем угол дуги от той части окружности, которая должна быть заполнена
                    paint.color = colors.getOrElse(index) { generateRandomColor() }
                    canvas.drawArc(oval, startAngle + angle/2, (angle/2F) * (progress) , false, paint)
                    canvas.drawArc(oval, startAngle + angle/2, (angle/2F) * (progress * -1F), false, paint)
                    startAngle += angle
                }
            }
        }
    }


    private fun update() {

        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        //val angelHolder = PropertyValuesHolder.ofFloat("rotation", -90F, 270F)
        val startAngelHolder = PropertyValuesHolder.ofFloat("angle", 0F, 1F)
        valueAnimator = ValueAnimator.ofPropertyValuesHolder(startAngelHolder).apply {
            addUpdateListener { anim ->
                // startAngle = anim.getAnimatedValue("rotation") as Float
                progress = anim.getAnimatedValue("angle") as Float
                invalidate()
            }
            duration = 2500
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }


    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}


