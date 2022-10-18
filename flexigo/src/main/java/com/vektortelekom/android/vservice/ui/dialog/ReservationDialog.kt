package com.vektortelekom.android.vservice.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.databinding.ReservationDialogBinding

class ReservationDialog(private val param: ReservationDialogBindingParams): Dialog(param.context) {

    private lateinit var binding: ReservationDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ReservationDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        param.apply(this)

    }


    fun setTitle(title: String) {
        binding.textViewTitle.text = title
    }

    fun setText1(text1: String) {
        binding.textViewText1.text = text1
    }

    fun setText2(text2: String) {
        binding.textViewText2.visibility = View.VISIBLE
        binding.textViewText2.text = text2
    }

    private fun setOkButton(name: String, listener: OnClickListener) {
        binding.buttonSubmit.visibility = View.VISIBLE
        binding.buttonSubmit.text = name
        binding.buttonSubmit.setOnClickListener {
            listener.onClick(this)
        }
    }

    private fun setOkAndCancelButton(nameOk: String, listenerOk: OnClickListener, nameCancel: String, listenerCancel: OnClickListener) {
        binding.layoutOkCancel.visibility = View.VISIBLE
        binding.buttonSubmit1.text = nameOk
        binding.buttonSubmit1.setOnClickListener {
            listenerOk.onClick(this)
        }
        binding.buttonCancel.text = nameCancel
        binding.buttonCancel.setOnClickListener {
            listenerCancel.onClick(this)
        }
    }


    class Builder (val context: Context) {

        var param = ReservationDialogBindingParams(context)

        fun setTitle(title: String): Builder {
            param.title = title
            return this
        }

        fun setText1(text1: String): Builder {
            param.text1 = text1
            return this
        }

        fun setText2(text2: String): Builder {
            param.text2 = text2
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            param.isCancelable = cancelable
            return this
        }

        inline fun setOkButton(buttonName: String, crossinline listener: (ReservationDialog) -> Unit): Builder {
            param.okButtonText = buttonName
            param.mOkButtonListener = object : OnClickListener {
                override fun onClick(dialog: ReservationDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        inline fun setCancelButton(buttonName: String, crossinline listener: (ReservationDialog) -> Unit): Builder {
            param.cancelButtonText = buttonName
            param.mCancelButtonListener = object : OnClickListener {
                override fun onClick(dialog: ReservationDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        fun setIconVisibility(isVisible: Boolean): Builder {
            param.isIconVisible = isVisible
            return this
        }

        fun create(): ReservationDialog {
            return ReservationDialog(param)
        }

    }

    interface OnClickListener {
        fun onClick(dialog: ReservationDialog)
    }

    class ReservationDialogBindingParams (val context: Context) {

        var iconId: Int? = null

        var title: String? = null
        var text1: String? = null
        var text2: String? = null

        var okButtonText: String? = null
        var mOkButtonListener: OnClickListener? = null

        var cancelButtonText: String? = null
        var mCancelButtonListener: OnClickListener? = null

        var isIconVisible: Boolean = true

        var isCancelable: Boolean = false

        fun apply(dialog: ReservationDialog) {

            title?.let {
                dialog.setTitle(it)
            }

            text1?.let {
                dialog.setText1(it)
            }

            text2?.let {
                dialog.setText2(it)
            }

            dialog.setCanceledOnTouchOutside(isCancelable)
            dialog.setCancelable(isCancelable)

            if (okButtonText != null) {
                if(cancelButtonText == null) {
                    dialog.setOkButton(okButtonText!!, mOkButtonListener!!)
                }
                else {
                    dialog.setOkAndCancelButton(okButtonText!!, mOkButtonListener!!, cancelButtonText!!, mCancelButtonListener!!)
                }
            }


        }

    }

}