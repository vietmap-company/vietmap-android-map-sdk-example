package vn.vietmap.mapsdkdemo.view

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerTabStrip
import androidx.viewpager.widget.ViewPager
import vn.vietmap.mapsdkdemo.R

class MapViewPager(context: Context, attrs: AttributeSet) : ViewPager(context!!, attrs) {
    override fun canScroll(v: View?, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        return v is SurfaceView || v is PagerTabStrip|| super.canScroll(v, checkV, dx, x, y)
    }

}