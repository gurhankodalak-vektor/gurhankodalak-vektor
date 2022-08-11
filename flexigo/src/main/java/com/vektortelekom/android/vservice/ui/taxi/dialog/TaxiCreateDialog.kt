package com.vektortelekom.android.vservice.ui.taxi.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.databinding.TaxiCreateDialogBinding

class TaxiCreateDialog(private val param: TaxiCreateDialogParams): Dialog(param.context) {
    private lateinit var  binding : TaxiCreateDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = TaxiCreateDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        param.apply(this)

        binding.buttonCancel.setOnClickListener {
            if(isShowing) {
                dismiss()
            }
        }

    }

    private fun setOkButton(listener: OnClickListener) {
        binding.buttonSubmit.setOnClickListener {
            listener.onClick(this)
        }
    }

    fun setTitle(textTitle: String) {
        binding.textViewTitle.text = textTitle
    }

    fun setTextDate(textDate: String) {
        binding.textViewDate.text = textDate
    }

    fun setTextStart(textStart: String) {
        binding.textViewStart.text = textStart
    }

    fun setTextFinish(textFinish: String) {
        binding.textViewFinish.text = textFinish
    }

    fun setTextDescription(textDescription: String) {
        binding.layoutDesctiption.visibility = View.VISIBLE
        binding.textViewDescription.text = textDescription
    }

    fun setTextPay(textPay: String) {
        binding.layoutPay.visibility = View.VISIBLE
        binding.textViewPay.text = textPay
    }

    class Builder (val context: Context) {

        var param = TaxiCreateDialogParams(context)

        inline fun setOkButton(crossinline listener: (TaxiCreateDialog) -> Unit): Builder {
            param.mOkButtonListener = object : OnClickListener {
                override fun onClick(dialog: TaxiCreateDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        fun setTitle(textTitle: String): Builder {
            param.title = textTitle
            return this
        }

        fun setTextDate(textDate: String): Builder {
            param.textDate = textDate
            return this
        }

        fun setTextStart(textStart: String): Builder {
            param.textStart = textStart
            return this
        }

        fun setTextFinish(textFinish: String): Builder {
            param.textFinish = textFinish
            return this
        }

        fun setTextDescription(textDescription: String): Builder {
            param.textDescription = textDescription
            return this
        }

        fun setTextPay(textPay: String): Builder {
            param.textPay = textPay
            return this
        }

        fun create(): TaxiCreateDialog {
            return TaxiCreateDialog(param)
        }

    }

    interface OnClickListener {
        fun onClick(dialog: TaxiCreateDialog)
    }

    class TaxiCreateDialogParams (val context: Context) {

        var mOkButtonListener: OnClickListener? = null

        var mCancelButtonListener: OnClickListener? = null

        var title: String? = null
        var textDate: String? = null
        var textStart: String? = null
        var textFinish: String? = null
        var textDescription: String? = null
        var textPay: String? = null

        fun apply(dialog: TaxiCreateDialog) {

            title?.let {
                dialog.setTitle(it)
            }

            textDate?.let {
                dialog.setTextDate(it)
            }

            textStart?.let {
                dialog.setTextStart(it)
            }

            textFinish?.let {
                dialog.setTextFinish(it)
            }

            textDescription?.let {
                dialog.setTextDescription(it)
            }

            textPay?.let {
                dialog.setTextPay(it)
            }

            mOkButtonListener?.let {
                dialog.setOkButton(it)
            }

        }

    }

}