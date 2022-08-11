package com.vektortelekom.android.vservice.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.PersonnelModel
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.MenuMainFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import javax.inject.Inject

class MenuMainFragment : BaseFragment<MenuViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    lateinit var binding: MenuMainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<MenuMainFragmentBinding>(inflater, R.layout.menu_main_fragment, container, false).apply {
            lifecycleOwner = this@MenuMainFragment
            viewModel = this@MenuMainFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModel.isPoolCarActive) {
            binding.buttonDrivingLicense.visibility = View.VISIBLE
        }

        if (AppDataManager.instance.personnelInfo == null) {
            viewModel.getPersonnelInfo()
        } else {
            fillProfileInfo(AppDataManager.instance.personnelInfo!!)
        }

        viewModel.personnelDetailsResponse.observe(viewLifecycleOwner) { response ->
            fillProfileInfo(response.response)
        }

        viewModel.isAddressNotValid.observe(viewLifecycleOwner) { result ->
            if (result) {
                viewModel.navigator?.showMenuAddressesFragment(null)
            }
        }

        viewModel.profilePhotoUuid.observe(viewLifecycleOwner) {

            val url: String = AppApiHelper().baseUrl
                    .plus("/")
                    .plus(BuildConfig.BASE_APP_NAME)
                    .plus("/doc/file/")
                    .plus(it)
                    .plus("/view/profile.jpeg?vektor-token=")
                    .plus(stateManager.vektorToken ?: "")

            val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)

            GlideApp.with(this).setDefaultRequestOptions(requestOptions).load(url).into(binding.imageViewProfile)

        }

        binding.buttonDrivingLicense.setOnClickListener {
            AppDataManager.instance.carShareUser?.user?.accountId?.let {
                viewModel.getLatestDrivingLicenceDocument(it.toString())
            } ?: kotlin.run {
                viewModel.getCustomerStatus()
            }

        }


        viewModel.customerStatus.observe(viewLifecycleOwner) {
            viewModel.customerStatus.value?.user?.accountId?.let {
                viewModel.getLatestDrivingLicenceDocument(it.toString())
            }
        }
    }

    private fun fillProfileInfo(personnelModel: PersonnelModel) {
        binding.textViewName.text = personnelModel.name.plus(" ").plus(personnelModel.surname)
        binding.textViewCompany.text = personnelModel.company?.title

        viewModel.profilePhotoUuid.value = AppDataManager.instance.personnelInfo?.profileImageUuid

    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuMainFragment"
        fun newInstance() = MenuMainFragment()
    }

}