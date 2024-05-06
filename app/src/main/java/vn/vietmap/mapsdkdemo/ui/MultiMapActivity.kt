package vn.vietmap.mapsdkdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.SupportMapFragment

class MultiMapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_map)
        val fragmentManager = supportFragmentManager

        initFragment(fragmentManager, R.id.map1, VietMapTiles.instance.lightRaster())
        initFragment(fragmentManager, R.id.map2, VietMapTiles.instance.lightVector())
        initFragment(fragmentManager, R.id.map3, VietMapTiles.instance.lightVector())
        initFragment(fragmentManager, R.id.map4, VietMapTiles.instance.lightRaster())

    }
    private fun initFragment(fragmentManager: FragmentManager, fragmentId:Int,  style: String){
        (fragmentManager.findFragmentById(fragmentId) as SupportMapFragment)?.getMapAsync{
            it.setStyle(style)
        }
    }
}