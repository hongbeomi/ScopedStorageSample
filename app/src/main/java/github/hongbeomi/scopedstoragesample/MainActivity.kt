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
    private val flexibleTakePicture = FlexibleTakePicture()
    private val takePhoto = registerForActivityResult(flexibleTakePicture) { isSuccess ->
        if (isSuccess) {
            photoUri?.let { uri ->
                scanMediaToBitmap(uri) {
                    runOnUiThread {
                        binding.imageViewImageFromGallery.setImageURI(uri)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.checkPermission()

        with(binding) {
            buttonSaveBitmap.setOnClickListener {
                val bitmapDrawable = imageViewSample.drawable as? BitmapDrawable
                val bitmap = bitmapDrawable?.bitmap
                bitmap?.saveToGallery(this@MainActivity)
            }
            buttonTakePhotoSaveToGallery.setOnClickListener {
                val intent = flexibleTakePicture.newIntent()
                photoUri = this@MainActivity.getMediaUri(intent)

                takePhoto.launch(null)
            }
            buttonGetImageFromGallery.setOnClickListener {
                choosePhoto.launch("image/Pictures/*")
            }
        }
    }

}