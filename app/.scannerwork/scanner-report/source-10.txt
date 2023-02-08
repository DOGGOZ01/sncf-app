package com.example.sncf

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sncf.classes.Station
import com.example.sncf.classes.Train
import com.example.sncf.databinding.ActivityMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnPolylineClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        with(googleMap) {
            setOnPolylineClickListener(this@MapsActivity)
            setOnPolygonClickListener(this@MapsActivity)

            val train = intent.extras?.get("train") as Train

            val text: TextView = findViewById(R.id.text_map) as TextView
            text.setText(train.toString())

            val stops: List<LatLng> = train.stops.map { LatLng(it.getStation().lat, it.getStation().long) }

            val latAverage = train.stops.map { it.getStation().lat }.average()
            val longAverage = train.stops.map { it.getStation().long }.average()

            addPolyline(PolylineOptions().apply {
                clickable(true)
                addAll(stops)
            })

            train.stops.forEach { addMarker(it.getStation()) }

            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latAverage, longAverage), 9.5f))
        }
    }

    private fun addMarker(station: Station) {
        val marker = LatLng(station.lat, station.long)
        mMap.addMarker(MarkerOptions().position(marker).title(station.libelle))
    }

    override fun onPolygonClick(p0: Polygon) {
        TODO("Not yet implemented")
    }

    override fun onPolylineClick(p0: Polyline) {
        TODO("Not yet implemented")
    }
}