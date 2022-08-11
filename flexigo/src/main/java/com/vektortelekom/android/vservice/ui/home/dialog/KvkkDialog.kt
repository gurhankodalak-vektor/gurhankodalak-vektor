package com.vektortelekom.android.vservice.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.databinding.KvkkDialogBinding
import java.net.URL

class KvkkDialog(context: Context, val url: String, val onclick: () -> Unit): Dialog(context) {

    private lateinit var binding: KvkkDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = KvkkDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setCanceledOnTouchOutside(false)
        setCancelable(false)

        Thread {
            try{
                binding.pdfViewer.fromStream(URL(url).openStream()).load()
            }
            catch (e: Exception) {
            }
        }.start()

        binding.buttonSubmit.setOnClickListener {
            onclick()
        }

    }

}