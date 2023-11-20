package com.example.camera
import BaseActivity
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.camera.camera.CameraManager
import com.example.camera.databinding.ActivityCameraBinding
import com.example.camera.photo.PhotoActivity

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.IOException

class CameraActivity : BaseActivity<ActivityCameraBinding>(R.layout.activity_camera) {

    companion object {
        fun getStartIntent(context: Context): Intent = Intent(context, CameraActivity::class.java)
    }

    private lateinit var cameraManager: CameraManager
    private var changeCamera = false
    private var flashMode = false
    private lateinit var fusedLocationClient:FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFunction()
        initLocation()

    }


    private fun initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()
        startLocationUpdates()
    }
    private fun initFunction() {
        startCamera()
        controlCameraSelectCamera()
        controlFlashCamera()
        takePhoto()
        close()
    }

    private fun startCamera() {
        cameraManager = CameraManager(
            owner = this,
            context = applicationContext,
            viewPreview = binding.pvViewCamera,
            onSuccess = ::onSuccess,
            onError = ::onError
        )
       // requestLastKnownLocation()
        cameraManager.startCamera(onFrontCamera = false)
    }

    private fun takePhoto() {

        binding.incButtons.ivCameraCapture.setOnClickListener {
            requestLastKnownLocation()

            cameraManager.takePhoto()
        }

    }

    private fun controlCameraSelectCamera() {
        binding.incButtons.ivChangeCamera.setOnClickListener {
            cameraManager.startCamera(onFrontCamera = changeCamera())
        }
    }

    private fun controlFlashCamera() {
        binding.incButtons.ivFlashCamera.setOnClickListener {
            cameraManager.enableFlash(flashMode())
        }
    }

    private fun changeCamera(): Boolean {
        changeCamera = !changeCamera
        if (changeCamera) binding.incButtons.ivFlashCamera.setImageResource(R.drawable.ic_no_flash)
        return changeCamera
    }

    private fun flashMode(): Boolean {
        flashMode = !flashMode
        if (flashMode) binding.incButtons.ivFlashCamera.setImageResource(R.drawable.ic_no_flash)
        else binding.incButtons.ivFlashCamera.setImageResource(R.drawable.ic_flash)
        return flashMode
    }

    private fun onSuccess(result: String?) {
        if (!result.isNullOrEmpty()) {
            val imageUri = Uri.parse(result)
            val imageFile = File(imageUri.path)

            // Save the image file locally
            saveImageLocally(imageFile)

            // Start PhotoActivity
            startActivity(PhotoActivity.getStartIntent(this, result))
            finish()
        }
        finish()
    }

    private fun onError(result: String?) {
        if (!result.isNullOrEmpty()) toast(result)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.stop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun close() {
        binding.ivClose.setOnClickListener {
            finish()
        }
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                updateLocation(it)
            }
        }
    }

    private fun updateLocation(location: Location) {

      //  val latitudeTextView = findViewById<TextView>(R.id.latitudeTextView)
        //val longitudeTextView = findViewById<TextView>(R.id.longitudeTextView)
        val latitude = location.latitude
        val longitude = location.longitude
      //  latitudeTextView.text = getString(R.string.latitude_format, latitude)
        //longitudeTextView.text = getString(R.string.longitude_format, longitude)
    }


    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    101
                )
            }
        }
    }

    private fun startLocationUpdates() {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000 // Update every 10 seconds
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null // Looper parameter, null for the main thread
            )
            // Request location permissions if not granted
            requestLocationPermission()
        }
    private fun requestLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        updateLocation(it)
                    }
                }
        } else {
            requestLocationPermission()
        }
    }

        private fun saveImageLocally(imageFile: File) {
            try {
                // Create a directory for saving images if it doesn't exist
                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                val cameraDir = File(storageDir, "Camera")
                if (!cameraDir!!.exists()) {
                    cameraDir.mkdirs()
                }

                // Create a destination file in the app's pictures directory
                val destFile = File(cameraDir, "captured_image_${System.currentTimeMillis()}.jpg")

                // Copy the image file to the destination
                imageFile.copyTo(destFile)

                // Optionally, you can update the media store to make the image visible in the gallery
                updateMediaStore(destFile)

                // Optionally, you can use the destFile path for further processing or display
                val savedImagePath = destFile.absolutePath
                toast("Image saved at: $savedImagePath")
            } catch (e: IOException) {
                e.printStackTrace()
                toast("Error saving image")
            }
        }

        private fun updateMediaStore(file: File) {
            // Update the media store to make the image visible in the gallery
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            sendBroadcast(mediaScanIntent)
        }
    }
fun Activity.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()




