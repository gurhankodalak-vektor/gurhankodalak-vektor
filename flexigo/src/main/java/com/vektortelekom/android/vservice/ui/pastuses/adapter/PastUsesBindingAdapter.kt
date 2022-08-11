package com.vektortelekom.android.vservice.ui.pastuses.adapter

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PastUseType

class PastUsesBindingAdapter {

    companion object {
        @JvmStatic
        @BindingAdapter("bind:pastUseTypeBackground")
        fun pastUseTypeBackground(cardView: MaterialCardView, type: PastUseType) {
            when(type) {
                PastUseType.POOLCAR -> {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.poolcar_tint_color))
                }
                PastUseType.FLEXIRIDE -> {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.flexiride_tint_color))
                }
                PastUseType.TAXI -> {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.taxi_tint_color))
                }
            }
        }

        @JvmStatic
        @BindingAdapter("bind:pastUseTypeText")
        fun pastUseTypeText(textView: TextView, type: PastUseType) {
            when(type) {
                PastUseType.POOLCAR -> {
                    textView.text = textView.context.getText(R.string.pool_car)
                }
                PastUseType.FLEXIRIDE -> {
                    textView.text = textView.context.getText(R.string.flexiride)
                }
                PastUseType.TAXI -> {
                    textView.text = textView.context.getText(R.string.taxi_invoicing)
                }
            }
        }


    }

}