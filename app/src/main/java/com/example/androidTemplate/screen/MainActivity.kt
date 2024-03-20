package com.example.androidTemplate.screen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.androidTemplate.data.model.City
import com.example.androidTemplate.data.model.CurrentWeatherData
import com.example.androidTemplate.data.repository.WeatherRepository
import com.example.androidTemplate.data.repository.source.local.WeatherLocalDataSource
import com.example.androidTemplate.data.repository.source.remote.WeatherRemoteDataSource
import com.example.androidTemplate.databinding.ActivityMainBinding
import com.example.androidTemplate.screen.presenter.WeatherInfoShowPresenter
import com.example.androidTemplate.screen.presenter.WeatherInfoShowPresenterImpl
import com.example.androidTemplate.utils.convertToListOfCityName

class MainActivity : AppCompatActivity(), MainActivityView {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var model = WeatherRepository
    private lateinit var presenter: WeatherInfoShowPresenter

    private var cityList: MutableList<City> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // initialize model and presenter
        presenter = WeatherInfoShowPresenterImpl(
            this,
            model.getInstance(
                WeatherRemoteDataSource.getInstance(),
                WeatherLocalDataSource.getInstance()
            )
        )

        // call for fetching city list
        presenter.fetchCityList()


        binding.layoutHeader.btnViewWeather.setOnClickListener {
            binding.outputGroup.visibility = View.GONE

            val spinnerSelectedItemPos = binding.layoutHeader.spinner.selectedItemPosition

            // fetch weather info of specific city
            presenter.fetchWeatherInfo(cityList[spinnerSelectedItemPos].id)
        }
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    /**
     * Activity doesn't know when should progress bar visible or hide. It only knows
     * how to show/hide it.
     * Presenter will decide the logic of progress bar visibility.
     * This method will be triggered by presenter when needed.
     */
    override fun handleProgressBarVisibility(visibility: Int) {
        binding.progressBar.visibility = visibility
    }

    /**
     * This method will be triggered when city list successfully fetched.
     * From where this list will be come? From local db or network call or from somewhere else?
     * Activity/View doesn't know and doesn't care anything about it. Activity only knows how to
     * show the city list on the UI and listen the click event of the Spinner.
     * Model knows about the data source of city list.
     */
    override fun onCityListFetchSuccess(cityList: MutableList<City>) {
        this.cityList = cityList

        val arrayAdapter = ArrayAdapter(
            this,
            androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
            cityList.convertToListOfCityName()
        )

        binding.layoutHeader.spinner.adapter = arrayAdapter
    }

    /**
     * This method will triggered if city list fetching process failed
     */
    override fun onCityListFetchFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    /**
     * This method will triggered when weather information successfully fetched.
     * Activity/View doesn't know anything about the data source of weather API.
     * Only model knows about the data source of weather API.
     */
    override fun onWeatherInfoFetchSuccess(currentWeather: CurrentWeatherData) {
        binding.outputGroup.visibility = View.VISIBLE
        binding.tvErrorMessage.visibility = View.GONE

        binding.layoutWeatherBasic.tvDateTime.text = "Today, ${currentWeather.dateTime}"
        binding.layoutWeatherBasic.tvTemperature.text = currentWeather.temperature
        //Glide.with(this).load(currentWeatherModel.weatherConditionIconUrl).into(binding.icWeather)
        binding.layoutWeatherBasic.tvMainCondition.text = currentWeather.weatherMainCondition
        binding.layoutWeatherBasic.tvWindValue.text = "${currentWeather.windSpeed} km/h"
        binding.layoutWeatherBasic.tvHumidityValue.text = currentWeather.humidity

        binding.layoutWeatherBasic.tvWind.text = "Wind"
        binding.layoutWeatherBasic.tvHumidity.text = "Hum"
        binding.layoutWeatherBasic.guideline.visibility = View.VISIBLE

    }

    /**
     * This method will triggered if weather information fetching process failed
     */
    override fun onWeatherInfoFetchFailure(errorMessage: String) {
        binding.outputGroup.visibility = View.GONE
        binding.tvErrorMessage.visibility = View.VISIBLE
        binding.tvErrorMessage.text = errorMessage
    }
}




