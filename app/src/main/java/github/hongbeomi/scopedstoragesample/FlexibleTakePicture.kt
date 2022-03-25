package github.hongbeomi.scopedstoragesample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

class CustomTakePicture : ActivityResultContract<Unit, Boolean>() {

    private var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    fun newIntent(): Intent {
        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return intent
    }

    override fun createIntent(context: Context, input: Unit?): Intent {
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }

}