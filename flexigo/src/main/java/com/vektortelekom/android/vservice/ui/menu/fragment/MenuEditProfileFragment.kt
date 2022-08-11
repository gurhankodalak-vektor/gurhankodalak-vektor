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
import com.vektortelekom.android.vservice.databinding.MenuEditProfileFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import com.vektortelekom.android.vservice.utils.GlideApp
import javax.inject.Inject

class MenuEditProfileFragment : BaseFragment<MenuViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    lateinit var binding: MenuEditProfileFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<MenuEditProfileFragmentBinding>(inflater, R.layout.menu_edit_profile_fragment, container, false).apply {
            lifecycleOwner = this@MenuEditProfileFragment
            viewModel = this@MenuEditProfileFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (AppDataManager.instance.personnelInfo == null) {
            viewModel.getPersonnelInfo()
        } else {
            fillProfileInfo(AppDataManager.instance.personnelInfo!!)
        }

        viewModel.personnelDetailsResponse.observe(viewLifecycleOwner) { response ->
            fillProfileInfo(response.response)
        }

        viewModel.editProfileSuccess.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                viewModel.editProfileSuccess.value = null

                val dialog = AppDialog.Builder(requireContext())
                        .setIconVisibility(true)
                        .setTitle(R.string.dialog_message_edit_profile_success)
                        .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                            dialog.dismiss()
                            viewModel.navigator?.returnMenuMainFragment()
                        }
                        .create()

                dialog.show()
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
    }

    private fun fillProfileInfo(personnelModel: PersonnelModel) {

        viewModel.name.value = personnelModel.name.plus("\n").plus(personnelModel.surname)
        viewModel.mail.value = personnelModel.email
        viewModel.phone.value = personnelModel.phoneNumber
        viewModel.company.value = personnelModel.company?.title
        viewModel.address.value = personnelModel.homeLocation?.address

        /*
        val url: String = AppApiHelper().baseUrl.plus("/").plus(BuildConfig.BASE_APP_NAME).plus("/doc/file/").plus(personnelModel.profileImageUuid).plus("/view/profile.jpeg")
        val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
        val glideUrl = GlideUrl(url, LazyHeaders.Builder()
                .addHeader("vektor-token", stateManager.vektorToken?:"")
                .addHeader("Content-Type", "image/png;charset=UTF-8")
                .addHeader("platform", "android").build())

        GlideApp.with(requireContext()).setDefaultRequestOptions(requestOptions).load(glideUrl).into(binding.imageViewProfile)
        */
    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuEditProfileFragment"
        fun newInstance() = MenuEditProfileFragment()
    }
}