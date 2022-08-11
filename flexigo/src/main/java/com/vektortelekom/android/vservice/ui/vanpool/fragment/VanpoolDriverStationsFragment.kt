package com.vektortelekom.android.vservice.ui.vanpool.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vektor.ktx.utils.logger.AppLogger
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.PersonsModel
import com.vektortelekom.android.vservice.data.model.StationModel
import com.vektortelekom.android.vservice.databinding.VanpoolDriverStationsBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleViewModel
import com.vektortelekom.android.vservice.ui.vanpool.adapter.VanpoolDriverStationsAdapter
import javax.inject.Inject

class VanpoolDriverStationsFragment : BaseFragment<ShuttleViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: ShuttleViewModel

    lateinit var binding: VanpoolDriverStationsBinding

    private var vanpoolDriverStationsAdapter: VanpoolDriverStationsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<VanpoolDriverStationsBinding>(inflater, R.layout.vanpool_driver_stations, container, false).apply {
            lifecycleOwner = this@VanpoolDriverStationsFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textviewStationName.text = viewModel.routeResponse.value?.route?.title

        vanpoolDriverStationsAdapter = VanpoolDriverStationsAdapter(object : VanpoolDriverStationsAdapter.VanpoolDriverStationsItemClickListener{
            override fun onNavigateClick(model: StationModel) {
                navigateToMap(viewModel.myLocation?.latitude ?: 0.0, viewModel.myLocation?.longitude
                        ?: 0.0, model.location.latitude, model.location.longitude)
            }

            override fun onPersonsClick(model: List<PersonsModel>?) {
                viewModel.vanpoolPassengers.value = model
                if (model != null) {
                    if (model.isNotEmpty())
                        viewModel.openVanpoolPassenger.value = true
                }
            }
        })
        vanpoolDriverStationsAdapter?.setDriverInfo(viewModel.routeResponse.value!!)
        vanpoolDriverStationsAdapter?.setStationList( viewModel.driverStationList.value!!)
        binding.recyclerviewStations.adapter = vanpoolDriverStationsAdapter
    }

    private fun navigateToMap(userLocationLatitude: Double, userLocationLongitude: Double, targetLocationLatitude: Double, targetLocationLongitude: Double) {
        val baseUri = "geo:%s,%s?q=%s,%s"
        val uri = String.format(baseUri, userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e1: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", userLocationLatitude, userLocationLongitude, targetLocationLatitude, targetLocationLongitude)))
                    startActivity(browserIntent)
                } catch (e2: java.lang.Exception) {
                    AppLogger.e(e, "NavigationAppNotFound")
                    Toast.makeText(requireContext(), R.string.Maps_No_Exist, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun getViewModel(): ShuttleViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[ShuttleViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "VanpoolDriverStationsFragment"

        fun newInstance() = VanpoolDriverStationsFragment()

    }

}