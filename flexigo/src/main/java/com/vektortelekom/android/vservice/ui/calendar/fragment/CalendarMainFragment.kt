package com.vektortelekom.android.vservice.ui.calendar.fragment

import android.accounts.Account
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.CalendarList
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.microsoft.graph.options.QueryOption
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.shrikanthravi.collapsiblecalendarview.data.Day
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.CalendarMainFragmentBinding
import com.vektortelekom.android.vservice.ui.base.BaseFragment
import com.vektortelekom.android.vservice.ui.calendar.CalendarActivity
import com.vektortelekom.android.vservice.ui.calendar.CalendarViewModel
import com.vektortelekom.android.vservice.ui.calendar.dialog.CalendarDialog
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideActivity
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationActivity
import com.vektortelekom.android.vservice.utils.*
import java.util.*
import javax.inject.Inject

class CalendarMainFragment: BaseFragment<CalendarViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: CalendarViewModel

    lateinit var binding: CalendarMainFragmentBinding

    var calendarStartDate: Long? = null
    var calendarEndDate: Long? = null

    private var googleCalendarItemList: MutableList<CalendarActivity.CalendarItem>? = null
    var outlookCalendarItemList: MutableList<CalendarActivity.CalendarItem>? = null
    private var shuttleCalendarItemList: MutableList<CalendarActivity.CalendarItem>? = null

    var isGoogleCalendarItemListOk = true
    var isOutlookCalendarItemListOk = true


    private var shuttleOutgoingStartMinutes = 8 * 60
    private var shuttleOutgoingEndMinutes = 9 * 60

    private var shuttleIncomingStartMinutes = 18 * 60
    private var shuttleIncomingEndMinutes = 19 * 60

    private lateinit var handler: Handler
    private lateinit var googleClient: GoogleSignInClient

    var calendarViewsGoogle: MutableList<View>? = null
    var calendarViewsOutlook: MutableList<View>? = null
    var calendarViewsShuttle: MutableList<View>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<CalendarMainFragmentBinding>(inflater, R.layout.calendar_main_fragment, container, false).apply {
            lifecycleOwner = this@CalendarMainFragment
            viewModel = this@CalendarMainFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        calendarStartDate = Date().convertForBackend().convertFromBackendToLong()!!
        calendarStartDate?.let {
            calendarEndDate = it + 1000 * 60 * 60 * 24

            getEventListGoogle(AppDataManager.instance.googleCalendarAccessToken)
            getEventListOutlook(AppDataManager.instance.outlookCalendarAccessToken)

            viewModel.getShuttleUseDays(Date(it))
        }

        binding.viewBottomCalendar.setOnClickListener {
            if (binding.calendarView.expanded) {
                binding.calendarView.collapse(500)
            } else {
                binding.calendarView.expand(500)
            }
        }

        binding.viewBottomCalendar.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {

                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                }
                else -> {

                }
            }
            return@setOnTouchListener true
        }

        val calendarToday = Calendar.getInstance(TimeZone.getDefault())

        binding.calendarView.select(Day(calendarToday.get(Calendar.YEAR), calendarToday.get(Calendar.MONTH), calendarToday.get(Calendar.DATE)))

        binding.calendarView.setCalendarListener(object : CollapsibleCalendar.CalendarListener {
            override fun onClickListener() {
            }

            override fun onDataUpdate() {
            }

            override fun onDayChanged() {
            }

            override fun onDaySelect() {

                binding.calendarView.collapse(500)

                calendarViewsGoogle?.let {
                    for (calendarView in it) {
                        binding.layoutCalendar.removeView(calendarView)
                    }
                }

                calendarViewsOutlook?.let {
                    for (calendarView in it) {
                        binding.layoutCalendar.removeView(calendarView)
                    }
                }

                calendarViewsShuttle?.let {
                    for (calendarView in it) {
                        binding.layoutCalendar.removeView(calendarView)
                    }
                }

                calendarViewsGoogle = mutableListOf()
                calendarViewsOutlook = mutableListOf()
                calendarViewsShuttle = mutableListOf()

                val calendar = Calendar.getInstance(TimeZone.getDefault())


                binding.calendarView.selectedDay?.let { day ->

                    calendar.set(day.year, day.month, day.day)
                    calendarStartDate = calendar.timeInMillis

                    calendarStartDate?.let { startDate ->
                        calendarEndDate = startDate + 1000 * 60 * 60 * 24

                        getEventListGoogle(AppDataManager.instance.googleCalendarAccessToken)
                        getEventListOutlook(AppDataManager.instance.outlookCalendarAccessToken)

                        viewModel.getShuttleUseDays(Date(startDate))

                    }

                }


            }

            override fun onItemClick(v: View) {
            }

            override fun onMonthChange() {
            }

            override fun onWeekChange(position: Int) {
            }

        })

        binding.textView0800.postDelayed({
            binding.scrollView.scrollY = binding.textView0800.y.toInt()
        }, 500)

        viewModel.shuttleUseDays.observe(viewLifecycleOwner) { shuttleDays ->

            shuttleCalendarItemList = mutableListOf()

            if (shuttleDays.isNotEmpty()) {
                val shuttleDay = shuttleDays[0]
                if (shuttleDay.isOutgoing == true) {
                    shuttleCalendarItemList?.add(CalendarActivity.CalendarItem(shuttleOutgoingStartMinutes, shuttleOutgoingEndMinutes, "Shuttle Outgoing",
                            shuttleDay.shuttleDay.convertBackendDateToLong()
                                    ?: (0L + shuttleOutgoingStartMinutes * 1000 * 60), shuttleDay.shuttleDay.convertBackendDateToLong()
                            ?: (0L + shuttleOutgoingEndMinutes * 1000 * 60), ""))
                }
                if (shuttleDay.isIncoming == true) {
                    shuttleCalendarItemList?.add(CalendarActivity.CalendarItem(shuttleIncomingStartMinutes, shuttleIncomingEndMinutes, "Shuttle Incoming",
                            shuttleDay.shuttleDay.convertBackendDateToLong()
                                    ?: (0L + shuttleIncomingStartMinutes * 1000 * 60), shuttleDay.shuttleDay.convertBackendDateToLong()
                            ?: (0L + shuttleIncomingEndMinutes * 1000 * 60), ""))
                }
            }

            setCalendarItems()
        }

        viewModel.googleCalendarAccessToken.observe(viewLifecycleOwner) { it ->
            if (it == null) {
                calendarViewsGoogle?.let {
                    for (calendarView in it) {
                        binding.layoutCalendar.removeView(calendarView)
                    }
                }
                calendarViewsGoogle = mutableListOf()
            } else {
                getEventListGoogle(it)
            }


        }

        viewModel.outlookCalendarEmail.observe(viewLifecycleOwner) { outlookEmail ->
            val configFile = R.raw.msal_config

            PublicClientApplication.createMultipleAccountPublicClientApplication(requireContext(),
                    configFile,
                    object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                        override fun onCreated(application: IMultipleAccountPublicClientApplication?) {

                            Thread {

                                if (outlookEmail == null) {

                                } else {
                                    val account: IAccount? = application?.getAccount(outlookEmail)

                                    if (account == null) {
                                        activity?.runOnUiThread {
                                            Toast.makeText(requireContext(), "Outlook account null", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        val newScopes = arrayOf("Calendars.Read")

                                        val authority: String = application.configuration.defaultAuthority.authorityURL.toString()

                                        //Use default authority to request token from pass null

                                        //Use default authority to request token from pass null
                                        val result: IAuthenticationResult = application.acquireTokenSilent(newScopes, account, authority)

                                        getEventListOutlook(result.accessToken)

                                    }

                                }


                            }.start()

                        }

                        override fun onError(exception: MsalException?) {
                            //Log Exception Here
                        }
                    })


        }

        binding.buttonAccounts.setOnClickListener {
            viewModel.navigator?.showCalendarAccountsFragment(null)
        }


    }

    private fun getEventListOutlook(outlookAccessToken: String?) {

        outlookAccessToken?.let { token ->

            Thread {

                try {

                    val graphClient: IGraphServiceClient = GraphServiceClient.builder()
                            .authenticationProvider {
                                it.addHeader("Authorization", "Bearer ".plus(token))
                            }
                            //.authenticationProvider(authProvider)
                            .buildClient()

                    val events = graphClient.me().events()
                            .buildRequest(listOf(QueryOption("startDateTime", calendarStartDate?.convertISO8601String()), QueryOption("endDateTime", calendarEndDate?.convertISO8601String())))
                            .get()

                    events?.let {
                        outlookCalendarItemList = mutableListOf()

                        for(event in it.currentPage.iterator()) {

                            val eventStartLong = event.start.dateTime.convertISO8601toLong()

                            if(eventStartLong >= calendarStartDate?:0L && eventStartLong <= calendarEndDate?:0L) {
                                outlookCalendarItemList?.add(CalendarActivity.CalendarItem(event.start.dateTime.convertISO8601toTotalMinutesOfDay(), event.end.dateTime.convertISO8601toTotalMinutesOfDay(), event.subject?:""
                                ?: "", eventStartLong, event.end.dateTime.convertISO8601toLong(), event.location?.displayName?:""))

                            }

                        }

                        activity?.runOnUiThread {
                            setCalendarItems()
                        }
                    }
                }
                catch (e: Exception) {

                    val configFile = R.raw.msal_config

                    PublicClientApplication.createMultipleAccountPublicClientApplication(requireContext(),
                            configFile,
                            object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                                override fun onCreated(application: IMultipleAccountPublicClientApplication?) {

                                    Thread {

                                        val outlookEmail = AppDataManager.instance.outlookCalendarEmail

                                        if (outlookEmail == null) {

                                        } else {
                                            val account: IAccount? = application?.getAccount(outlookEmail)

                                            if (account == null) {
                                                activity?.runOnUiThread {
                                                    Toast.makeText(requireContext(), "Outlook account null", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                val newScopes = arrayOf("Calendars.Read")

                                                val authority: String = application.configuration.defaultAuthority.authorityURL.toString()

                                                //Use default authority to request token from pass null

                                                //Use default authority to request token from pass null
                                                val result: IAuthenticationResult = application.acquireTokenSilent(newScopes, account, authority)


                                                val graphClient: IGraphServiceClient = GraphServiceClient.builder()
                                                        .authenticationProvider {
                                                            it.addHeader("Authorization", "Bearer ".plus(result.accessToken))
                                                        }
                                                        //.authenticationProvider(authProvider)
                                                        .buildClient()



                                                val events = graphClient.me().events()
                                                        .buildRequest(listOf(QueryOption("startDateTime", calendarStartDate?.convertISO8601String()), QueryOption("endDateTime", calendarEndDate?.convertISO8601String())))
                                                        .get()

                                                events?.let {
                                                    outlookCalendarItemList = mutableListOf()

                                                    for(event in it.currentPage.iterator()) {

                                                        val eventStartLong = event.start.dateTime.convertISO8601toLong()

                                                        if(eventStartLong >= calendarStartDate?:0L && eventStartLong <= calendarEndDate?:0L) {
                                                            outlookCalendarItemList?.add(CalendarActivity.CalendarItem(event.start.dateTime.convertISO8601toTotalMinutesOfDay(), event.end.dateTime.convertISO8601toTotalMinutesOfDay(), event.subject?:""
                                                            ?: "", eventStartLong, event.end.dateTime.convertISO8601toLong(), event.location?.displayName?:""))

                                                        }

                                                    }

                                                    activity?.runOnUiThread {
                                                        setCalendarItems()
                                                    }
                                                }

                                            }

                                        }


                                    }.start()

                                }

                                override fun onError(exception: MsalException?) {
                                    //Log Exception Here
                                }
                            })


                }




            }
            .start()
        }
    }

    private fun getEventListGoogle(googleAccessToken: String?) {

        googleAccessToken?.let {
            Thread {


                var calendarList: CalendarList

                var calendarService : com.google.api.services.calendar.Calendar

                try {

                    val credential = GoogleCredential()
                    credential.accessToken = it

                    calendarService = com.google.api.services.calendar.Calendar.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), credential).build()


                    calendarList = calendarService.CalendarList().list().execute()
                }
                catch (e: Exception) {

                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Refresh token", Toast.LENGTH_SHORT).show()
                    }

                    val scope = "oauth2:" + "https://www.googleapis.com/auth/calendar"
                    val account = Account(AppDataManager.instance.googleCalendarEmail, "com.google")
                    val token: String = GoogleAuthUtil.getToken(requireContext(), account, scope)

                    AppDataManager.instance.googleCalendarAccessToken = token

                    val credential = GoogleCredential()
                    credential.accessToken = token

                    calendarService = com.google.api.services.calendar.Calendar.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), credential).build()

                    calendarList = calendarService.CalendarList().list().execute()
                }

                if (calendarList.isNotEmpty()) {
                    val eventList = calendarService.events().list(calendarList.items[0].id).setTimeMin(DateTime(calendarStartDate
                            ?: 0L)).setTimeMax(DateTime(calendarEndDate ?: 0L)).execute()

                    handler.post {

                        googleCalendarItemList = mutableListOf()

                        for (event in eventList.items) {

                            googleCalendarItemList?.add(CalendarActivity.CalendarItem(event.start?.dateTime?.value?.convertNowToTotalMinutesOfDay()?:0, event.end?.dateTime?.value?.convertNowToTotalMinutesOfDay()?:0, event.summary
                                    ?: "", event.start?.dateTime?.value?:0, event.end?.dateTime?.value?:0, event.location
                                    ?: ""))
                        }

                        setCalendarItems()

                    }
                }
            }.start()
        }
    }

    private fun setCalendarItems() {

        calendarViewsGoogle?.let {
            for (calendarView in it) {
                binding.layoutCalendar.removeView(calendarView)
            }
        }

        calendarViewsOutlook?.let {
            for (calendarView in it) {
                binding.layoutCalendar.removeView(calendarView)
            }
        }

        calendarViewsShuttle?.let {
            for (calendarView in it) {
                binding.layoutCalendar.removeView(calendarView)
            }
        }

        calendarViewsGoogle = mutableListOf()
        calendarViewsOutlook = mutableListOf()
        calendarViewsShuttle = mutableListOf()

        var minStartValue = 24 * 60
        var isAdded = false

        val addedCalendarItems: MutableList<MutableList<CalendarActivity.CalendarItem>> = mutableListOf()

        googleCalendarItemList?.let {
            for (calendarItem in it) {


                var hasCollision = true

                for (columnIndex in addedCalendarItems.indices) {

                    hasCollision = false

                    val addedColumn = addedCalendarItems[columnIndex]

                    for (addedRow in addedColumn) {
                        if (((calendarItem.endMinutes < addedRow.startMinutes)
                                        || (calendarItem.startMinutes > addedRow.endMinutes)).not()) {
                            hasCollision = true
                            break
                        }
                    }

                    if (hasCollision.not()) {
                        isAdded = true

                        if (calendarItem.startMinutes < minStartValue) {
                            minStartValue = calendarItem.startMinutes
                        }

                        calendarViewsGoogle?.add(addCalendarView(columnIndex, calendarItem))
                        addedColumn.add(calendarItem)

                        break
                    }

                }

                if (hasCollision) {

                    isAdded = true

                    if (calendarItem.startMinutes < minStartValue) {
                        minStartValue = calendarItem.startMinutes
                    }


                    calendarViewsGoogle?.add(addCalendarView(addedCalendarItems.size, calendarItem))

                    val newColumn = mutableListOf<CalendarActivity.CalendarItem>()
                    newColumn.add(calendarItem)
                    addedCalendarItems.add(newColumn)
                }

            }
        }

        outlookCalendarItemList?.let {
            for (calendarItem in it) {

                var hasCollision = true

                for (columnIndex in addedCalendarItems.indices) {

                    hasCollision = false

                    val addedColumn = addedCalendarItems[columnIndex]

                    for (addedRow in addedColumn) {
                        if (((calendarItem.endMinutes < addedRow.startMinutes)
                                        || (calendarItem.startMinutes > addedRow.endMinutes)).not()) {
                            hasCollision = true
                            break
                        }
                    }

                    if (hasCollision.not()) {
                        isAdded = true

                        if (calendarItem.startMinutes < minStartValue) {
                            minStartValue = calendarItem.startMinutes
                        }

                        calendarViewsOutlook?.add(addCalendarView(columnIndex, calendarItem))
                        addedColumn.add(calendarItem)

                        break
                    }

                }

                if (hasCollision) {

                    isAdded = true

                    if (calendarItem.startMinutes < minStartValue) {
                        minStartValue = calendarItem.startMinutes
                    }

                    calendarViewsOutlook?.add(addCalendarView(addedCalendarItems.size, calendarItem))

                    val newColumn = mutableListOf<CalendarActivity.CalendarItem>()
                    newColumn.add(calendarItem)
                    addedCalendarItems.add(newColumn)
                }

            }
        }

        shuttleCalendarItemList?.let {
            for (calendarItem in it) {

                var hasCollision = true

                for (columnIndex in addedCalendarItems.indices) {

                    hasCollision = false

                    val addedColumn = addedCalendarItems[columnIndex]

                    for (addedRow in addedColumn) {
                        if (((calendarItem.endMinutes < addedRow.startMinutes)
                                        || (calendarItem.startMinutes > addedRow.endMinutes)).not()) {
                            hasCollision = true
                            break
                        }
                    }

                    if (hasCollision.not()) {
                        isAdded = true

                        if (calendarItem.startMinutes < minStartValue) {
                            minStartValue = calendarItem.startMinutes
                        }

                        calendarViewsShuttle?.add(addCalendarView(columnIndex, calendarItem))
                        addedColumn.add(calendarItem)

                        break
                    }

                }

                if (hasCollision) {

                    isAdded = true

                    if (calendarItem.startMinutes < minStartValue) {
                        minStartValue = calendarItem.startMinutes
                    }

                    calendarViewsShuttle?.add(addCalendarView(addedCalendarItems.size, calendarItem))

                    val newColumn = mutableListOf<CalendarActivity.CalendarItem>()
                    newColumn.add(calendarItem)
                    addedCalendarItems.add(newColumn)
                }

            }
        }

        /*if(isAdded) {
            var layoutParams = binding.layoutCalendar.layoutParams
            layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            binding.layoutCalendar.layoutParams = layoutParams

            layoutParams = binding.cardViewCalendar.layoutParams
            layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            binding.cardViewCalendar.layoutParams = layoutParams

            binding.scrollView.scrollY = minStartValue.toFloat().dpToPx(this)
        }
        else {
            var layoutParams = binding.layoutCalendar.layoutParams
            layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            binding.layoutCalendar.layoutParams = layoutParams

            layoutParams = binding.cardViewCalendar.layoutParams
            layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            binding.cardViewCalendar.layoutParams = layoutParams
        }*/

    }

    private fun addCalendarView(columnIndex: Int, calendarItem: CalendarActivity.CalendarItem): View {

        val calendarView = layoutInflater.inflate(R.layout.calendar_item_view, null)

        val textViewTitle: MaterialTextView = calendarView.findViewById(R.id.text_view_calendar_title)
        textViewTitle.text = calendarItem.title

        val textViewCalendarDate: MaterialTextView = calendarView.findViewById(R.id.text_view_calendar_date)
        textViewCalendarDate.text = calendarItem.startMinutes.convertHoursAndMinutes().plus(" - ").plus(calendarItem.endMinutes.convertHoursAndMinutes())

        calendarView.id = View.generateViewId()
        binding.layoutCalendar.addView(calendarView)

        val topMinDiff = calendarItem.startMinutes
        val height = calendarItem.endMinutes - calendarItem.startMinutes
        val constraintSet = ConstraintSet()
        constraintSet.constrainWidth(calendarView.id, 150f.dpToPx(requireContext()))
        constraintSet.constrainHeight(calendarView.id, height.toFloat().dpToPx(requireContext()))
        constraintSet.connect(calendarView.id, ConstraintSet.START, binding.viewLine0000.id, ConstraintSet.START, (16 + columnIndex * 16 + columnIndex * 150).toFloat().dpToPx(requireContext()))
        constraintSet.connect(calendarView.id, ConstraintSet.TOP, binding.viewLine0000.id, ConstraintSet.TOP, topMinDiff.toFloat().dpToPx(requireContext()))
        constraintSet.applyTo(binding.layoutCalendar)

        calendarView.setOnClickListener {
            CalendarDialog(requireContext(), calendarItem, object : CalendarDialog.CalendarDialogListener {
                override fun calendarItemClickedPoolCar(calendarItem: CalendarActivity.CalendarItem, dialog: Dialog) {
                    dialog.dismiss()
                    val intent = Intent(requireContext(), PoolCarReservationActivity::class.java)
                    intent.putExtra("isAdd", true)
                    intent.putExtra("startTime", calendarItem.startTime)
                    intent.putExtra("endTime", calendarItem.endTime)
                    startActivity(intent)
                }

                override fun calendarItemClickedFlexiride(calendarItem: CalendarActivity.CalendarItem, dialog: Dialog) {
                    val intent = Intent(requireContext(), FlexirideActivity::class.java)
                    intent.putExtra("is_list", false)
                    intent.putExtra("startTime", calendarItem.startTime)
                    intent.putExtra("endTime", calendarItem.endTime)
                    intent.putExtra("location", calendarItem.location?:"")
                    startActivity(intent)
                }

            }).show()
        }

        return calendarView

    }


    override fun getViewModel(): CalendarViewModel {
        viewModel = activity?.run { ViewModelProvider(requireActivity(), factory)[CalendarViewModel::class.java] }
                ?: throw Exception("Invalid Activity")
        return viewModel
    }

    companion object {
        const val TAG: String = "CalendarMainFragment"

        fun newInstance() = CalendarMainFragment()

    }

}