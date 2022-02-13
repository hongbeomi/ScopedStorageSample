package github.hongbeomi.scopedstoragesample

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.OutputStream

private const val IMAGE_JPEG_SUFFIX = ".jpg"
private const val IMAGE_MIME_TYPE = "image/jpeg"

internal class MediaUtil {

    companion object {
        internal fun Bitmap.saveToGallery(context: Context): Uri? {
            val imageOutputStream: OutputStream

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues()

                    contentValues.apply {
                        put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            "${System.currentTimeMillis()}$IMAGE_JPEG_SUFFIX"
                        )
                        put(MediaStore.MediaColumns.MIME_TYPE, IMAGE_MIME_TYPE)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    imageOutputStream = resolver.openOutputStream(imageUri!!)!!
                    imageOutputStream.use {
                        this.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()

                    return imageUri
                } else {
                    val imageUrl = MediaStore.Images.Media.insertImage(
                        context.contentResolver,
                        this,
                        "${System.currentTimeMillis()}",
                        "${context.applicationInfo.loadLabel(context.packageManager)}-image"
                    )
                    val savedImageUri = Uri.parse(imageUrl)

                    Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()

                    return savedImageUri
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Image not saved \n$e", Toast.LENGTH_SHORT).show()
            }
            return null
        }

        internal fun Context.getMediaUri(): Uri {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis()}$IMAGE_JPEG_SUFFIX")
                    put(MediaStore.MediaColumns.MIME_TYPE, IMAGE_MIME_TYPE)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )!!
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                if (!directory.exists()) {
                    directory.mkdir()
                }
                val file = File.createTempFile("${System.currentTimeMillis()}", IMAGE_JPEG_SUFFIX, directory)
                FileProvider.getUriForFile(
                    this,
                    this.applicationContext.packageName + ".provider",
                    file
                )
            }
        }

        internal fun Context.scanMediaToBitmap(uri: Uri, action: (Bitmap) -> Unit) {
            MediaScannerConnection.scanFile(this, arrayOf(uri.path), null) { _, _ ->
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(contentResolver, uri)
                    )
                } else {
                    val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    originalBitmap.rotateFromGalleryPreVersionP(this, uri)
                }
                action.invoke(bmp)
            }
        }

        private fun Bitmap.rotateFromGalleryPreVersionP(context: Context, uri: Uri): Bitmap {
            val path = context.getFilePath(uri)
            return Bitmap.createBitmap(
                this,
                0,
                0,
                width,
                height,
                Matrix().apply {
                    postRotate(
                        calculateExif(path)
                    )
                },
                false
            )
        }

        private fun Context.getFilePath(uri: Uri): String {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.moveToNext()
            val path = cursor?.getString(cursor.getColumnIndex("_data"))
            cursor?.close()

            if (path == null) {
                Toast.makeText(this, "not found file path!", Toast.LENGTH_SHORT).show()
                throw NullPointerException()
            } else {
                return path
            }
        }

        /**
         * Mac의 경우 Orientation 해석이 다를 수 있어서 테스트 시 실기기를 사용해야함
         * https://stackoverflow.com/questions/39400351/android-exif-data-always-0-how-to-change-it/39567169
         */
        private fun calculateExif(path: String): Float {
            val attribute = ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            return when (attribute) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }

    }

}