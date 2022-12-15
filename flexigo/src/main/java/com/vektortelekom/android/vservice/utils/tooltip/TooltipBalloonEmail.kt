package com.vektortelekom.android.vservice.utils.tooltip

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.*
import com.vektortelekom.android.vservice.R

class TooltipBalloonEmail : Balloon.Factory() {

    override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {

        return createBalloon(context) {
            setLayout(R.layout.tooltips_item_email)
            setIsVisibleArrow(true)
            setArrowSize(15)
            setArrowElevation(20)
            setWidthRatio(1.0f)
            setFocusable(false)
            setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
            setArrowColorResource(R.color.colorWhite)
            setArrowOrientation(ArrowOrientation.TOP)
            setArrowPosition(0.5f)
            setCornerRadius(8f)
            setElevation(15)
            setFocusable(true)
            setPadding(10)
            setBackgroundColorResource(R.color.colorWhite)
            setBalloonAnimation(BalloonAnimation.FADE)
            setDismissWhenClicked(false)
            setDismissWhenTouchOutside(false)
            setDismissWhenShowAgain(false)
            setLifecycleOwner(lifecycle)
            build()

        }
    }
}