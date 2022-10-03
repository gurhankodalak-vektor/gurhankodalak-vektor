package com.vektortelekom.android.vservice.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.databinding.MessageBoxDialogBinding

class MessageBoxDialog(private var param: AppDialogParam) : Dialog(param.context) {

    private lateinit var binding: MessageBoxDialogBinding

       override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = MessageBoxDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        this.setCancelable(false)

        param.apply(this)
    }


    private fun setCloseButtonVisibility(isVisible: Boolean) {
//        binding.closeDialog.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    class Builder constructor(val context: Context) {
        var param = AppDialogParam(context)

        fun setIcon(@DrawableRes iconId: Int): Builder {
            param.iconId = iconId
            return this
        }

        fun setIcon(icon: Drawable): Builder {
            param.icon = icon
            return this
        }

        fun setTitle(@StringRes title: Int): Builder {
            param.title = param.context.getText(title).toString()
            return this
        }

        fun setTitle(title: String): Builder {
            param.title = title
            return this
        }

        fun setSubtitle(@StringRes subtitle: Int): Builder {
            param.subtitle = param.context.getText(subtitle).toString()
            return this
        }

        fun setSubtitle(subtitle: String): Builder {
            param.subtitle = subtitle
            return this
        }

        inline fun setOkButton(@StringRes textId: Int, crossinline listener: (MessageBoxDialog) -> Unit): Builder {
            return setOkButton(param.context.getText(textId).toString(), listener)
        }

        inline fun setOkButton(buttonName: String, crossinline listener: (MessageBoxDialog) -> Unit): Builder {
            param.okButtonText = buttonName
            param.mOkButtonListener = object : OnClickListener {
                override fun onClick(dialog: MessageBoxDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        inline fun setCancelButton(@StringRes textId: Int, crossinline listener: (MessageBoxDialog) -> Unit): Builder {
            return setCancelButton(param.context.getText(textId).toString(), listener)
        }

        inline fun setCancelButton(buttonName: String, crossinline listener: (MessageBoxDialog) -> Unit): Builder {
            param.cancelButtonText = buttonName
            param.mCancelButtonListener = object : OnClickListener {
                override fun onClick(dialog: MessageBoxDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        inline fun setOtherButton(@StringRes textId: Int, crossinline listener: (MessageBoxDialog) -> Unit): Builder {
            return setOtherButton(param.context.getText(textId).toString(), listener)
        }

        inline fun setOtherButton(buttonName: String, crossinline listener: (MessageBoxDialog) -> Unit): Builder {
            param.otherButtonText = buttonName
            param.mOtherButtonListener = object : OnClickListener {
                override fun onClick(dialog: MessageBoxDialog) {
                    listener(dialog)
                }
            }
            return this
        }

        fun setIconVisibility(isVisible: Boolean): Builder {
            param.isIconVisible = isVisible
            return this
        }

        fun setOkButtonIcon(iconId: Drawable, iconDirection: Int): Builder {
            param.okButtonIcon = iconId
            param.okIconDirection = iconDirection
            return this
        }

        fun setCloseButtonVisibility(isVisible: Boolean): Builder {
            param.isCloseButtonVisible = isVisible
            return this
        }

        fun create(): MessageBoxDialog {
            return MessageBoxDialog(param)
        }

    }

    interface OnClickListener {
        fun onClick(dialog: MessageBoxDialog)
    }

    class AppDialogParam constructor(val context: Context) {

        var iconId: Int? = null
        var icon: Drawable? = null
        var title: String? = null
        var subtitle: String? = null
        var okButtonText: String? = null
        var cancelButtonText: String? = null
        var otherButtonText: String? = null
        var isIconVisible: Boolean? = null
        var isCloseButtonVisible: Boolean? = true

        var okButtonIcon: Drawable? = null
        var okIconDirection: Int? = null

        var mOkButtonListener: OnClickListener? = null
        var mCancelButtonListener: OnClickListener? = null
        var mOtherButtonListener: OnClickListener? = null

        fun apply(dialog: MessageBoxDialog) {
            iconId?.let {
//                dialog.setIcon(it)
            }

            if (icon != null)
//                dialog.setIcon(icon!!)

            if (cancelButtonText != null)
//                dialog.setCancelButton(cancelButtonText!!, mCancelButtonListener!!)

            if (otherButtonText != null)
//                dialog.setOtherButton(otherButtonText!!, mOtherButtonListener!!)

            if (isIconVisible != null)
//                dialog.setIconVisibility(isIconVisible!!)

            if (okButtonIcon != null && okIconDirection != null)
//                dialog.setOkButtonIcon(okButtonIcon!!, okIconDirection!!)

            if (isCloseButtonVisible != null)
                dialog.setCloseButtonVisibility(isCloseButtonVisible!!)

        }
    }
}

