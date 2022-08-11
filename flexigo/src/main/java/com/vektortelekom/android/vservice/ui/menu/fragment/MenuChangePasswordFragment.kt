package com.vektortelekom.android.vservice.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.MenuChangePasswordFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import javax.inject.Inject

class MenuChangePasswordFragment : BaseFragment<MenuViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    lateinit var binding: MenuChangePasswordFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<MenuChangePasswordFragmentBinding>(inflater, R.layout.menu_change_password_fragment, container, false).apply {
            lifecycleOwner = this@MenuChangePasswordFragment
            viewModel = this@MenuChangePasswordFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.changePasswordSuccess.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                viewModel.changePasswordSuccess.value = null

                val dialog = AppDialog.Builder(requireContext())
                        .setIconVisibility(true)
                        .setTitle(R.string.dialog_message_change_password_success)
                        .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            viewModel.navigator?.returnMenuMainFragment()
                        }
                        .create()

                dialog.show()
            }

        }

    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuChangePasswordFragment"
        fun newInstance() = MenuChangePasswordFragment()
    }

}