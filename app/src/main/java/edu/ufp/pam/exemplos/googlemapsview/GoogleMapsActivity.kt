package edu.ufp.pam.exemplos.googlemapsview

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.IOException
import java.util.*
import edu.ufp.pam.exemplos.R


/**
 * Starting with Android 6.0, user permissions are handled a little differently than before, i.e.,
 * no need request permission during the installation of app; instead, request permissions at
 * runtime, when these are actually required.
 *
 * Permissions are classified into two categories:
 *  normal categories and dangerous categories (require run time permission from user, such as
 *  access to CONTACTS, CALENDAR, LOCATION, etc.).
 *
 *  Add a companion object with the code to request location permission.
 *
 *  ================ TO DO   ================
 *  1. Change App ID:
 *    - File > Project Structure:
 *          Modules > Default Config:
 *              Application ID: package edu.ufp.pam.examples.googlemapsview
 *
 *  2. Get API Key from Google Console:
 *    - Login to https://console.developers.google.com/ to generate API Key
 *          Restrict key use to Android apps (set package name and SHA-1 certificate fingerprint).
 *
 *  3. Insert the API Key into google_maps_api.xml file
 *    AndroidManifest should also include meta-data with the API key from this file.
 */
class GoogleMapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnPolylineClickListener,
    GoogleMap.OnPolygonClickListener{

    private lateinit var map: GoogleMap

    //Set a companion object (static field) with code to request location permission:
    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FIME_LOCATION_CODE = 1
        private const val PERMISSION_REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
        private const val REQUESTING_LOCATION_UPDATES_KEY = "LOCATION_UPDATES_KEY"

        //Color, Thickness and Pattern of polyline and polygon
        private const val COLOR_BLACK_ARGB = 0xff000000
        private const val COLOR_WHITE_ARGB = -0x1
        private const val COLOR_GREEN_ARGB = -0xc771c4
        private const val COLOR_PURPLE_ARGB = -0x7e387c
        private const val COLOR_ORANGE_ARGB = -0xa80e9
        private const val COLOR_BLUE_ARGB = -0x657db

        private const val POLYLINE_STROKE_WIDTH_PX = 12
        private const val POLYGON_STROKE_WIDTH_PX = 8
        private const val PATTERN_DASH_LENGTH_PX = 20.0f
        private const val PATTERN_GAP_LENGTH_PX = 20.0f
    }

    private val DOT: PatternItem = Dot()
    private val DASH: PatternItem = Dash(PATTERN_DASH_LENGTH_PX)
    private val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX)

    // Create a stroke pattern of a gap followed by a dot.
    private val PATTERN_POLYLINE_DOTTED: List<PatternItem> = Arrays.asList(GAP, DOT)
    // Create a stroke pattern of a gap followed by a dash.
    private val PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH)
    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private val PATTERN_POLYGON_BETA = Arrays.asList(DOT, GAP, DASH, GAP)


    //Properties enabling to receive device location updates
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationRequestsActivated = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        updateValuesFromBundle(savedInstanceState)

        // Construct a FusedLocationProviderClient to be able to obtain current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Setup LocationCallback() to receive current location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(localRes: LocationResult) {
                super.onLocationResult(localRes)
                lastLocation = localRes.lastLocation
                Log.e(
                    this.javaClass.simpleName,
                    "onLocationResult(): localRes=${lastLocation.toString()}"
                )
                //Put marker on the lastly received known location
                placeMarkerOnMapWithUserIcon(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }

        //Create LocatioRequest so that When app change location the map updates with a new marker
        createLocationRequest()
    }

    /**
     * Callback triggered when the map is ready to be used... manipulate the map once available:
     *  - add markers or lines, add listeners or move the camera.
     *    e.g. add a marker near Sydney, Australia.
     *
     * If Google Play services not installed on device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        Log.e(this.javaClass.simpleName, "onMapReady(): going to draw markers on map...")
        //STEP 1: Zoom over a city
        //drawSydneyMarker()
        //drawRotundaBoavistaMarker(16.0f)

        //STEP 2: Get current location
        //Draw a polyline
        //drawPolyLine()

        //STEP 3: Get current location
        //Get permissions from user and show current location
        setUpMapTypeAndDrawCurrentLocationMarker()
    }

    override fun onMarkerClick(marker: Marker?) = false

    override fun onPolylineClick(polyline: Polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if (polyline.pattern == null || !polyline.pattern!!.contains(DOT)) {
            polyline.pattern = PATTERN_POLYLINE_DOTTED
        } else {
            // The default pattern is a solid stroke.
            polyline.pattern = null
        }
        Toast.makeText(
            this, "Route type " + polyline.tag.toString(),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPolygonClick(p0: Polygon?) {
        TODO("Not yet implemented")
    }

    private fun drawSydneyMarker(){
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun drawRotundaBoavistaMarker(zoom: Float){
        //Add a marker in Rotunda Boavista @ Porto and zoom it
        val rotundaBoavista = LatLng(41.157921, -8.629162)

        map.addMarker(MarkerOptions().position(rotundaBoavista).title("Lyon over Eagle! :)"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(rotundaBoavista, zoom))

        //Enable zoom controls on the map (Zoom level 0..20)
        map.getUiSettings().setZoomControlsEnabled(true)
        //Declare instance callback triggered when user clicks a marker on this map
        map.setOnMarkerClickListener(this)
    }

    private fun drawPolyLineRotunda2Palacio(){
        val polylineA: Polyline = map.addPolyline(
            PolylineOptions()
                .clickable(true)
                .add(
                    LatLng(41.157921, -8.629162),
                    LatLng(41.155013, -8.626934),
                    LatLng(41.152627, -8.626193),
                    LatLng(41.151305, -8.625782),
                    LatLng(41.150883, -8.625931),
                    LatLng(41.148773, -8.625408)
                )
        )
        // Store a data object with the polyline to associate a tag
        polylineA.setTag("A");
        //Set properties
        stylePolyline(polylineA)

        // Set listeners for click events
        map.setOnPolylineClickListener(this);
        //map.setOnPolygonClickListener(this);
    }

    private fun stylePolyline(polyline: Polyline) {
        var type = ""
        // Get the data object stored with the polyline.
        if (polyline.tag != null) {
            type = polyline.tag.toString()
        }
        when (type) {
            "A" -> // Use a custom bitmap as the cap at the start of the line
                polyline.startCap = CustomCap(
                    BitmapDescriptorFactory.fromResource(R.mipmap.ic_user_location), 10F
                )
            "B" -> // Use a round cap at the start of the line
                polyline.startCap = RoundCap()
        }
        polyline.endCap = RoundCap()
        polyline.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
        polyline.color = COLOR_BLACK_ARGB.toInt()
        polyline.jointType = JointType.ROUND
    }

    private fun stylePolygon(polygon: Polygon) {
        var type = ""
        // Get the data object stored with the polygon.
        if (polygon.tag != null) {
            type = polygon.tag.toString()
        }
        var pattern: List<PatternItem?>? = null
        var strokeColor = COLOR_BLACK_ARGB.toInt()
        var fillColor = COLOR_WHITE_ARGB
        when (type) {
            "A" -> {
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA
                strokeColor = COLOR_GREEN_ARGB
                fillColor = COLOR_PURPLE_ARGB
            }
            "B" -> {
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA
                strokeColor = COLOR_ORANGE_ARGB
                fillColor = COLOR_BLUE_ARGB
            }
        }
        polygon.strokePattern = pattern
        polygon.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()
        polygon.strokeColor = strokeColor
        polygon.fillColor = fillColor
    }

    /**
     * Check if app has been granted ACCESS_FINE_LOCATION permission,
     * and if it does not then request it from the user
     */
    private fun askUserPermissionToAccessFineLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FIME_LOCATION_CODE
            )
            return
        }
    }

    /**
     * Method called after asking user permission:
     *  Start update request if it has RESULT_OK for the REQUEST_CHECK_SETTINGS
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //If receives code for permission request
        if (requestCode == PERMISSION_REQUEST_CHECK_SETTINGS) {
            Log.e(
                this.javaClass.simpleName,
                "onActivityResult(): requestCode = REQUEST_CHECK_SETTINGS"
            )
            //If permission granted
            if (resultCode == Activity.RESULT_OK) {
                locationRequestsActivated = true
                startLocationUpdates()
            }
        }
    }

    private fun setUpMapTypeAndDrawCurrentLocationMarker() {
        //Get permissions from user
        askUserPermissionToAccessFineLocation()

        //Android Maps API provides different map types:
        //  MAP_TYPE_NORMAL: typical road map with labels
        //  MAP_TYPE_SATELLITE: satellite view of an area with no labels
        //  MAP_TYPE_TERRAIN: detailed view of area (e.g.show elevation)
        //  MAP_TYPE_HYBRID: combination of the satellite and normal mode
        map.mapType = GoogleMap.MAP_TYPE_HYBRID

        //Get zoom at current location
        zoomCurrentLocationMarker()
    }

    @Throws(SecurityException::class)
    private fun zoomCurrentLocationMarker(){
        // Enable my-location layer which draws a light blue dot on the user’s location.
        // Also adds Button to map to center on user’s location.
        map.isMyLocationEnabled = true

        // Gives most recent available location
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Move camera to the user’s current location.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //Draw marker
                //placeMarkerOnMapWithUserIcon(currentLatLng)
                placeMarkerOnMapWithAddr(currentLatLng)
                //Zoom in
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }
    }

    private fun placeMarkerOnMapWithUserIcon(location: LatLng) {
        //Set user’s current location as the position for the marker
        val markerOptions = MarkerOptions().position(location)

        //Step 1: place marker with different color
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        //Step 2: place marker with different icon
        // Dowmload custom pins (e.g. ic_user_location) from
        // https://koenig-media.raywenderlich.com/uploads/2016/09/ic_user_location.zip
        // Unzip and copy/paste into *res* project folder
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(
                    resources,
                    R.mipmap.ic_user_location
                )
            )
        )

        //Add the marker to the map
        map.addMarker(markerOptions)
    }

    //Show address of location when the user clicks on marker
    private fun getAddress(latLng: LatLng): String {
        //Geocoder allows turning latitude and longitude coordinate into an address and vice versa.
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            //Get address from given location (lat/long)
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            Log.e(this.javaClass.simpleName, "getAddress(): addresses = ${addresses}")

            //If response contains an address then append to string and return.
            if (addresses != null  && !addresses.isEmpty()) {
                address = addresses[0]
                Log.e(this.javaClass.simpleName, "getAddress(): address = ${address}")
                Log.e(
                    this.javaClass.simpleName,
                    "getAddress(): address.maxAddressLineIndex = ${address.maxAddressLineIndex}"
                )
                Log.e(
                    this.javaClass.simpleName, "getAddress(): address.getAddressLine = " +
                            address.getAddressLine(0)
                )
                for (i in 0..address.maxAddressLineIndex) {
                    addressText +=
                        if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                    Log.e(this.javaClass.simpleName, "getAddress(): addressText = ${addressText}")
                }
            }
        } catch (e: IOException) {
            val msg = e.localizedMessage
            Log.e(this.javaClass.simpleName, msg)
        }
        return addressText
    }

    private fun placeMarkerOnMapWithAddr(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        val titleStr = getAddress(location)
        Log.e(this.javaClass.simpleName, "placeMarkerOnMapWithAddr(): titleStr = ${titleStr}")
        markerOptions.title(titleStr)
        map.addMarker(markerOptions)
    }

    private fun createLocationRequest() {
        //Create and set locationRequest attributes
        locationRequest = LocationRequest()
        //Rate at which app wants to receive updates
        locationRequest.interval = 10000
        //Rate at which app can handle updates.
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        //Builder of request
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        //Check state of user’s location settings
        val client = LocationServices.getSettingsClient(this)
        //Create task to check location settings
        val task = client.checkLocationSettings(builder.build())

        //On task success init location request
        task.addOnSuccessListener {
            locationRequestsActivated = true
            startLocationUpdates()
        }

        //On task failure means location settings have issues (e.g. location settings turned off)
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // User location settings are not satisfied... show dialog to set it
                try {
                    // Show dialog by calling startResolutionForResult() and
                    // check result in onActivityResult()
                    e.startResolutionForResult(
                        this@GoogleMapsActivity,
                        PERMISSION_REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(
                        this.javaClass.simpleName,
                        "createLocationRequest(): sendEx = ${sendEx.toString()}"
                    )
                }
            }
        }
    }

    @Throws(SecurityException::class)
    private fun startLocationUpdates() {
        //If does not have ACCESS_FINE_LOCATION then request for it
        if (!locationRequestsActivated) {
            askUserPermissionToAccessFineLocation()
        }
        Log.e(this.javaClass.simpleName,
            "startLocationUpdates(): going to request location update..."
        )
        //Request for location updates with locationRequest, locationCallback, and null Looper
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    //Stop location update request
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    //Restart the location update request
    public override fun onResume() {
        super.onResume()
        if (!locationRequestsActivated) {
            startLocationUpdates()
        }
    }

    //Callback to save the Activity instance state
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, locationRequestsActivated)
        super.onSaveInstanceState(outState)
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        // Update the value of requestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            locationRequestsActivated = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY)
        }
        //...
        // Update UI to match restored state
        //updateUI()
    }

}