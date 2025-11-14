package vn.vietmap.mapsdkdemo.utils

import android.content.Context
import vn.vietmap.mapsdkdemo.R

class VietMapTiles {
    companion object {
        val instance = VietMapTiles()
    }

    private fun getApiKey(context: Context?): String? {
        if (context == null) return null
        return ApiKeyProvider.getApiKey(context)
    }

    fun lightVector(context: Context? = null):String{
        val key = getApiKey(context) ?: ""
        return "https://maps.vietmap.vn/api/maps/light/styles.json?apikey=$key"
    }

    fun  lightRaster(context: Context? = null):String{
        val key = getApiKey(context) ?: ""
        return "https://maps.vietmap.vn/api/maps/raster/styles.json?apikey=$key"
    }
    fun  google(context: Context? = null):String{
        val key = getApiKey(context) ?: ""
        return "https://maps.vietmap.vn/api/maps/google/styles.json?apikey=$key"
    }
    fun  googleSatellite(context: Context? = null):String{
        val key = getApiKey(context) ?: ""
        return "https://maps.vietmap.vn/api/maps/google-satellite/styles.json?apikey=$key"
    }
}