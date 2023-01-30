package com.vektortelekom.android.vservice.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.vektor.ktx.data.remote.model.BaseErrorModel
import com.vektor.ktx.utils.logger.AppLogger
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.lang.ref.WeakReference

abstract class BaseViewModel<N> : ViewModel() {

    open val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    open val compositeDisposable = CompositeDisposable()

    private var mNavigator: WeakReference<N>? = null

    val sessionExpireError: MutableLiveData<Boolean> = MutableLiveData()

    var navigator: N?
        get() = mNavigator?.get()
        set(navigator) {
            this.mNavigator = if (navigator != null) WeakReference(navigator) else null
        }

    fun setIsLoading(isLoading: Boolean) {
        this.isLoading.value = isLoading
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun getErrorIdFromHTTPException(error: Throwable) : Int? {
        when (error) {
            is HttpException -> {
                var baseErrorModel: BaseErrorModel? = null
                try {
                    val responseBody = error.response()!!.errorBody()
                    val gson = Gson()
                    baseErrorModel = gson.fromJson(responseBody!!.string(), BaseErrorModel::class.java)
                    return  baseErrorModel.error?.errorId
                } catch (t: Throwable) {
                    AppLogger.e(t, "API Response Parse Error")
                }

            }
        }
        return  null
    }
}
