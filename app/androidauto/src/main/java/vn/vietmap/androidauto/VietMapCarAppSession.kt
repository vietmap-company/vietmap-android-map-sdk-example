package vn.vietmap.androidauto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import vn.vietmap.vietmapandroidautosdk.map.VietMapAndroidAutoSurface
import vn.vietmap.vietmapsdk.Vietmap

class VietMapCarAppSession: Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        Vietmap.getInstance(carContext)
        val mNavigationCarSurface = VietMapAndroidAutoSurface(carContext, lifecycle)
        val screenMap = VietMapCarAppScreen(carContext,mNavigationCarSurface)
        return screenMap
    }
}