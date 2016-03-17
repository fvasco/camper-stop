package io.github.fvasco.pinpoi

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.fvasco.pinpoi.dao.PlacemarkCollectionDao
import io.github.fvasco.pinpoi.dao.PlacemarkDao
import io.github.fvasco.pinpoi.model.Placemark
import io.github.fvasco.pinpoi.model.PlacemarkAnnotation
import io.github.fvasco.pinpoi.util.LocationUtil
import io.github.fvasco.pinpoi.util.escapeHtml
import io.github.fvasco.pinpoi.util.isHtml
import kotlinx.android.synthetic.main.placemark_detail.*
import java.util.concurrent.Future

/**
 * A fragment representing a single Placemark detail screen.
 * This fragment is either contained in a [PlacemarkListActivity]
 * in two-pane mode (on tablets) or a [PlacemarkDetailActivity]
 * on handsets.
 */
class PlacemarkDetailFragment : Fragment() {
    // show coordinates
    // show address
    // show placemark collection details
    var placemark: Placemark? = null
        set(placemark) {
            saveData()
            field = placemark
            Log.i(PlacemarkDetailFragment::class.java.simpleName, "open placemark " + placemark?.id)
            placemarkAnnotation = if (placemark == null) null else placemarkDao.loadPlacemarkAnnotation(placemark)
            val placemarkCollection = if (placemark == null) null else placemarkCollectionDao.findPlacemarkCollectionById(placemark.collectionId)
            if (placemark != null) {
                preferences.edit().putLong(ARG_PLACEMARK_ID, placemark.id).apply()
            }

            val activity = this.activity
            val appBarLayout = activity.findViewById(R.id.toolbarLayout) as? CollapsingToolbarLayout
            if (appBarLayout != null) {
                appBarLayout.title = placemark?.name
            }
            placemarkDetailText.text = if (placemark == null)
                null
            else if (placemark.description.isNullOrEmpty())
                placemark.name
            else if (placemark.description.isHtml())
                Html.fromHtml("<p>" + escapeHtml(placemark.name) + "</p>" + placemark.description)
            else
                placemark.name + "\n\n" + placemark.description
            noteText.setText(if (placemarkAnnotation == null) null else placemarkAnnotation!!.note)
            coordinatesText.text = if (placemark == null)
                null
            else
                getString(R.string.location,
                        Location.convert(placemark.coordinates.latitude.toDouble(), Location.FORMAT_DEGREES),
                        Location.convert(placemark.coordinates.longitude.toDouble(), Location.FORMAT_DEGREES))
            searchAddressFuture?.cancel(true)
            addressText.text = null
            addressText.visibility = View.GONE
            if (placemark != null) {
                searchAddressFuture = LocationUtil.getAddressStringAsync(placemark.coordinates) { address ->
                    if (!address.isNullOrEmpty()) {
                        addressText.visibility = View.VISIBLE
                        addressText.text = address
                    }
                }
            }
            if (placemarkCollection != null) {
                collectionDescriptionTitle.visibility = View.VISIBLE
                collectionDescriptionText.visibility = View.VISIBLE
                collectionDescriptionTitle.text = placemarkCollection.name
                collectionDescriptionText.text = placemarkCollection.description
            } else {
                collectionDescriptionTitle.visibility = View.GONE
                collectionDescriptionText.visibility = View.GONE
            }
        }
    val longClickListener: View.OnLongClickListener = View.OnLongClickListener { view ->
        LocationUtil.openExternalMap(placemark!!, true, view.context)
        true
    }
    private lateinit var placemarkDao: PlacemarkDao
    private lateinit var placemarkCollectionDao: PlacemarkCollectionDao
    var placemarkAnnotation: PlacemarkAnnotation? = null
        private set
    private lateinit var preferences: SharedPreferences
    private var searchAddressFuture: Future<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = activity.getSharedPreferences(PlacemarkDetailFragment::class.java.simpleName, Context.MODE_PRIVATE)
        placemarkDao = PlacemarkDao.instance
        placemarkDao.open()
        placemarkCollectionDao = PlacemarkCollectionDao.instance
        placemarkCollectionDao.open()

        val id = if (savedInstanceState == null)
            arguments.getLong(ARG_PLACEMARK_ID, preferences.getLong(ARG_PLACEMARK_ID, 0))
        else
            savedInstanceState.getLong(ARG_PLACEMARK_ID)
        preferences.edit().putLong(ARG_PLACEMARK_ID, id).apply()
    }

    override fun onStop() {
        saveData()
        super.onStop()
    }

    override fun onDestroy() {
        placemarkDao.close()
        placemarkCollectionDao.close()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.placemark_detail, container, false)
        return rootView
    }

    override fun onStart() {
        super.onStart()
        // By default these links will appear but not respond to user input.
        placemarkDetailText.movementMethod = LinkMovementMethod.getInstance()
        placemark = placemarkDao.getPlacemark(preferences.getLong(ARG_PLACEMARK_ID, 0))
    }

    override fun onResume() {
        super.onResume()
        resetStarFabIcon(activity.findViewById(R.id.fabStar) as FloatingActionButton)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (placemark != null) {
            outState!!.putLong(ARG_PLACEMARK_ID, placemark!!.id)
        }
        super.onSaveInstanceState(outState)
    }

    fun onMapClick(view: View) {
        placemark?.apply {
            LocationUtil.openExternalMap(this, false, view.context)
        }
    }


    fun resetStarFabIcon(starFab: FloatingActionButton) {
        val drawable = if (placemarkAnnotation?.isFlagged ?: false)
            R.drawable.abc_btn_rating_star_on_mtrl_alpha
        else
            R.drawable.abc_btn_rating_star_off_mtrl_alpha
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            starFab.setImageDrawable(resources.getDrawable(drawable, activity.baseContext.theme))
        } else {
            //noinspection deprecation
            starFab.setImageDrawable(resources.getDrawable(drawable))
        }
    }

    fun onStarClick(starFab: FloatingActionButton) {
        placemarkAnnotation!!.isFlagged = !placemarkAnnotation!!.isFlagged
        resetStarFabIcon(starFab)
    }

    private fun saveData() {
        // save previous annotation
        placemarkAnnotation?.apply {
            note = noteText.text.toString()
            placemarkDao.update(this)
        }
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        @JvmStatic
        val ARG_PLACEMARK_ID = "placemarkId"
    }

}
