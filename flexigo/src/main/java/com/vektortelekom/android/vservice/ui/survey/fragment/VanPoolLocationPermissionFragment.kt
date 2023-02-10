package com.vektortelekom.android.vservice.ui.survey.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.PermissionsUtils
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.databinding.VanpoolLocationPermissionBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.survey.SurveyViewModel
import javax.inject.Inject


class VanPoolLocationPermissionFragment: BaseFragment<SurveyViewModel>(), PermissionsUtils.LocationStateListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: SurveyViewModel
    private lateinit var buttonContinue: AppCompatButton
    private lateinit var imageToolbar: AppCompatImageButton

    lateinit var binding: VanpoolLocationPermissionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<VanpoolLocationPermissionBinding>(inflater, R.layout.vanpool_location_permission, container, false).apply {
            lifecycleOwner = this@VanPoolLocationPermissionFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonContinue = requireActivity().findViewById<View>(R.id.button_continue) as AppCompatButton
        imageToolbar = requireActivity().findViewById<View>(R.id.imagebutton_layout_toolbar) as AppCompatImageButton
        buttonContinue.text = getString(R.string.got_it)


        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.endToEnd = ConstraintSet.PARENT_ID
        layoutParams.startToStart = ConstraintSet.PARENT_ID
        layoutParams.topToTop = ConstraintSet.PARENT_ID

        layoutParams.topMargin = 100

        imageToolbar.layoutParams = layoutParams

        viewModel.isLocationPermissionVisible = true
    }

    override fun getViewModel(): SurveyViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[SurveyViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "VanPoolLocationPermissionFragment"
        fun newInstance() = VanPoolLocationPermissionFragment()

    }

    override fun onLocationPermissionOk() {
        viewModel.isLocationPermissionSuccess = true
        viewModel.navigator?.showMenuAddressesFragment()
    }

    override fun onLocationPermissionFailed() {
        viewModel.isLocationPermissionSuccess = false
        viewModel.navigator?.showMenuAddressesFragment()
    }
}