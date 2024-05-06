package vn.vietmap.mapsdkdemo.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.LongSparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import butterknife.internal.ListenerClass.NONE
import vn.vietmap.mapsdkdemo.R
import vn.vietmap.mapsdkdemo.utils.VietMapTiles
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.VietMapGL

class AnimatorAnimationActivity : AppCompatActivity() , OnMapReadyCallback{
    private  val animators = LongSparseArray<Animator>()
    private  lateinit var set:Animator
    private lateinit var mapView: MapView
    private lateinit var vietMapGL: VietMapGL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animator_animation)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(p0: VietMapGL) {
        vietMapGL = p0
        vietMapGL.setStyle(VietMapTiles.instance.lightVector())
        initFab()
    }
    private  fun initFab(){
        findViewById<View>(R.id.fab).setOnClickListener{
            view:View->
            view.visibility = View.GONE
            val animatedPosition =
                CameraPosition.Builder()
                    .target(LatLng(16.4356518,107.4773938))
                    .tilt(60.0).zoom(5.5).bearing(180.0)
                    .build()
            set = createExampleAnimator(vietMapGL.cameraPosition, animatedPosition)
            set.start()
        }
    }

    private fun createExampleAnimator(
        currentPosition: CameraPosition,
        targetPosition: CameraPosition
    ): Animator {
        val animatorSet = AnimatorSet()
        animatorSet.play(createLatLngAnimator(currentPosition.target!!, targetPosition.target!!))
        animatorSet.play(createZoomAnimator(currentPosition.zoom, targetPosition.zoom))
        animatorSet.play(createBearingAnimator(currentPosition.bearing, targetPosition.bearing))
        animatorSet.play(createTiltAnimator(currentPosition.tilt, targetPosition.tilt))
        return animatorSet
    }

    private fun createLatLngAnimator(currentPosition: LatLng, targetPosition: LatLng): Animator {
        val latLngAnimator =
            ValueAnimator.ofObject(LatLngEvaluator(), currentPosition, targetPosition)
        latLngAnimator.duration = (1000 * ANIMATION_DELAY_FACTOR).toLong()
        latLngAnimator.interpolator = FastOutSlowInInterpolator()
        latLngAnimator.addUpdateListener { animation: ValueAnimator ->
            vietMapGL.moveCamera(
                CameraUpdateFactory.newLatLng((animation.animatedValue as LatLng))
            )
        }
        return latLngAnimator
    }

    private fun createZoomAnimator(currentZoom: Double, targetZoom: Double): Animator {
        val zoomAnimator = ValueAnimator.ofFloat(currentZoom.toFloat(), targetZoom.toFloat())
        zoomAnimator.duration = (2200 * ANIMATION_DELAY_FACTOR).toLong()
        zoomAnimator.startDelay = (600 * ANIMATION_DELAY_FACTOR).toLong()
        zoomAnimator.interpolator = AnticipateOvershootInterpolator()
        zoomAnimator.addUpdateListener { animation: ValueAnimator ->
            vietMapGL.moveCamera(
                CameraUpdateFactory.zoomTo((animation.animatedValue as Float).toDouble())
            )
        }
        return zoomAnimator
    }

    private fun createBearingAnimator(currentBearing: Double, targetBearing: Double): Animator {
        val bearingAnimator =
            ValueAnimator.ofFloat(currentBearing.toFloat(), targetBearing.toFloat())
        bearingAnimator.duration = (1000 * ANIMATION_DELAY_FACTOR).toLong()
        bearingAnimator.startDelay = (1000 * ANIMATION_DELAY_FACTOR).toLong()
        bearingAnimator.interpolator = FastOutLinearInInterpolator()
        bearingAnimator.addUpdateListener { animation: ValueAnimator ->
            vietMapGL.moveCamera(
                CameraUpdateFactory.bearingTo((animation.animatedValue as Float).toDouble())
            )
        }
        return bearingAnimator
    }

    private fun createTiltAnimator(currentTilt: Double, targetTilt: Double): Animator {
        val tiltAnimator = ValueAnimator.ofFloat(currentTilt.toFloat(), targetTilt.toFloat())
        tiltAnimator.duration = (1000 * ANIMATION_DELAY_FACTOR).toLong()
        tiltAnimator.startDelay = (1500 * ANIMATION_DELAY_FACTOR).toLong()
        tiltAnimator.addUpdateListener { animation: ValueAnimator ->
            vietMapGL.moveCamera(
                CameraUpdateFactory.tiltTo((animation.animatedValue as Float).toDouble())
            )
        }
        return tiltAnimator
    }

    //
    // Interpolator examples
    //
    private fun obtainExampleInterpolator(menuItemId: Int): Animator? {
        return animators[menuItemId.toLong()]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_animator, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!::vietMapGL.isInitialized) {
            return false
        }
        if (item.itemId != android.R.id.home) {
            findViewById<View>(R.id.fab).visibility = View.GONE
            resetCameraPosition()
            playAnimation(item.itemId)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetCameraPosition() {
        vietMapGL.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(START_LAT_LNG)
                    .zoom(11.0)
                    .bearing(0.0)
                    .tilt(0.0)
                    .build()
            )
        )
    }

    private fun playAnimation(itemId: Int) {
        val animator = obtainExampleInterpolator(itemId)
        if (animator != null) {
            animator.cancel()
            animator.start()
        }
    }

    private fun obtainExampleInterpolator(interpolator: Interpolator, duration: Long): Animator {
        val zoomAnimator = ValueAnimator.ofFloat(11.0f, 16.0f)
        zoomAnimator.duration = (duration * ANIMATION_DELAY_FACTOR).toLong()
        zoomAnimator.interpolator = interpolator
        zoomAnimator.addUpdateListener { animation: ValueAnimator ->
            vietMapGL.moveCamera(
                CameraUpdateFactory.zoomTo((animation.animatedValue as Float).toDouble())
            )
        }
        return zoomAnimator
    }

    //
    // MapView lifecycle
    //
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        for (i in 0 until animators.size()) {
            animators[animators.keyAt(i)]!!.cancel()
        }
        if (set != null) {
            set.cancel()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mapView.isInitialized) {
            mapView.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (::mapView.isInitialized) {
            mapView.onLowMemory()
        }
    }

    /** Helper class to evaluate LatLng objects with a ValueAnimator */
    private class LatLngEvaluator : TypeEvaluator<LatLng> {
        private val latLng = LatLng()
        override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
            latLng.latitude = startValue.latitude + (endValue.latitude - startValue.latitude) * fraction
            latLng.longitude = startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
            return latLng
        }
    }

    companion object {
        private const val ANIMATION_DELAY_FACTOR = 1.5
        private val START_LAT_LNG = LatLng(20.957036, 105.792975            )
    }

    init {
        val accelerateDecelerateAnimatorSet = AnimatorSet()
        accelerateDecelerateAnimatorSet.playTogether(
            createLatLngAnimator(START_LAT_LNG, LatLng(20.957036, 105.792975)),
            obtainExampleInterpolator(FastOutSlowInInterpolator(), 2500)
        )
        animators.put(
            R.id.menu_action_accelerate_decelerate_interpolator.toLong(),
            accelerateDecelerateAnimatorSet
        )
        val bounceAnimatorSet = AnimatorSet()
        bounceAnimatorSet.playTogether(
            createLatLngAnimator(START_LAT_LNG, LatLng(20.957036, 105.792975)),
            obtainExampleInterpolator(BounceInterpolator(), 1650)
        )
        animators.put(R.id.menu_action_bounce_interpolator.toLong(), bounceAnimatorSet)
        animators.put(
            R.id.menu_action_anticipate_overshoot_interpolator.toLong(),
            obtainExampleInterpolator(AnticipateOvershootInterpolator(), 2500)
        )
        animators.put(
            R.id.menu_action_path_interpolator.toLong(),
            obtainExampleInterpolator(
                PathInterpolatorCompat.create(.22f, .68f, 0f, 1.71f),
                2500
            )
        )
    }
}