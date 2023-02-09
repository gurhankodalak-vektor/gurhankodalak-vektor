package com.vektortelekom.android.vservice.ui.menu.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.MenuSettingsFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.menu.MenuViewModel
import javax.inject.Inject


class MenuSettingsFragment : BaseFragment<MenuViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MenuViewModel

    private lateinit var binding: MenuSettingsFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<MenuSettingsFragmentBinding>(inflater, R.layout.menu_settings_fragment, container, false).apply {
            lifecycleOwner = this@MenuSettingsFragment
            viewModel = this@MenuSettingsFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchSoundEffect.isChecked = AppDataManager.instance.isSettingsSoundEffectsEnabled
        binding.switchNotifications.isChecked = AppDataManager.instance.isSettingsNotificationsEnabled
        binding.switchEmailNotificaitons.isChecked = AppDataManager.instance.isSettingsEmailNotificationsEnabled
        binding.switchShowPhone.isChecked = AppDataManager.instance.isSettingsShowPhoneEnabled

        try {
            val pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            val version = pInfo.versionName
            val versionCode = pInfo.versionCode

            binding.textviewVersion.text = getString(R.string.version).plus(" ").plus(version).plus(" ").plus("(").plus(versionCode).plus(")")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    override fun getViewModel(): MenuViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[MenuViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "MenuSettingsFragment"
        fun newInstance() = MenuSettingsFragment()
    }

}