package com.ksc.contactformassignment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SelfieActivity : AppCompatActivity(), LocationListener {

    private lateinit var captureButton: Button
    private lateinit var submitButton: Button
    private lateinit var selfieImageView: ImageView
    private var capturedImage: Bitmap? = null
    private lateinit var locationManager: LocationManager
    private var latitude: Double? = null
    private var longitude: Double? = null

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_LOCATION_PERMISSION = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selfie)

        captureButton = findViewById(R.id.btn_capture_selfie)
        submitButton = findViewById(R.id.btn_submit)
        selfieImageView = findViewById(R.id.image_selfie)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        checkProviderAvailability()


        captureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                openCamera()
            }
        }

        submitButton.setOnClickListener {
            if (capturedImage == null) {
                Toast.makeText(this, "Please capture your selfie before proceeding", Toast.LENGTH_SHORT).show()
            } else if (latitude == null || longitude == null) {
                Toast.makeText(this, "Waiting for location. Please ensure GPS is enabled.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location captured: Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()
                // Pass data to next activity or handle accordingly
                val intent = Intent(this, ResultActivity::class.java)
                startActivity(intent)
            }
        }

        requestLocationUpdates()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val cameraId = getFrontCameraId()
            if (cameraId != null) {
                takePictureIntent.putExtra("android.intent.extra.CAMERA_FACING", 1)
            }
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFrontCameraId(): String? {
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        for (cameraId in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId
            }
        }
        return null
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, this)
            // Get last known location as a fallback
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                latitude = lastKnownLocation.latitude
                longitude = lastKnownLocation.longitude
            }
        }
    }


    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required to capture a selfie", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates()
                } else {
                    Toast.makeText(this, "Location permission is required for GPS", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun checkProviderAvailability() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("LocationCheck", "GPS Provider is enabled.")
            Toast.makeText(this, "GPS Provider is enabled.", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("LocationCheck", "GPS Provider is not enabled.")
            Toast.makeText(this, "GPS provider is not enabled. Please enable GPS.", Toast.LENGTH_SHORT).show()
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.d("LocationCheck", "Network Provider is enabled.")
            Toast.makeText(this, "Network Provider is enabled.", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("LocationCheck", "Network Provider is not enabled.")
            Toast.makeText(this, "Network provider is not enabled.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            capturedImage = imageBitmap
            selfieImageView.setImageBitmap(imageBitmap)
        }
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
