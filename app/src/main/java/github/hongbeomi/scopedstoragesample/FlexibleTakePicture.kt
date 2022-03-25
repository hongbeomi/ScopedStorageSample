package github.hongbeomi.scopedstoragesample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.EXTRA_OUTPUT
import androidx.activity.result.contract.ActivityResultContract

class FlexibleTakePicture : ActivityResultContract<Uri, Boolean>() {

    private var intent: Intent? = null

    fun newIntent(): Intent {
        val generatedIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent = generatedIntent
        return generatedIntent
    }

    override fun createIntent(context: Context, input: Uri?): Intent {
        val createdIntent =  intent ?: newIntent()

        if (input != null) {
            createdIntent.putExtra(EXTRA_OUTPUT, input)
        }
        return createdIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }

}