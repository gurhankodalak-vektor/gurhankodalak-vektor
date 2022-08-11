package com.vektortelekom.android.vservice.ui.poolcar.reservation.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.vektortelekom.android.vservice.data.model.PersonnelModel
import com.vektortelekom.android.vservice.databinding.PoolCarAdditionalRidersDialogBinding
import com.vektortelekom.android.vservice.ui.poolcar.reservation.adapter.PersonListAdapter

class AdditionalRidersDialog(context: Context, private val personList: MutableList<PersonnelModel>, val searchEvent: (String) -> Unit, val closeEvent: () -> Unit): Dialog(context)  {

    private var foundUser: PersonnelModel? = null

    private lateinit var binding: PoolCarAdditionalRidersDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = PoolCarAdditionalRidersDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setCanceledOnTouchOutside(false)
        setCancelable(false)

        binding.recyclerViewPersonList.adapter = PersonListAdapter(personList)

        binding.buttonRegistrationNumber.setOnClickListener {
            foundUser = null
            binding.buttonFoundUser.text = ""
            binding.buttonAddUser.visibility = View.INVISIBLE
            searchEvent(binding.editTextRegistrationNumber.text.toString())
        }

        binding.buttonAddUser.setOnClickListener {
            foundUser?.let {
                personList.add(it)
                binding.recyclerViewPersonList.adapter?.notifyDataSetChanged()
                foundUser = null
                binding.buttonFoundUser.text = ""
                binding.buttonAddUser.visibility = View.INVISIBLE
            }
        }

        binding.buttonSubmit.setOnClickListener {
            closeEvent()
            dismiss()
        }

    }

    fun searchPersonCallback(person: PersonnelModel) {
        binding.buttonAddUser.visibility = View.VISIBLE
        foundUser = person
        binding.buttonFoundUser.text = person.fullName
    }

}