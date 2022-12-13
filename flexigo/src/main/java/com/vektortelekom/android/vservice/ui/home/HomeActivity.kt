package com.vektortelekom.android.vservice.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.data.model.*
import com.vektortelekom.android.vservice.data.remote.AppApiHelper
import com.vektortelekom.android.vservice.databinding.HomeActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.base.HighlightView
import com.vektortelekom.android.vservice.ui.calendar.CalendarActivity
import com.vektortelekom.android.vservice.ui.carpool.CarPoolActivity
import com.vektortelekom.android.vservice.ui.carpool.CarPoolQrCodeActivity
import com.vektortelekom.android.vservice.ui.comments.CommentsActivity
import com.vektortelekom.android.vservice.ui.dialog.AppDialog
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideActivity
import com.vektortelekom.android.vservice.ui.home.adapter.DashboardAdapter
import com.vektortelekom.android.vservice.ui.home.adapter.MessageAdapter
import com.vektortelekom.android.vservice.ui.home.adapter.NotificationsAdapter
import com.vektortelekom.android.vservice.ui.home.adapter.UnusedFieldPhotosAdapter
import com.vektortelekom.android.vservice.ui.home.dialog.KvkkDialog
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.notification.NotificationActivity
import com.vektortelekom.android.vservice.ui.pastuses.PastUsesActivity
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarActivity
import com.vektortelekom.android.vservice.ui.poolcar.intercity.PoolCarIntercityActivity
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationActivity
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleActivity
import com.vektortelekom.android.vservice.ui.taxi.TaxiActivity
import com.vektortelekom.android.vservice.ui.vanpool.VanPoolDriverActivity
import com.vektortelekom.android.vservice.utils.*

import javax.inject.Inject

class HomeActivity : BaseActivity<HomeViewModel>(), HomeNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeViewModel

    private lateinit var binding: HomeActivityBinding

    var isNotificationExpanded: Boolean = false
    var isNotificationHide: Boolean = true
    var isMessageExpanded: Boolean = false
    var isMessageHide: Boolean = true

    private var prevPhotoUuid: String? = null

    private lateinit var bottomSheetBehaviorPoolCar: BottomSheetBehavior<*>

    private lateinit var bottomSheetUnusedFields: BottomSheetBehavior<*>

    private var notifications: MutableList<NotificationModel>? = null
    private var firstNotification: NotificationModel? = null

    private var notificationsAdapter: NotificationsAdapter? = null

    private var dashboardAdapter: DashboardAdapter? = null

    private lateinit var unusedFieldPhotosAdapter: UnusedFieldPhotosAdapter

    var unusedFieldPhotoList: List<String>? = null

    private val REQUEST_DRIVING_LICENSE = 776
    private val CARPOOL_PAGE_CODE = 1001
    private var kvkkDialog: KvkkDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<HomeActivityBinding>(this, R.layout.home_activity).apply {
            lifecycleOwner = this@HomeActivity
            viewModel = this@HomeActivity.viewModel
        }
        viewModel.navigator = this

        val isComingRegistration = intent.getBooleanExtra("is_coming_registration", false)

        val notification = intent.getStringExtra("notification")
        val subCategory = intent.getStringExtra("subCategory")

        if (notification != null && subCategory != null){
            if (subCategory == "CARPOOL_MATCHED")
            {
                showCarpoolNotificationDialog(notification)
            }
        }

        setGreetingText()

        viewModel.getVanpoolApprovalList()

        viewModel.approvalListItem.observe(this) {
            if (it != null) {
                val intent = Intent(this, VanPoolDriverActivity::class.java)
                intent.putExtra("workgroupInstanceId", it.workgroupInstanceId)
                intent.putExtra("versionedRouteId", it.versionedRouteId)
                intent.putExtra("approvalType", it.approvalType)
                intent.putExtra("approvalItemId", it.id)
                startActivity(intent)
            }
        }

        if(AppDataManager.instance.personnelInfo?.company?.kvkkDocUrl?.isBlank()?.not() == true
            && AppDataManager.instance.personnelInfo?.aggrementDate == null && AppDataManager.instance.isShowingKvkkDialog == false) {
            kvkkDialog = KvkkDialog(this, AppDataManager.instance.personnelInfo?.company?.kvkkDocUrl?:"") {
                viewModel.agreeKvkk()
            }
            kvkkDialog?.show()
        }
        else {
            val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

            if(homeLocation == null || homeLocation.latitude == 0.0) {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("is_address_not_valid", true)
                intent.putExtra("is_coming_registration", isComingRegistration)
                startActivity(intent)
            }
        }

        viewModel.agreeKvkkResponse.observe(this) {
            if (kvkkDialog != null && kvkkDialog?.isShowing == true) {
                kvkkDialog?.dismiss()
            }

            AppDataManager.instance.isShowingKvkkDialog = true
            kvkkDialog = null

            val homeLocation = AppDataManager.instance.personnelInfo?.homeLocation

            if (homeLocation == null || homeLocation.latitude == 0.0) {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("is_address_not_valid", true)
                intent.putExtra("is_coming_registration", isComingRegistration)
                startActivity(intent)
            }
        }

        viewModel.name.value = AppDataManager.instance.personnelInfo?.name?.plus(" ").plus(AppDataManager.instance.personnelInfo?.surname)

        viewModel.dashboardResponse.observe(this) { response ->
//            if (kvkkDialog == null)
                initViews(response)
        }
        setAnimations()

        bottomSheetBehaviorPoolCar = BottomSheetBehavior.from(binding.bottomSheetPoolCar)

        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetUnusedFields = BottomSheetBehavior.from(binding.bottomSheetUnusedFields)

        bottomSheetUnusedFields.state = BottomSheetBehavior.STATE_HIDDEN

        binding.buttonUnusedGotIt.setOnClickListener {
            bottomSheetUnusedFields.state = BottomSheetBehavior.STATE_HIDDEN
        }

        unusedFieldPhotosAdapter = UnusedFieldPhotosAdapter()

        binding.recyclerViewUnused.adapter = unusedFieldPhotosAdapter

        binding.recyclerViewUnused.addItemDecoration(object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)

                with(outRect) {
                    left = if (parent.getChildAdapterPosition(view) == 0) {
                        30f.dpToPx(this@HomeActivity)
                    } else {
                        15f.dpToPx(this@HomeActivity)
                    }
                    right = if (parent.getChildAdapterPosition(view) == (unusedFieldPhotoList?.size?:0) -1) {
                        30f.dpToPx(this@HomeActivity)
                    } else {
                        15f.dpToPx(this@HomeActivity)
                    }
                }
            }
        })

        viewModel.countPoolCarVehicle.observe(this) {
            dashboardAdapter?.setPoolCarVehicleCount(it ?: 0)
        }

        viewModel.sessionExpireError.observe(this) {
            stateManager.logout()
            AppDataManager.instance.logout()
            showLoginActivity()
        }
    }

    private fun showCarpoolNotificationDialog(message: String) {

        val dialog = AlertDialog.Builder(this, R.style.MaterialAlertDialogRounded).create()
        dialog.setCancelable(false)
        dialog.setMessage(message)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,resources.getString(R.string.view_now)) { d, _ ->
            showCarPoolActivity()
            d.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,resources.getString(R.string.later)) { d, _ ->
            d.dismiss()
        }

        dialog.show()
        dialog.withCenteredButtons()

    }

    private fun AlertDialog.withCenteredButtons() {
        val positive = getButton(AlertDialog.BUTTON_POSITIVE)
        val negative = getButton(AlertDialog.BUTTON_NEGATIVE)

        val parent = positive.parent as? LinearLayout
        parent?.gravity = Gravity.CENTER_HORIZONTAL


        val leftSpacer = parent?.getChildAt(1)
        leftSpacer?.visibility = View.GONE

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.weight = 1f
        layoutParams.gravity = Gravity.CENTER

        positive.layoutParams = layoutParams
        negative.layoutParams = layoutParams
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCarpool(getString(R.string.generic_language))

        viewModel.getPersonnelInfo()

        val currentPhotoUuid = AppDataManager.instance.personnelInfo?.profileImageUuid

        if(prevPhotoUuid != currentPhotoUuid) {

            val url: String = AppApiHelper().baseUrl
                    .plus("/")
                    .plus(BuildConfig.BASE_APP_NAME)
                    .plus("/doc/file/")
                    .plus(currentPhotoUuid)
                    .plus("/view/profile.jpeg?vektor-token=")
                    .plus(stateManager.vektorToken?:"")

            val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)

            Glide.with(this).setDefaultRequestOptions(requestOptions).load(url).into(binding.imageViewProfile)

            prevPhotoUuid = currentPhotoUuid

        }

    }

    private fun initViews(response: DashboardResponse) {

        response.response.notifications.let {
            if (it.isNotEmpty()){
                binding.textViewToolbarNotificationCount.visibility = View.VISIBLE
                if (it.count() > 99)
                    binding.textViewToolbarNotificationCount.text = "99+"
                else
                    binding.textViewToolbarNotificationCount.text = it.count().toString()

            } else{
                binding.textViewToolbarNotificationCount.visibility = View.GONE
            }
        }

        initMessages(response.response.messages)

        setFirstAnimationState()

        initDashboard(response.response.dashboard)
    }

    private fun initDashboard(dashboard: ArrayList<DashboardModel>) {

            for (item in dashboard) {
                if (item.type == DashboardItemType.PoolCar && item.userPermission) {
                    viewModel.getStations()
                }
            }

            dashboardAdapter = DashboardAdapter(dashboard, object: DashboardAdapter.DashboardItemListener {
                override fun itemClicked(model: DashboardModel) {
                    showDashboardItemPage(model)
                }

                override fun highlightCompleted() {
//                    HighlightView.Builder(this@HomeActivity, binding.buttonDotMenu, this@HomeActivity, "home_menu", "sequence_home_activity")
//                            .setHighlightText(getString(R.string.tutorial_menu))
//                            .create()
                }

            }, binding.nestedScrollView, viewModel.countPoolCarVehicle.value)

            binding.recyclerViewDashboard.adapter = dashboardAdapter

    }

    private fun showDashboardItemPage(model: DashboardModel) {

        when(model.type) {
            DashboardItemType.Shuttle -> {
                binding.cardViewIntercity.visibility = View.GONE
                showShuttleActivity()
            }
            DashboardItemType.PoolCar -> {
                if(viewModel.isPoolCarActive) {
                    if(model.userPermission) {
                        showPoolCarBottomSheet(model.isPoolCarReservationRequired)
                    }
                    else {
                        showPoolCarInactiveBottomSheet(model)
                    }
                }
                else {
                    showPoolCarInactiveBottomSheet(model)

                }
            }
            DashboardItemType.Taxi -> {

                binding.cardViewIntercity.visibility = View.GONE

                if(model.userPermission) {
                    showTaxiBottomSheet()
                }
                else {
                    val photos = mutableListOf<String>()
                    photos.add("taxi_1")
                    photos.add("taxi_2")
                    unusedFieldPhotoList = photos
                    unusedFieldPhotosAdapter.setPhotoList(photos)
                    binding.recyclerViewUnused.scrollToPosition(0)

                    binding.textViewUnusedSubtitle.text = getString(R.string.unused_taxi_subtitle)
                    binding.textViewUnusedDescription1.text = getString(R.string.unused_taxi_text_1)
                    binding.textViewUnusedDescription2.text = getString(R.string.unused_taxi_text_2)
                    binding.imageViewUnusedField.setImageResource(resources.getIdentifier("ic_dashboard_${model.iconName.fromCamelCaseToSnakeCase()}", "drawable", packageName))
                    binding.textViewUnusedTitle.text = model.title
                    binding.cardViewUnusedIcon.setCardBackgroundColor(Color.parseColor("#${model.tintColor}"))
                    bottomSheetUnusedFields.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
            DashboardItemType.FlexiRide -> {

                binding.cardViewIntercity.visibility = View.GONE
                showFlexirideBottomSheet(model)

            }
            DashboardItemType.Calendar -> {

                binding.cardViewIntercity.visibility = View.GONE

                if(model.userPermission) {
                    showCalendarActivity(null)
                }
                else {
                    val photos = mutableListOf<String>()
                    photos.add("calendar_1")
                    photos.add("calendar_2")
                    unusedFieldPhotoList = photos
                    unusedFieldPhotosAdapter.setPhotoList(photos)
                    binding.recyclerViewUnused.scrollToPosition(0)

                    binding.textViewUnusedSubtitle.text = getString(R.string.unused_calendar_subtitle)
                    binding.textViewUnusedDescription1.text = getString(R.string.unused_calendar_text_1)
                    binding.textViewUnusedDescription2.text = getString(R.string.unused_calendar_text_2)
                    binding.imageViewUnusedField.setImageResource(resources.getIdentifier("ic_dashboard_${model.iconName.fromCamelCaseToSnakeCase()}", "drawable", packageName))
                    binding.textViewUnusedTitle.text = model.title
                    binding.cardViewUnusedIcon.setCardBackgroundColor(Color.parseColor("#${model.tintColor}"))
                    bottomSheetUnusedFields.state = BottomSheetBehavior.STATE_EXPANDED
                }

            }
            DashboardItemType.ReportComplaints -> {
                binding.cardViewIntercity.visibility = View.GONE
                showCommentsActivity()
            }
            DashboardItemType.PastUses -> {

                binding.cardViewIntercity.visibility = View.GONE

                if(model.userPermission) {
                    showPastUsesActivity(null)
                }
                else {
                    val photos = mutableListOf<String>()
                    photos.add("pastuses_1")
                    photos.add("pastuses_2")
                    unusedFieldPhotoList = photos
                    unusedFieldPhotosAdapter.setPhotoList(photos)
                    binding.recyclerViewUnused.scrollToPosition(0)

                    binding.textViewUnusedSubtitle.text = getString(R.string.unused_past_uses_subtitle)
                    binding.textViewUnusedDescription1.text = getString(R.string.unused_past_uses_text_1)
                    binding.textViewUnusedDescription2.text = getString(R.string.unused_past_uses_text_2)
                    binding.imageViewUnusedField.setImageResource(resources.getIdentifier("ic_dashboard_${model.iconName.fromCamelCaseToSnakeCase()}", "drawable", packageName))
                    binding.textViewUnusedTitle.text = model.title
                    binding.cardViewUnusedIcon.setCardBackgroundColor(Color.parseColor("#${model.tintColor}"))
                    bottomSheetUnusedFields.state = BottomSheetBehavior.STATE_EXPANDED
                }

            }
            DashboardItemType.CarPool -> {
                binding.cardViewIntercity.visibility = View.GONE
                showCarPoolActivity()
            }
            DashboardItemType.ScanQR -> {
                binding.cardViewIntercity.visibility = View.GONE
                showScanQrCodeActivity()
            }
            DashboardItemType.MyQR -> {
                binding.cardViewIntercity.visibility = View.GONE
                showCarPoolQRCodeActivity()
            }
        }
    }

    private fun initNotifications(notifications: MutableList<NotificationModel>) {

        this.notifications = notifications

        if (notifications.isEmpty().not()) {
            isNotificationHide = false
            val firstNotification = notifications.removeAt(0)
            this.firstNotification = firstNotification
            initFirstNotification(firstNotification)

            notificationsAdapter = NotificationsAdapter(notifications, object: NotificationsAdapter.NotificationListener {
                override fun notificationClicked() {
                    startActivity(Intent(this@HomeActivity, NotificationActivity::class.java))
                }
            })
            binding.recyclerViewNotifications.adapter = notificationsAdapter
        }
    }

    fun addNotification(notification: NotificationModel) {

        if(firstNotification != null) {

            if(notifications.isNullOrEmpty()) {
                notifications = mutableListOf(firstNotification!!)
            }
            else {
                notifications?.add(0, firstNotification!!)
            }

        }
        else {

            notifications = mutableListOf()
        }

        firstNotification = notification

        initFirstNotification(firstNotification!!)


        if(notifications?.size == 3) {
            notifications?.removeAt(2)
        }

        notificationsAdapter?.updateNotifications(notifications!!)

        if(isNotificationHide) {
            isNotificationHide = false
            isNotificationExpanded = false

            when {
                isMessageHide -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_hide)
                }
                isMessageExpanded -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_end)
                }
                else -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_start)
                }
            }
        }

        /*if(isNotificationHide.not()) {
            if(firstNotification != null) {

                if(notifications.isNullOrEmpty()) {
                    notifications = mutableListOf(firstNotification!!)
                }
                else {
                    notifications?.add(0, firstNotification!!)
                }

            }
            else {

                isNotificationHide = false
                isNotificationExpanded = false

                when {
                    isMessageHide -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_hide)
                    }
                    isMessageExpanded -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_end)
                    }
                    else -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_start)
                    }
                }

                notifications = mutableListOf()
            }

            firstNotification = notification

            initFirstNotification(firstNotification!!)


            if(notifications?.size == 3) {
                notifications?.removeAt(2)
            }

            notificationsAdapter?.updateNotifications(notifications!!)

        }
        else if (firstNotification == null){
            firstNotification = notification
            initFirstNotification(firstNotification!!)

            isNotificationHide = false
            isNotificationExpanded = false

            when {
                isMessageHide -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_hide)
                }
                isMessageExpanded -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_end)
                }
                else -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_start)
                }
            }

            notifications = mutableListOf()

            notificationsAdapter = NotificationsAdapter(notifications!!)

            binding.recyclerViewNotifications.adapter = notificationsAdapter

        }*/
    }

    private fun initFirstNotification(notification: NotificationModel) {
        binding.includeViewNotification.textViewNotification.text = notification.message
        binding.includeViewNotification.textViewNotification.postDelayed({
        }, 300)
    }

    private fun initMessages(messages: MutableList<MessageModel>) {

        if(messages.isEmpty().not()) {
            isMessageHide = false
            val firstMessage = messages.removeAt(0)
            initFirstMessage(firstMessage)

            binding.recyclerViewMessages.adapter = MessageAdapter(messages)
        }
    }

    private fun initFirstMessage(message: MessageModel) {
        val sentBy = message.sentBy
        val messageText = message.message

        val firstIndex = (sentBy.length) +1
        val lastIndex = firstIndex + messageText.length + 1
        val spanText =  SpannableStringBuilder()
                .append(sentBy)
                .append(": ")
                .append(messageText)

        spanText.setSpan(TextAppearanceSpan(this@HomeActivity, R.style.TextMessageSender),0, firstIndex-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@HomeActivity, R.color.darkNavyBlue)), firstIndex, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


        binding.includeViewMessage.textViewMessage.text = spanText
    }

    private fun setFirstAnimationState() {
        if (isNotificationHide.not() && isMessageHide.not()) {
            binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_start)
        }
        else if (isNotificationHide && isMessageHide.not()) {
            binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_hide_message_start)
        }
        else if (isNotificationHide.not() && isMessageHide) {
            binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_hide)
        }
    }

    /*
        There are 9 state of views for animation.
        3 steps for Notifications (Expand, Collapse, Hide)
        3 steps for Messages (Expand, Collapse, Hide)
        3x3 = 9 animation states and their triggers defined here
     */
    private fun setAnimations() {
        binding.includeViewNotification.cardViewNotification.setOnClickListener {

            if(isNotificationExpanded) {
                startActivity(Intent(this@HomeActivity, NotificationActivity::class.java))
            }
            else {
                binding.includeViewNotification.motionLayoutNotificationButton.transitionToEnd()
                when {
                    isMessageHide -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_end_message_hide)
                    }
                    isMessageExpanded -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_end_message_end)
                    }
                    else -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_end_message_start)
                    }
                }
            }

        }

        binding.layoutNotificationHead.buttonNotificationShowLess.setOnClickListener {
            binding.includeViewNotification.motionLayoutNotificationButton.transitionToStart()
            when {
                isMessageHide -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_hide)
                }
                isMessageExpanded -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_end)
                }
                else -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_start)
                }
            }
        }

        binding.layoutNotificationHead.buttonNotificationHide.setOnClickListener {
            AppDataManager.instance.isShowNotification = false
            when {
                isMessageHide -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_hide_message_hide)
                }
                isMessageExpanded -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_hide_message_end)
                }
                else -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_hide_message_start)
                }
            }
        }

        binding.includeViewMessage.cardViewMessage.setOnClickListener {
            if(!isMessageExpanded) {
                binding.includeViewMessage.motionLayoutMessageButton.transitionToEnd()

                when {
                    isNotificationHide -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_hide_message_end)
                    }
                    isNotificationExpanded -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_end_message_end)
                    }
                    else -> {
                        binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_end)
                    }
                }
            }

        }

        binding.layoutMessageHead.buttonMessageShowLess.setOnClickListener {
            binding.includeViewMessage.motionLayoutMessageButton.transitionToStart()

            when {
                isNotificationHide -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_hide_message_start)
                }
                isNotificationExpanded -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_end_message_start)
                }
                else -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_start)
                }
            }
        }

        binding.layoutMessageHead.buttonMessageHide.setOnClickListener {
            when {
                isNotificationHide -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_hide_message_hide)
                }
                isNotificationExpanded -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_end_message_hide)
                }
                else -> {
                    binding.motionLayoutNotifications.transitionToState(R.id.constraint_set_notification_start_message_hide)
                }
            }
        }

        binding.motionLayoutNotifications.setTransitionListener(object: MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {

                when (binding.motionLayoutNotifications.currentState) {
                    R.id.constraint_set_notification_start_message_start -> {
                        isNotificationExpanded = false
                        isMessageExpanded = false
                        isNotificationHide = false
                        isMessageHide = false
                    }
                    R.id.constraint_set_notification_start_message_end -> {
                        isNotificationExpanded = false
                        isMessageExpanded = true
                        isNotificationHide = false
                        isMessageHide = false
                    }
                    R.id.constraint_set_notification_end_message_start -> {
                        isNotificationExpanded = true
                        isMessageExpanded = false
                        isNotificationHide = false
                        isMessageHide = false
                    }
                    R.id.constraint_set_notification_end_message_end -> {
                        isNotificationExpanded = true
                        isMessageExpanded = true
                        isNotificationHide = false
                        isMessageHide = false
                    }
                    R.id.constraint_set_notification_hide_message_start -> {
                        isNotificationExpanded = true
                        isMessageExpanded = false
                        isNotificationHide = true
                        isMessageHide = false
                    }
                    R.id.constraint_set_notification_hide_message_end -> {
                        isNotificationExpanded = true
                        isMessageExpanded = true
                        isNotificationHide = true
                        isMessageHide = false
                    }
                    R.id.constraint_set_notification_start_message_hide -> {
                        isNotificationExpanded = false
                        isMessageExpanded = true
                        isNotificationHide = false
                        isMessageHide = true
                    }
                    R.id.constraint_set_notification_end_message_hide -> {
                        isNotificationExpanded = true
                        isMessageExpanded = true
                        isNotificationHide = false
                        isMessageHide = true
                    }
                    R.id.constraint_set_notification_hide_message_hide -> {
                        isNotificationExpanded = true
                        isMessageExpanded = true
                        isNotificationHide = true
                        isMessageHide = true
                    }
                }
            }

        })
    }
    // End of Animation States and Their Functionality

    private fun showShuttleActivity() {
        val intent = Intent(this, ShuttleActivity::class.java)
        startActivity(intent)
    }

    private fun showCarPoolActivity() {
        val intent = Intent(this, CarPoolActivity::class.java)
        startActivityForResult(intent, CARPOOL_PAGE_CODE)
    }

    private fun showCarPoolQRCodeActivity() {
        val intent = Intent(this, CarPoolQrCodeActivity::class.java)
        startActivity(intent)
    }

    private fun showScanQrCodeActivity() {
        val intent = Intent(this, ScanQrCodeActivity::class.java)
        startActivity(intent)
    }

    private fun showCommentsActivity() {
        val intent = Intent(this, CommentsActivity::class.java)
        startActivity(intent)
    }

    override fun showPoolCarActivity(view: View?) {
        val intent = Intent(this, PoolCarActivity::class.java)
        startActivity(intent)
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showStartTaxiActivity(view: View?) {
        val intent = Intent(this, TaxiActivity::class.java)
        intent.putExtra("type", 1)
        startActivity(intent)
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showReportTaxiActivity(view: View?) {
        val intent = Intent(this, TaxiActivity::class.java)
        intent.putExtra("type", 2)
        startActivity(intent)
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showTaxiListFragment(view: View?) {
        val intent = Intent(this, TaxiActivity::class.java)
        intent.putExtra("type", 3)
        startActivity(intent)
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showPoolCarReservationsActivity(view: View?) {
        val intent = Intent(this, PoolCarReservationActivity::class.java)
        intent.putExtra("isAdd", false)
        startActivity(intent)
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showPoolCarAddReservationActivity(isIntercity: Boolean) {
        val intent = Intent(this, PoolCarReservationActivity::class.java)
        intent.putExtra("isAdd", true)
        intent.putExtra("isIntercity", isIntercity)
        startActivity(intent)
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showFlexirideActivity(type: Int) {
        val intent = Intent(this, FlexirideActivity::class.java)
        intent.putExtra("is_list", false)
        intent.putExtra("type", type)
        startActivity(intent)
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showFlexirideListActivity(view: View?) {
        val intent = Intent(this, FlexirideActivity::class.java)
        intent.putExtra("is_list", true)
        startActivity(intent)
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showCalendarActivity(view: View?) {
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
    }

    override fun showPastUsesActivity(view: View?) {
        val intent = Intent(this, PastUsesActivity::class.java)
        startActivity(intent)
    }

    private fun showPoolCarBottomSheet(isPoolCarReservationRequired: Boolean?) {

        if(viewModel.customerStatus.value?.flexirideRequestInfo == null) {
            binding.cardViewIntercity.visibility = View.GONE
        }
        else {
            binding.cardViewIntercity.visibility = View.VISIBLE

            binding.cardViewIntercity.setOnClickListener {
                viewModel.customerStatus.value?.flexirideRequestInfo?.let { rental ->
                    intercityRentalClicked(rental)
                }
            }
            binding.cardViewImageIntercity.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purpleyAlpha10))
            binding.imageViewIntercity.setImageResource(R.drawable.ic_calendar)
            binding.imageViewIntercity.setColorFilter(ContextCompat.getColor(this, R.color.purpley), PorterDuff.Mode.SRC_ATOP)
            binding.textViewIntercity.setTextColor(ContextCompat.getColor(this, R.color.purpley))
            binding.textViewIntercity.text = getString(R.string.active_rental)
            binding.textViewIntercityDescription.text = getString(R.string.active_rental_desc)

        }


        binding.cardViewRentACar.visibility = View.VISIBLE

        if(isPoolCarReservationRequired == true) {

            if(viewModel.customerStatus.value?.rental != null) {
                binding.cardViewIntercity.visibility = View.VISIBLE

                binding.cardViewIntercity.setOnClickListener {
                    showPoolCarActivity(null)
                }
                binding.cardViewImageIntercity.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purpleyAlpha10))
                binding.imageViewIntercity.setColorFilter(ContextCompat.getColor(this, R.color.purpley), PorterDuff.Mode.SRC_ATOP)
                binding.textViewIntercity.setTextColor(ContextCompat.getColor(this, R.color.purpley))
                binding.textViewIntercity.text = getString(R.string.continue_rent)
                binding.textViewIntercityDescription.text = getString(R.string.rent_a_car_description)
            }

            binding.textViewPoolCar.text = getString(R.string.pool_car)
            binding.textViewPoolCarDescription.text = getString(R.string.pool_car_description)

            /////////////////////////////////
            binding.cardViewRentACar.setOnClickListener {
                makeReservationClicked(false)
            }
            binding.cardViewImageRentCar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purpleyAlpha10))
            binding.imageViewRentCar.setImageResource(R.drawable.ic_calendar)
            binding.imageViewRentCar.setColorFilter(ContextCompat.getColor(this, R.color.purpley), PorterDuff.Mode.SRC_ATOP)
            binding.textViewRentCar.setTextColor(ContextCompat.getColor(this, R.color.purpley))
            binding.textViewRentCar.text = getString(R.string.make_reservation_local)
            binding.textViewRentCarDescription.text = getString(R.string.make_reservation_local_description)

            ////////////////////////////////////

            binding.cardViewMakeReservation.setOnClickListener {
                makeReservationClicked(true)
            }
            binding.cardViewImageMakeReservation.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purpleyAlpha10))
            binding.imageViewMakeReservation.setImageResource(R.drawable.ic_calendar)
            binding.imageViewMakeReservation.setColorFilter(ContextCompat.getColor(this, R.color.purpley), PorterDuff.Mode.SRC_ATOP)
            binding.textViewMakeReservation.setTextColor(ContextCompat.getColor(this, R.color.purpley))
            binding.textViewMakeReservation.text = getString(R.string.make_reservation_intercity)
            binding.textViewMakeReservationDescription.text = getString(R.string.make_reservation_intercity_description)

            //////////////////////
            binding.cardViewReservations.setOnClickListener {
                showPoolCarReservationsActivity(null)
            }

            binding.cardViewReservations.visibility = View.VISIBLE

            binding.cardViewImageReservations.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purpleyAlpha10))
            binding.imageViewReservations.setImageResource(R.drawable.ic_history)
            binding.imageViewReservations.setColorFilter(ContextCompat.getColor(this, R.color.purpley), PorterDuff.Mode.SRC_ATOP)
            binding.textViewReservations.setTextColor(ContextCompat.getColor(this, R.color.purpley))
            binding.textViewReservations.text = getString(R.string.reservations)
            binding.textViewReservationsDescription.text = getString(R.string.reservations_description)

            bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_EXPANDED
        }
        else {
            binding.textViewPoolCar.text = getString(R.string.pool_car)
            binding.textViewPoolCarDescription.text = getString(R.string.pool_car_description)
            binding.cardViewRentACar.setOnClickListener {
                showPoolCarActivity(null)
            }
            binding.cardViewImageRentCar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purpleyAlpha10))
            binding.imageViewRentCar.setColorFilter(ContextCompat.getColor(this, R.color.purpley), PorterDuff.Mode.SRC_ATOP)
            binding.textViewRentCar.setTextColor(ContextCompat.getColor(this, R.color.purpley))
            binding.textViewRentCar.text = if(viewModel.customerStatus.value?.rental == null) getString(R.string.rent_a_car) else getString(R.string.continue_rent)
            binding.textViewRentCarDescription.text = getString(R.string.rent_a_car_description)

            binding.cardViewMakeReservation.setOnClickListener {
                makeReservationClicked(false)
            }
            binding.cardViewImageMakeReservation.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purpleyAlpha10))
            binding.imageViewMakeReservation.setImageResource(R.drawable.ic_calendar)
            binding.imageViewMakeReservation.setColorFilter(ContextCompat.getColor(this, R.color.purpley), PorterDuff.Mode.SRC_ATOP)
            binding.textViewMakeReservation.setTextColor(ContextCompat.getColor(this, R.color.purpley))
            binding.textViewMakeReservation.text = getString(R.string.make_reservation)
            binding.textViewMakeReservationDescription.text = getString(R.string.make_reservation_description)

            binding.cardViewReservations.setOnClickListener {
                showPoolCarReservationsActivity(null)
            }

            binding.cardViewReservations.visibility = View.VISIBLE

            binding.cardViewImageReservations.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purpleyAlpha10))
            binding.imageViewReservations.setImageResource(R.drawable.ic_history)
            binding.imageViewReservations.setColorFilter(ContextCompat.getColor(this, R.color.purpley), PorterDuff.Mode.SRC_ATOP)
            binding.textViewReservations.setTextColor(ContextCompat.getColor(this, R.color.purpley))
            binding.textViewReservations.text = getString(R.string.reservations)
            binding.textViewReservationsDescription.text = getString(R.string.reservations_description)

            bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun showTaxiBottomSheet() {
        binding.textViewPoolCar.text = getString(R.string.taxi_invoicing)
        binding.textViewPoolCarDescription.text = getString(R.string.taxi_invoicing_description)

        binding.cardViewRentACar.visibility = View.VISIBLE

        binding.cardViewRentACar.setOnClickListener {
            showStartTaxiActivity(null)
        }
        binding.cardViewImageRentCar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.marigoldAlpha10))
        binding.imageViewRentCar.setColorFilter(ContextCompat.getColor(this, R.color.marigold), PorterDuff.Mode.SRC_ATOP)
        binding.textViewRentCar.setTextColor(ContextCompat.getColor(this, R.color.marigold))

        if(viewModel.taxiUsage.value == null) {
            binding.textViewRentCar.text = getString(R.string.start_taxi_use)
            binding.textViewRentCarDescription.text = getString(R.string.start_taxi_use_description)
        }
        else {
            binding.textViewRentCar.text = getString(R.string.finish_taxi_use)
            binding.textViewRentCarDescription.text = getString(R.string.finish_taxi_use_description)
        }

        binding.cardViewMakeReservation.setOnClickListener {
            showReportTaxiActivity(null)
        }
        binding.cardViewImageMakeReservation.setCardBackgroundColor(ContextCompat.getColor(this, R.color.marigoldAlpha10))
        binding.imageViewMakeReservation.setImageResource(R.drawable.ic_attention)
        binding.imageViewMakeReservation.setColorFilter(ContextCompat.getColor(this, R.color.marigold), PorterDuff.Mode.SRC_ATOP)
        binding.textViewMakeReservation.setTextColor(ContextCompat.getColor(this, R.color.marigold))
        binding.textViewMakeReservation.text = getString(R.string.report_taxi_use)
        binding.textViewMakeReservationDescription.text = getString(R.string.report_taxi_use_description)


        binding.cardViewReservations.visibility = View.GONE

        /*binding.cardViewReservations.setOnClickListener {
            showTaxiListFragment(null)
        }
        binding.cardViewImageReservations.setCardBackgroundColor(ContextCompat.getColor(this, R.color.marigoldAlpha10))
        binding.imageViewReservations.setImageResource(R.drawable.ic_history)
        binding.imageViewReservations.setColorFilter(ContextCompat.getColor(this, R.color.marigold), PorterDuff.Mode.SRC_ATOP)
        binding.textViewReservations.setTextColor(ContextCompat.getColor(this, R.color.marigold))
        binding.textViewReservations.text = "Taxi List"//getString(R.string.reservations)
        binding.textViewReservationsDescription.text = "Taxi List Description"//getString(R.string.reservations_description)*/

        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showFlexirideBottomSheet(model: DashboardModel) {

        if(model.userPermission) {
            binding.textViewPoolCar.text = getString(R.string.flexiride)
            binding.textViewPoolCarDescription.text = getString(R.string.flexiride_description)

            binding.cardViewRentACar.visibility = View.VISIBLE

            binding.cardViewRentACar.setOnClickListener {
                showFlexirideActivity(0)
            }
            binding.cardViewImageRentCar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorOrangeAlpha10))
            binding.imageViewRentCar.setColorFilter(ContextCompat.getColor(this, R.color.colorOrange), PorterDuff.Mode.SRC_ATOP)
            binding.textViewRentCar.setTextColor(ContextCompat.getColor(this, R.color.colorOrange))

            binding.textViewRentCar.text = getString(R.string.flexiride_create)
            binding.textViewRentCarDescription.text = getString(R.string.flexiride_create_desc)

            binding.cardViewMakeReservation.visibility = View.VISIBLE

            binding.cardViewMakeReservation.setOnClickListener {
                showFlexirideActivity(1)
            }
            binding.cardViewImageMakeReservation.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorOrangeAlpha10))
            binding.imageViewMakeReservation.setImageResource(R.drawable.ic_attention)
            binding.imageViewMakeReservation.setColorFilter(ContextCompat.getColor(this, R.color.colorOrange), PorterDuff.Mode.SRC_ATOP)
            binding.textViewMakeReservation.setTextColor(ContextCompat.getColor(this, R.color.colorOrange))
            binding.textViewMakeReservation.text = getString(R.string.flexiride_guest_create)
            binding.textViewMakeReservationDescription.text = getString(R.string.flexiride_guest_create_desc)


            binding.cardViewReservations.visibility = View.VISIBLE

            binding.cardViewReservations.setOnClickListener {
                showFlexirideListActivity(null)
            }
            binding.cardViewImageReservations.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorOrangeAlpha10))
            binding.imageViewReservations.setImageResource(R.drawable.ic_attention)
            binding.imageViewReservations.setColorFilter(ContextCompat.getColor(this, R.color.colorOrange), PorterDuff.Mode.SRC_ATOP)
            binding.textViewReservations.setTextColor(ContextCompat.getColor(this, R.color.colorOrange))
            binding.textViewReservations.text = getString(R.string.flexiride_list)
            binding.textViewReservationsDescription.text = getString(R.string.flexiride_list_description)

            bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_EXPANDED

        }
        else {
            val photos = mutableListOf<String>()
            photos.add("flexiride_1")
            photos.add("flexiride_2")
            photos.add("flexiride_3")
            unusedFieldPhotoList = photos
            unusedFieldPhotosAdapter.setPhotoList(photos)
            binding.recyclerViewUnused.scrollToPosition(0)

            binding.textViewUnusedSubtitle.text = getString(R.string.unused_flexiride_subtitle)
            binding.textViewUnusedDescription1.text = getString(R.string.unused_flexiride_text_1)
            binding.textViewUnusedDescription2.text = getString(R.string.unused_flexiride_text_2)
            binding.imageViewUnusedField.setImageResource(resources.getIdentifier("ic_dashboard_${model.iconName.fromCamelCaseToSnakeCase()}", "drawable", packageName))
            binding.textViewUnusedTitle.text = model.title
            binding.cardViewUnusedIcon.setCardBackgroundColor(Color.parseColor("#${model.tintColor}"))
            bottomSheetUnusedFields.state = BottomSheetBehavior.STATE_EXPANDED
        }

    }

    override fun getViewModel(): HomeViewModel {
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        return viewModel
    }

    override fun showMenuActivity(view: View?) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("is_pool_car_active", viewModel.isPoolCarActive)
        startActivityForResult(intent, REQUEST_DRIVING_LICENSE)
    }

    private fun setGreetingText() {

        when (System.currentTimeMillis().getHourOfTimestamp()) {
            in 6..10 -> {
                binding.textViewGoodMorning.text = getString(R.string.good_morning)
            }
            in 15..19 -> {
                binding.textViewGoodMorning.text = getString(R.string.good_evening)
            }
            in 20..22 -> {
                binding.textViewGoodMorning.text = getString(R.string.good_night)
            }
            else -> {
                binding.textViewGoodMorning.text = getString(R.string.greetings)
            }
        }

    }

    private fun showPoolCarInactiveBottomSheet(model: DashboardModel) {
        val photos = mutableListOf<String>()
        photos.add("poolcar_1")
        photos.add("poolcar_2")
        unusedFieldPhotoList = photos
        unusedFieldPhotosAdapter.setPhotoList(photos)
        binding.recyclerViewUnused.scrollToPosition(0)

        binding.textViewUnusedSubtitle.text = getString(R.string.unused_pool_car_subtitle)
        binding.textViewUnusedDescription1.text = getString(R.string.unused_pool_car_text_1)
        binding.textViewUnusedDescription2.text = getString(R.string.unused_pool_car_text_2)
        binding.imageViewUnusedField.setImageResource(resources.getIdentifier("ic_dashboard_${model.iconName.fromCamelCaseToSnakeCase()}", "drawable", packageName))
        binding.textViewUnusedTitle.text = model.title
        binding.cardViewUnusedIcon.setCardBackgroundColor(Color.parseColor("#${model.tintColor}"))
        bottomSheetUnusedFields.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun makeReservationClicked(isIntercity: Boolean) {
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
        if(viewModel.customerStatus.value?.user?.canStartRental == true) {

            showPoolCarAddReservationActivity(isIntercity)

        }
        else {

            when(viewModel.customerStatus.value?.userDocumentInfo?.drivingLicenseStatus) {
                DrivingLicenseStatus.PENDING_APPROVAL -> {
                    val dialog = AppDialog.Builder(this@HomeActivity)
                            .setIconVisibility(false)
                            .setTitle("Ehliyet bilgileri onaylanma sürecinde.")
                            .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                                dialog.dismiss()
                                viewModel.getCustomerStatus()
                            }
                            .create()

                    dialog.show()
                }
                DrivingLicenseStatus.MISSING -> {
                    val dialog = AppDialog.Builder(this@HomeActivity)
                            .setIconVisibility(false)
                            .setTitle("Ehliyet bilgilerini yüklemeniz gerekiyor")
                            .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                                val intent = Intent(this, MenuActivity::class.java)
                                intent.putExtra("is_for_driving_license", true)
                                startActivityForResult(intent, REQUEST_DRIVING_LICENSE)
                                dialog.dismiss()
                            }
                            .create()

                    dialog.show()
                }
                DrivingLicenseStatus.REJECTED -> {
                    val dialog = AppDialog.Builder(this@HomeActivity)
                            .setIconVisibility(false)
                            .setTitle(viewModel.customerStatus.value?.userDocumentInfo?.drivingLicenseRejectReason?:"Ehliyet ${2} yıldan eski olmalı.")
                            .setOkButton(resources.getString(R.string.Generic_Ok)) { dialog ->
                                dialog.dismiss()
                            }
                            .create()

                    dialog.show()
                }
            }

        }
    }

    private fun intercityRentalClicked(request: PoolcarAndFlexirideModel) {
        bottomSheetBehaviorPoolCar.state = BottomSheetBehavior.STATE_HIDDEN
        val intent = Intent(this, PoolCarIntercityActivity::class.java)
        intent.putExtra("rental", request)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_DRIVING_LICENSE && resultCode == Activity.RESULT_OK) {
            viewModel.getCustomerStatus()
        } else if (requestCode == CARPOOL_PAGE_CODE && resultCode == Activity.RESULT_OK){
            viewModel.getCarpool(getString(R.string.generic_language))
        }

    }


}
