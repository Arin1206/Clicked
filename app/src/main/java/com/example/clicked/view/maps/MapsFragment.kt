package com.example.clicked.view.maps

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.clicked.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapsFragment : Fragment() {

    private lateinit var googleMap: GoogleMap
    private val offset = 0.0001

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        fetchNewsData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun fetchNewsData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("news")
            .get()
            .addOnSuccessListener { result ->
                val markerLocations = mutableSetOf<LatLng>()
                for (document in result) {
                    var latitude = document.getDouble("latitude")
                    var longitude = document.getDouble("longitude")
                    val title = document.getString("judulBerita")
                    val description = document.getString("paragraf1")

                    if (latitude != null && longitude != null && title != null && description != null) {
                        var location = LatLng(latitude, longitude)

                        while (markerLocations.contains(location)) {
                            latitude += offset
                            longitude += offset
                            location = LatLng(latitude, longitude)
                        }
                        markerLocations.add(location)

                        googleMap.addMarker(
                            MarkerOptions().position(location).title(title).snippet(description)
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                    }
                }
            }
            .addOnFailureListener { exception ->

            }
    }
}
