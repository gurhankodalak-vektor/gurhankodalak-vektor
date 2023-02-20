package com.vektortelekom.android.vservice.ui.notification

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.local.AppDataManager
import com.vektortelekom.android.vservice.databinding.NotificationActivityBinding
import com.vektortelekom.android.vservice.ui.base.BaseActivity
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.notification.adapters.NotificationsAdapter
import com.vektortelekom.android.vservice.utils.dpToPx
import javax.inject.Inject

class NotificationActivity: BaseActivity<NotificationViewModel>(), NotificationNavigator {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: NotificationViewModel

    private lateinit var binding: NotificationActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<NotificationActivityBinding>(this, R.layout.notification_activity).apply {
            lifecycleOwner = this@NotificationActivity
            viewModel = this@NotificationActivity.viewModel
        }
        viewModel.navigator = this

        viewModel.getNotifications()

        viewModel.notifications.observe(this) { notifications ->
            notifications?.let {
                AppDataManager.instance.unReadNotificationCount = 0
                binding.recyclerViewNotifications.adapter = NotificationsAdapter(it)
                binding.recyclerViewNotifications.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        super.getItemOffsets(outRect, view, parent, state)

                        with(outRect) {
                            top = if (parent.getChildAdapterPosition(view) == 0) {
                                10f.dpToPx(this@NotificationActivity)
                            } else {
                                5f.dpToPx(this@NotificationActivity)
                            }
                            bottom = if (parent.getChildAdapterPosition(view) == notifications.size - 1) {
                                10f.dpToPx(this@NotificationActivity)
                            } else {
                                5f.dpToPx(this@NotificationActivity)
                            }
                        }
                    }
                })
            }
        }

    }

    override fun getViewModel(): NotificationViewModel {
        viewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
        return viewModel
    }

    override fun backPressed(view: View?) {
        onBackPressed()
    }

    override fun showMenuActivity(view: View?) {
        startActivity(Intent(this, MenuActivity::class.java))
    }

}