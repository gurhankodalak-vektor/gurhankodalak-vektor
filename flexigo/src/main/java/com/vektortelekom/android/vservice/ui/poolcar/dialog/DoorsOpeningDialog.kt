package com.vektortelekom.android.vservice.ui.poolcar.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.PoolCarDoorsOpeningDialogBinding

class DoorsOpeningDialog(context: Context, private val isOpen: Boolean): Dialog(context) {

    private var countDownTimer: CountDownTimer? = null

    private lateinit var binding: PoolCarDoorsOpeningDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = PoolCarDoorsOpeningDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        if(isOpen) {
            binding.textViewTitle.text = context.getString(R.string.doors_opening_title)
            binding.textViewInfo.text = context.getString(R.string.doors_opening_info)
            binding.textViewInfoBottom.text = context.getString(R.string.doors_opening_info_bottom)
        }
        else {
            binding.textViewTitle.text = context.getString(R.string.doors_closing_title)
            binding.textViewInfo.text = context.getString(R.string.doors_closing_info)
            binding.textViewInfoBottom.text = context.getString(R.string.doors_closing_info_bottom)
        }

    }

    private fun startProgressCountDown() {
        countDownTimer?.cancel()

        val max = 100
        val interval = 30L
        binding.progress.max = max
        binding.progress.progress = max
        binding.progress.secondaryProgress = max
        val millisInFuture = 3 * 1000

        countDownTimer = object : CountDownTimer(millisInFuture.toLong(), interval) {
            override fun onTick(millisUntilFinished: Long) {
                val time = max - (millisUntilFinished / interval).toInt()
                binding.progress.secondaryProgress = time
            }

            override fun onFinish() {
                binding.progress.secondaryProgress = max
                startProgressCountDown()
            }
        }
        countDownTimer?.start()
    }

    private fun stopProgressCountDown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startProgressCountDown()
        Handler().postDelayed({
        }, 3000)
    }

    override fun onDetachedFromWindow() {
        stopProgressCountDown()
        super.onDetachedFromWindow()
    }

}