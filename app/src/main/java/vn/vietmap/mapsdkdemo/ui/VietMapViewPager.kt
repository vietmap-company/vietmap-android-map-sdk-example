package vn.vietmap.mapsdkdemo.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager.widget.ViewPager
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.databinding.ActivityMapViewPagerBinding
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.SupportMapFragment
import vn.vietmap.vietmapsdk.maps.VietMapGLOptions

class VietMapViewPager : AppCompatActivity() {
    private lateinit var binding: ActivityMapViewPagerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter =
            ItemAdapter(this, LayoutInflater.from(this), supportFragmentManager)
    }

    class ItemAdapter(
        private val context: Context,
        private val inflater: LayoutInflater,
        private val fragmentManager: FragmentManager
    ) : RecyclerView.Adapter<ViewHolder>() {
        private val items = listOf(
            "one",
            "two",
            "three",
            ViewPagerItem(),
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "ten",
            "eleven",
            "twelve",
            "thirteen",
            "fourteen",
            "fifteen",
            "sixteen",
            "seventeen",
            "eighteen",
            "nineteen",
            "twenty",
            "twenty-one"
        )

        private var mapHolder: ViewPagerHolder? = null

        companion object {
            const val TYPE_VIEWPAGER = 0
            const val TYPE_TEXT = 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return if (viewType == TYPE_VIEWPAGER) {
                val viewPager =
                    inflater.inflate(R.layout.item_viewpager, parent, false) as ViewPager
                mapHolder = ViewPagerHolder(context, viewPager, fragmentManager)
                return mapHolder as ViewPagerHolder
            } else {
                TextHolder(
                    inflater.inflate(
                        android.R.layout.simple_list_item_1,
                        parent,
                        false
                    ) as TextView
                )
            }
        }

        override fun getItemCount(): Int {
            return items.count()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.itemViewType == TYPE_TEXT) {
                val textHolder = holder as TextHolder
                textHolder.bind(items[position] as String)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (items[position] is ViewPagerItem) {
                TYPE_VIEWPAGER
            } else {
                TYPE_TEXT
            }
        }

        class TextHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
            fun bind(item: String) {
                textView.text = item
            }
        }

        class ViewPagerItem
        class ViewPagerHolder(
            context: Context,
            private val viewPager: ViewPager,
            fragmentManager: FragmentManager
        ) : RecyclerView.ViewHolder(viewPager) {
            init {
                viewPager.adapter = MapPagerAdapter(context, fragmentManager)
                viewPager.setOnTouchListener { view, motionEvent ->
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    viewPager.onTouchEvent(motionEvent)
                    false
                }
            }
        }

        class MapPagerAdapter(private val context: Context, fragmentManager: FragmentManager) :
            FragmentStatePagerAdapter(fragmentManager) {
            override fun getItem(position: Int): Fragment {
                val options = VietMapGLOptions.createFromAttributes(context)
                options.textureMode(true)
                options.doubleTapGesturesEnabled(false)
                options.rotateGesturesEnabled(false)
                options.tiltGesturesEnabled(false)
                options.scrollGesturesEnabled(false)
                options.zoomGesturesEnabled(false)
                when (position) {
                    0 -> {
                        options.camera(
                            CameraPosition.Builder().target(LatLng(10.57885, 106.489596)).zoom(11.0)
                                .build()
                        )
                        val fragment = SupportMapFragment.newInstance(options)
                        fragment.getMapAsync { vietmapGL ->
                            vietmapGL.setStyle(VietMapTiles.instance.lightVector())
                        }
                        return fragment
                    }

                    1 -> {
                        return EmptyFragment.newInstance()
                    }

                    2 -> {
                        options.camera(
                            CameraPosition.Builder().target(LatLng(62.326440, 92.764913)).zoom(3.0)
                                .build()
                        )
                        val fragment = SupportMapFragment.newInstance(options)
                        fragment.getMapAsync { vietMapGL ->
                            vietMapGL.setStyle(
                                "https://maps.vietmap.vn/api/maps/raster/styles.json?apikey=YOUR_API_KEY_HERE"
                            )
                        }
                        return fragment
                    }

                    3 -> {
                        return EmptyFragment.newInstance()
                    }

                    4 -> {
                        options.camera(
                            CameraPosition.Builder().target(LatLng(-25.007786, 133.623852))
                                .zoom(3.0).build()
                        )
                        val fragment = SupportMapFragment.newInstance(options)
                        fragment.getMapAsync { vietMapGL ->
                            vietMapGL.setStyle(
                                "https://maps.vietmap.vn/api/maps/light/styles.json?apikey=YOUR_API_KEY_HERE"
                            )
                        }
                        return fragment
                    }

                    5 -> {
                        return EmptyFragment.newInstance()
                    }
                }
                throw IllegalAccessError()
            }

            override fun getCount(): Int {
                return 6
            }
        }

        class EmptyFragment : Fragment() {
            companion object {
                fun newInstance(): EmptyFragment {
                    return EmptyFragment()
                }
            }

            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
            ): View? {
                return TextView(inflater.context)
            }
        }
    }
}