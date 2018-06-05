package com.example.hendratay.whatheweather.presentation.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Log
import com.example.hendratay.whatheweather.domain.interactor.DefaultObserver
import com.example.hendratay.whatheweather.domain.interactor.GetCurrentWeather
import com.example.hendratay.whatheweather.domain.model.CurrentWeather
import com.example.hendratay.whatheweather.presentation.data.Resource
import com.example.hendratay.whatheweather.presentation.data.ResourceState
import com.example.hendratay.whatheweather.presentation.model.CurrentWeatherView
import com.example.hendratay.whatheweather.presentation.model.mapper.CurrentWeatherViewMapper
import javax.inject.Inject

class CurrentWeatherViewModel @Inject constructor(val getCurrentWeather: GetCurrentWeather,
                                                  val currentWeatherViewMapper: CurrentWeatherViewMapper):
        ViewModel() {

    private val weatherLiveData: MutableLiveData<Resource<CurrentWeatherView>> = MutableLiveData()
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCleared() {
        getCurrentWeather.dispose()
        super.onCleared()
    }

    fun getCurrentWeather() = weatherLiveData

    fun setLatLng(lat: Double?, lng: Double?) {
        latitude = lat
        longitude =  lng
        if(latitude == null && longitude == null) {
            weatherLiveData.postValue(Resource(ResourceState.ERROR, null, "No Location Provided"))
        } else {
            fetchCurrentWeather()
        }
    }

    fun fetchCurrentWeather() {
        weatherLiveData.postValue(Resource(ResourceState.LOADING, null, null))
        getCurrentWeather.execute(CurrentWeatherObserver(), GetCurrentWeather.Params.forLocation(latitude!!, longitude!!))
    }

    inner class CurrentWeatherObserver: DefaultObserver<CurrentWeather>() {

        override fun onComplete() {

        }

        override fun onNext(t: CurrentWeather) {
            weatherLiveData.postValue(Resource(ResourceState.SUCCESS, currentWeatherViewMapper.mapToView(t), null))
            Log.d("Current Weather", t.toString())
        }

        override fun onError(e: Throwable) {
            weatherLiveData.postValue(Resource(ResourceState.ERROR, null, e.message))
            Log.d("Current Weather", e.toString())
        }

    }

}

open class CurrentWeatherViewModelFactory(
        private val getCurrentWeather: GetCurrentWeather,
        private val currentWeatherViewMapper: CurrentWeatherViewMapper): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)) {
            return CurrentWeatherViewModel(getCurrentWeather, currentWeatherViewMapper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}