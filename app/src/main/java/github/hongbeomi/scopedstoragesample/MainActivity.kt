package github.hongbeomi.scopedstoragesample

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import github.hongbeomi.scopedstoragesample.MediaUtil.Companion.getMediaUri
import github.hongbeomi.scopedstoragesample.MediaUtil.Companion.saveToGallery
import github.hongbeomi.scopedstoragesample.MediaUtil.Companion.scanMediaToBitmap
import github.hongbeomi.scopedstoragesample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val choosePhoto = registerForActivityResult(ActivityResultContracts.GetContent()) {
        binding.imageViewImageFromGallery.setImageURI(it)
    }
    private var photoUri: Uri? = null
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess && photoUri != null) {
            scanMediaToBitmap(photoUri!!) {
                runOnUiThread {
                    binding.imageViewImageFromGallery.setImageURI(photoUri!!)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.checkPermission()

        binding.apply {
            buttonSaveBitmap.setOnClickListener {
                val bitmapDrawable = imageViewSample.drawable as? BitmapDrawable
                val bitmap = bitmapDrawable?.bitmap
                bitmap?.saveToGallery(this@MainActivity)
            }
            buttonTakePhotoSaveToGallery.setOnClickListener {
                photoUri = this@MainActivity.getMediaUri()
                takePhoto.launch(photoUri)
            }
            buttonGetImageFromGallery.setOnClickListener {
                choosePhoto.launch("image/Pictures/*")
            }
        }
    }

}