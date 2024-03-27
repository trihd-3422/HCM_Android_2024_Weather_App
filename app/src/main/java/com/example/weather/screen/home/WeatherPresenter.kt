package com.example.weather.screen.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.weather.data.model.Weather
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.screen.RequestCompleteListener
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class WeatherPresenter(
    private val repository: WeatherRepository
) : WeatherContract.Presenter {
    private var view: WeatherContract.View? = null

    private val mExecutor: Executor = Executors.newCachedThreadPool()
    private val mHandler = Handler(Looper.getMainLooper())
    private var current: Weather? = null
    private var hourly: Weather? = null
    private var daily: Weather? = null


    override fun setView(view: WeatherContract.View?) {
        this.view = view
    }

    override fun onStart() {}

    override fun getWeather(latitude: Double, longitude: Double) {
        Log.v("myTag", "getWeather")
        view?.onProgressLoading(true)
        mExecutor.execute {
            repository.fetchWeatherForecastCurrent(
                latitude,
                longitude,
                object : RequestCompleteListener<Weather> {
                    override fun onRequestSuccess(data: Weather) {
                        current = data
                        insertWeatherIfDataAvailable(current, hourly, daily)
                    }

                    override fun onRequestFailed(e: Exception?) {
                        view?.onProgressLoading(false)
                        e?.let { view?.onError(e) }
                    }
                }
            )
            repository.fetchWeatherForecastHourly(
                latitude,
                longitude,
                object : RequestCompleteListener<Weather> {
                    override fun onRequestSuccess(data: Weather) {
                        hourly = data
                        insertWeatherIfDataAvailable(current, hourly, daily)
                    }

                    override fun onRequestFailed(e: Exception?) {
                        view?.onProgressLoading(false)
                        e?.let { view?.onError(e) }
                    }
                }
            )
            repository.fetchWeatherForecastDaily(
                latitude,
                longitude,
                object : RequestCompleteListener<Weather> {
                    override fun onRequestSuccess(data: Weather) {
                        daily = data
                        insertWeatherIfDataAvailable(current, hourly, daily)
                    }

                    override fun onRequestFailed(e: Exception?) {
                        view?.onProgressLoading(false)
                        e?.let { view?.onError(e) }
                    }
                }
            )

        }
    }

    fun insertWeatherIfDataAvailable(current: Weather?, hourly: Weather?, daily: Weather?) {
        if (current != null && hourly != null && daily != null) {
            mHandler.post {
                view?.onProgressLoading(false)
                view?.onGetCurrentWeatherSuccess(current)
                Log.v("myTag1", "Save data success")
                repository.insertWeather(current, hourly, daily)
            }
        }
    }

    override fun getWeatherLocal() {
        Log.v("myTag", "getWeatherLocal")
//        repository.getAllLocalWeathers().let {
//            Log.v("myTag", "$it")
//            if (it.isNotEmpty()) {
//                Log.v("myTag", "getWeatherLocal Inside")
//                view?.onGetCurrentWeatherSuccess(it[0])
//            }
//        }
        val a: Weather? = repository.getLocalWeather("Ho Chi Minh CityVN")
        Log.v("myTag", "$a")
        if (a != null) {
            view?.onGetCurrentWeatherSuccess(a)
        }
    }

    override fun onStop() {}
}

