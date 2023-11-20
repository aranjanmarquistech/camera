package com.example.camera.photo

import BaseActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.example.camera.CameraActivity
import com.example.camera.Constants.EXTRA_IMAGE_CAMERA
import com.example.camera.R
import com.example.camera.databinding.ActivityPhotoBinding

class PhotoActivity : BaseActivity<ActivityPhotoBinding>(R.layout.activity_photo) {

    companion object {
        fun getStartIntent(context: Context, photoUri: String): Intent =
            Intent(context, PhotoActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_CAMERA, photoUri)
            }
    }

    private var picture: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiveIntent()
        takeAnotherPhoto()
    }

    private fun receiveIntent() {
        picture = intent?.getStringExtra(EXTRA_IMAGE_CAMERA) as String
        setupView()
    }

    private fun setupView() {
        val pictureUri: Uri? = Uri.parse(picture)
        binding.ivPhoto.setImageURI(pictureUri)
    }

    private fun takeAnotherPhoto() {
        binding.takeAnotherPicture.setOnClickListener {
            startActivity(CameraActivity.getStartIntent(this))
            finish()
        }
        binding.close.setOnClickListener {
            finish()
        }
    }


}


