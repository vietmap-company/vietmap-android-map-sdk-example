package vn.vietmap.mapsdkdemo.utils

import android.content.res.Resources
import vn.vietmap.mapsdkdemo.R

class VietMapTiles {
    companion object {
        val instance = VietMapTiles()
    }

    private fun getApiKey(): String {
        return (Resources.getSystem().getString(R.string.VietMapAPIKey))
    }

    fun lightVector():String{
        return "https://maps.vietmap.vn/api/maps/light/styles.json?apikey=YOUR_API_KEY_HERE"
    }

    fun  lightRaster():String{
        return "https://maps.vietmap.vn/api/maps/raster/styles.json?apikey=YOUR_API_KEY_HERE"
    }
    fun  google():String{
        return "https://maps.vietmap.vn/api/maps/google/styles.json?apikey=YOUR_API_KEY_HERE"
    }
    fun  googleSatellite():String{
        return "https://maps.vietmap.vn/api/maps/google-satellite/styles.json?apikey=YOUR_API_KEY_HERE"
    }
}