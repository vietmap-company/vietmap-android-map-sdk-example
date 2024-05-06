package vn.vietmap.mapsdkdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.databinding.ActivityVietMapGlsurfaceRecyclerViewBinding
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.Style

class VietMapGLSurfaceRecyclerView : AppCompatActivity() {
    lateinit var binding: ActivityVietMapGlsurfaceRecyclerViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVietMapGlsurfaceRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ItemAdapter(this, LayoutInflater.from(this))
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // to release memory, we need to call MapView#onLowMemory
        (binding.recyclerView.adapter as ItemAdapter).onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        // to perform cleanup, we need to call MapView#onDestroy
        (binding.recyclerView.adapter as ItemAdapter).onDestroy()
    }

    open fun getMapItemLayoutId(): Int {
        return R.layout.item_vietmap_gl
    }


    class ItemAdapter(
        private val activity: VietMapGLSurfaceRecyclerView, private val inflater: LayoutInflater
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val items = listOf(

            "one",
            "two",
            "three",
            MapItem(VietMapTiles.instance.lightVector()),
            "four",
            "five",
            MapItem(VietMapTiles.instance.lightVector()
            ),
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
        private var mapHolders: MutableList<MapHolder> = mutableListOf()

        companion object {
            const val TYPE_MAP = 0
            const val TYPE_TEXT = 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_MAP) {
                val mapView =
                    inflater.inflate(activity.getMapItemLayoutId(), parent, false) as MapView
                val mapHolder = MapHolder(mapView)
                return mapHolder
            } else {
                TextHolder(
                    inflater.inflate(
                        android.R.layout.simple_list_item_1, parent, false
                    ) as TextView
                )
            }
        }

        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            super.onViewAttachedToWindow(holder)
            if (holder is MapHolder) {
                val mapView = holder.mapView
                mapView.isEnabled = false
                mapView.onStart()
                mapView.onResume()
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            if (holder is MapHolder) {
                val mapView = holder.mapView
                mapView.onPause()
                mapView.onStop()
            }
        }

        override fun getItemCount(): Int {
            return items.count()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is TextHolder) {
                holder.bind(items[position] as String)
            } else if (holder is MapHolder) {
                holder.bind(items[position] as MapItem)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (items[position] is MapItem) {
                TYPE_MAP
            } else {
                TYPE_TEXT
            }
        }

        fun onLowMemory() {
            for (mapHolder in mapHolders) {
                mapHolder.mapView.onLowMemory()
            }
        }

        fun onDestroy() {
            for (mapHolder in mapHolders) {
                mapHolder.mapView.let {
                    it.onPause()
                    it.onStop()
                    it.onDestroy()
                }
            }
        }

        data class MapItem(val style: String)
        class MapHolder(val mapView: MapView) : RecyclerView.ViewHolder(mapView) {
            init {
                mapView.onCreate(null)
                mapView.setOnTouchListener { view, motionEvent ->
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    mapView.onTouchEvent(motionEvent)
                    true
                }
            }

            fun bind(mapItem: MapItem) {
                mapView.getMapAsync { vietmapGL ->
                    vietmapGL.setStyle(mapItem.style) {
                        vietmapGL.snapshot { }
                    }
                }
            }
        }

        class TextHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
            fun bind(item: String) {
                textView.text = item
            }
        }
    }
}