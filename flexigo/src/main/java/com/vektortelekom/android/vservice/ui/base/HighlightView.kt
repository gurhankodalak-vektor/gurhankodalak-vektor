package com.vektortelekom.android.vservice.ui.base

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager

class HighlightView: FrameLayout {

    var mParentView: ViewGroup? = null

    var mCanvas: Canvas? = null

    var mBitmap: Bitmap? = null

    var mEraser: Paint? = null

    var targetView: View? = null

    var textPaint: TextPaint? = null

    var targetX: Float = 0f
    var targetY: Float = 0f
    var targetWidth: Float = 0f
    var targetHeight: Float = 0f

    var isFirstDraw = true

    var gotItClickListener: OnClickListener? = null

    var skipClickListener: OnClickListener? = null

    var highlightText: String? = null

    var gotItButtonText: String = "GOT IT"

    private val buttonHeight: Int = 50

    constructor(context: Context): super(context) {
        //initialize()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        //initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        //initialize()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        //initialize()
    }

    private fun initialize() {

        getDimensionsOfView()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(mBitmap == null) {
            mBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        }

        if(mCanvas == null) {
            mBitmap?.let {
                mCanvas = Canvas(it)
            }
        }

        if(mEraser == null) {
            mEraser = Paint()
            mEraser?.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            mEraser?.flags = Paint.ANTI_ALIAS_FLAG
        }

        mCanvas?.let { mCanvas ->

            mBitmap?.let { bitmap ->

                mEraser?.let { eraser ->

                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                    // draw solid background
                    mCanvas.drawColor(ContextCompat.getColor(context, R.color.blackAlpha80))

                    var radius = 0f

                    if(targetView is MaterialCardView) {

                        val materialView = targetView as MaterialCardView
                        radius = materialView.radius

                    }
                    else if (targetView is MaterialButton) {
                        val materialView = targetView as MaterialButton

                        radius = materialView.cornerRadius.toFloat()
                    }

                    mCanvas.drawRect(targetX + radius-1, targetY, targetX+targetWidth-radius+1, targetY+targetHeight, eraser)

                    mCanvas.drawRect(targetX, targetY+radius-1, targetX+radius, targetY+targetHeight-radius+1, eraser)

                    mCanvas.drawRect(targetX+targetWidth-radius, targetY+radius-1, targetX+targetWidth, targetY+targetHeight-radius+1, eraser)

                    mCanvas.drawArc(targetX, targetY, targetX+radius*2, targetY+radius*2, 180f, 90f, true, eraser)

                    mCanvas.drawArc(targetX+targetWidth-radius*2, targetY, targetX+targetWidth, targetY+radius*2, 270f, 90f, true, eraser)

                    mCanvas.drawArc(targetX, targetY+targetHeight-radius*2, targetX+radius*2, targetY+targetHeight, 90f, 90f, true, eraser)

                    mCanvas.drawArc(targetX+targetWidth-radius*2, targetY+targetHeight-radius*2, targetX+targetWidth, targetY+targetHeight, 0f, 90f, true, eraser)


                    val topSpace = targetY
                    val bottomSpace = measuredHeight - (targetY + targetHeight) - (32+32+32+buttonHeight) * resources.displayMetrics.density

                    if(textPaint == null) {
                        textPaint = TextPaint()
                        textPaint?.isAntiAlias = true
                        textPaint?.textSize = 16 * resources.displayMetrics.density
                        textPaint?.color = ContextCompat.getColor(context, R.color.colorWhite)
                    }

                    textPaint?.let { textPaint ->

                        val text = highlightText?:""

                        val width = (measuredWidth - 64 * resources.displayMetrics.density).toInt()
                        val alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
                        val spacingMultiplier = 1f
                        val spacingAddition = 0f
                        val includePadding = false

                        val myStaticLayout = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
                                    .setAlignment(alignment)
                                    .setLineSpacing(spacingAddition, spacingMultiplier)
                                    .setIncludePad(includePadding)
                                    .build()
                        }
                        else {
                            StaticLayout(text, textPaint, width, alignment, spacingMultiplier, spacingAddition, includePadding)
                        }

                        val textHeight = myStaticLayout.height.toFloat()

                        if(bottomSpace > textHeight) {

                            mCanvas.withTranslation(32 * resources.displayMetrics.density, targetY + targetHeight + 32 * resources.displayMetrics.density) {

                                myStaticLayout.draw(mCanvas)

                                if(isFirstDraw) {

                                    // ADD GOT IT BUTTON
                                    val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, (buttonHeight * resources.displayMetrics.density).toInt())
                                    layoutParams.setMargins((32 * resources.displayMetrics.density).toInt(), (targetY + targetHeight + 64 * resources.displayMetrics.density + textHeight).toInt(), (32 * resources.displayMetrics.density).toInt(), (32 * resources.displayMetrics.density).toInt())


                                    val contextThemeWrapper = ContextThemeWrapper(context, R.style.PrimaryButton)

                                    val button = MaterialButton(contextThemeWrapper)
                                    button.layoutParams = layoutParams
                                    button.text = gotItButtonText
                                    button.setPadding((12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt())
                                    button.setOnClickListener {
                                        mParentView?.removeView(this@HighlightView)
                                        gotItClickListener?.onClick(this@HighlightView)
                                    }
                                    button.setRippleColorResource(R.color.colorWhite)


                                    addView(button)
                                    //END OF ADD GOT IT BUTTON

                                    //ADD SKIP BUTTON

                                    val layoutParams2 = LayoutParams(LayoutParams.WRAP_CONTENT, (buttonHeight * resources.displayMetrics.density).toInt())
                                    layoutParams2.setMargins((150 * resources.displayMetrics.density).toInt(), (targetY + targetHeight + 64 * resources.displayMetrics.density + textHeight).toInt(), (32 * resources.displayMetrics.density).toInt(), (32 * resources.displayMetrics.density).toInt())


                                    val contextThemeWrapper2 = ContextThemeWrapper(context, R.style.Widget_MaterialComponents_Button_TextButton)

                                    val button2 = MaterialButton(contextThemeWrapper2)
                                    button2.layoutParams = layoutParams2
                                    button2.text = context.getString(R.string.skip)
                                    button2.setPadding((12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt())
                                    button2.setOnClickListener {
                                        mParentView?.removeView(this@HighlightView)
                                        skipClickListener?.onClick(this@HighlightView)
                                    }
                                    button2.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                                    button2.setRippleColorResource(R.color.colorWhite)


                                    addView(button2)

                                    //END OF ADD SKIP BUTTON

                                    isFirstDraw = false

                                }

                            }

                        }
                        else if(topSpace > textHeight) {


                            mCanvas.withTranslation(32 * resources.displayMetrics.density, topSpace - textHeight - (32+buttonHeight) * resources.displayMetrics.density) {

                                myStaticLayout.draw(mCanvas)

                                if(isFirstDraw) {

                                    val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, (buttonHeight * resources.displayMetrics.density).toInt())
                                    layoutParams.setMargins((32 * resources.displayMetrics.density).toInt(), (topSpace - (32+buttonHeight) * resources.displayMetrics.density).toInt(), (32 * resources.displayMetrics.density).toInt(), (32 * resources.displayMetrics.density).toInt())


                                    val contextThemeWrapper = ContextThemeWrapper(context, R.style.PrimaryButton)

                                    val button = MaterialButton(contextThemeWrapper)
                                    button.layoutParams = layoutParams
                                    button.text = gotItButtonText
                                    button.setPadding((12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt())
                                    button.setOnClickListener {
                                        mParentView?.removeView(this@HighlightView)
                                        gotItClickListener?.onClick(this@HighlightView)
                                    }
                                    button.setRippleColorResource(R.color.colorWhite)


                                    addView(button)

                                    //ADD SKIP BUTTON

                                    val layoutParams2 = LayoutParams(LayoutParams.WRAP_CONTENT, (buttonHeight * resources.displayMetrics.density).toInt())
                                    layoutParams.setMargins((150 * resources.displayMetrics.density).toInt(), (topSpace - (32+buttonHeight) * resources.displayMetrics.density).toInt(), (32 * resources.displayMetrics.density).toInt(), (32 * resources.displayMetrics.density).toInt())


                                    val contextThemeWrapper2 = ContextThemeWrapper(context, R.style.Widget_MaterialComponents_Button_TextButton)

                                    val button2 = MaterialButton(contextThemeWrapper2)
                                    button2.layoutParams = layoutParams2
                                    button2.text = context.getString(R.string.skip)
                                    button2.setPadding((12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt())
                                    button2.setOnClickListener {
                                        mParentView?.removeView(this@HighlightView)
                                        skipClickListener?.onClick(this@HighlightView)
                                    }
                                    button2.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                                    button2.setRippleColorResource(R.color.colorWhite)

                                    addView(button2)

                                    //END OF ADD SKIP BUTTON
                                    isFirstDraw = false

                                }

                            }

                        }
                        else {
                            // ??? what if required height is insufficient
                        }

                    }

                    // draw mCanvas to main canvas
                    canvas?.drawBitmap(bitmap, 0f, 0f, null)

                }

            }


        }

    }

    private fun getDimensionsOfView() {

        targetView?.let { view ->

            val coordinates = intArrayOf(0,0)
            view.getLocationOnScreen(coordinates)

            if(coordinates[0] == 0 && coordinates[1] == 0) {
                view.post { getDimensionsOfView() }
            }
            else {
                targetX = coordinates[0].toFloat()
                targetY = coordinates[1].toFloat()
                targetWidth = view.width.toFloat()
                targetHeight = view.height.toFloat()
                afterDimensionsGet()
            }

        }

    }

    private fun afterDimensionsGet() {

        setWillNotDraw(false)
        isClickable = true

    }

    class Builder(context: Context, targetView: View, val activity: Activity, val key: String, val sequenceKey: String) {

        var param = HighlighViewParams(context, targetView, activity)

        inline fun addGotItListener(crossinline listener: () -> Unit): Builder {

            param.gotItButtonListener = OnClickListener { listener() }

            return this
        }

        inline fun addSkipButtonListener(crossinline listener: () -> Unit): Builder {

            param.skipButtonListener = OnClickListener { listener() }

            return this
        }

        inline fun addPreActionListener(crossinline listener: () -> Unit, view: View): Builder {

            param.preActionListener = OnClickListener { listener() }

            return this
        }

        fun setHighlightText(highlightText: String): Builder {
            param.text = highlightText

            return this
        }

        fun create() {

            if(AppDataManager.instance.isHighlightSequenceSkipped(sequenceKey)) {
                return
            }
            else if(AppDataManager.instance.isHighlightAlreadyShown(key)) {
                param.gotItButtonListener?.onClick(null)
            }
            else {

                param.preActionListener?.onClick(null)

                if(param.preActionView == null) {
                    continueCreate()
                }
                else {
                    param.preActionView?.post {
                        continueCreate()
                    }
                }


            }
        }

        private fun continueCreate() {
            val highlightView = HighlightView(param.context)

            highlightView.id = View.generateViewId()

            highlightView.targetView = param.targetView

            highlightView.highlightText = param.text

            highlightView.gotItClickListener = OnClickListener {
                param.gotItButtonListener?.onClick(null)
                AppDataManager.instance.setHighlightAlreadyShown(key)
            }

            highlightView.mParentView = param.mParentView

            highlightView.gotItButtonText = param.context.getString(R.string.got_it)

            param.mParentView.addView(highlightView)

            highlightView.skipClickListener = OnClickListener {
                AppDataManager.instance.setHighlightSequenceSkipped(sequenceKey)
                param.skipButtonListener?.onClick(null)
            }

            highlightView.initialize()

        }

    }

    class HighlighViewParams(var context: Context, var targetView: View, activity: Activity) {

        var mParentView: ViewGroup = activity.window?.decorView as ViewGroup

        var gotItButtonListener: OnClickListener? = null

        var skipButtonListener: OnClickListener? = null

        var preActionListener: OnClickListener? = null

        var preActionView: View? = null

        var text: String? = null

        /*fun apply(highlightView: HighlightView) {

            highlightView.highlightText = text
            highlightView.gotItClickListener = gotItButtonListener

        }*/

    }

}