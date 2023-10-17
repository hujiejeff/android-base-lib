package com.hujiejeff.library.base.image

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale
import java.util.concurrent.Executors

object ImageUtil {
    private const val TAG = "BitmapUtil"
    private const val foldName = "DuteNews"
    private val handler = Handler(Looper.myLooper()!!)

    fun saveImageToAlbum(context: Context, url: String) {
        if (Build.VERSION.SDK_INT < 29) {
            XXPermissions.with(context)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .request(object: OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        if (!allGranted) {
                            return
                        }
                        saveBmpToAlbum(context.applicationContext, url)
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            Toast.makeText(context, "获取权限被拒绝，请手动授予", Toast.LENGTH_SHORT).show()
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(context, permissions)
                        } else {
                            Toast.makeText(context.applicationContext, "获取权限失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        } else {
            saveBmpToAlbum(context.applicationContext, url)
        }
    }


    @JvmStatic
    private fun saveBmpToAlbum(context: Context, uri: String) {
        val name = getStringMD5(uri)
        val executor = Executors.newSingleThreadExecutor()
        executor.execute(Runnable {
            try {
                val imageFile = getImageFile(context, uri)
                val imageType = getImageType(imageFile)
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    foldName
                )
                if (!dir.exists()) dir.mkdirs()
                val destFile = File(dir, "$name.$imageType")
                if (Build.VERSION.SDK_INT < 29) {
                    if (destFile.exists()) destFile.delete()
                    destFile.createNewFile()

                    FileOutputStream(destFile).use { out ->
                        FileInputStream(imageFile).use {
                            copyFile(it, out)
                        }
                    }
                    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    intent.data = Uri.parse("file://" + destFile.absolutePath)
                    context.sendBroadcast(intent)
                    handler.post {
                        Toast.makeText(context, "图片保存成功，请在相册查看", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    //android10以上，增加了新字段，自己insert，因为RELATIVE_PATH，DATE_EXPIRES，IS_PENDING是29新增字段
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, destFile.name)
                    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                    val contentUri: Uri =
                        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else {
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI
                        }
                    contentValues.put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_DCIM + "/" + foldName
                    )
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
                    val uri: Uri? = context.contentResolver.insert(contentUri, contentValues)
                    if (uri == null) {
                        Log.d(TAG, "Fail")
                        return@Runnable
                    }
                    val resolver = context.contentResolver
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(imageFile).use {
                            copyFile(it, out)
                        }
                    }
                    // Everything went well above, publish it!
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    //                            contentValues.putNull(MediaStore.MediaColumns.DATE_EXPIRES);
                    resolver.update(uri, contentValues, null, null)
                }
                Log.i(TAG, "Saved Success")
                handler.post {
                    Toast.makeText(context, "图片保存成功，请在相册查看", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(TAG, "Saved Fail")
            }
        })
    }

    fun copyFile(fis: InputStream, fos: OutputStream) {
        fis.use {
            fos.use {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.copy(fis, fos)
                } else {
                    writeFileFromIS(fis, fos)
                }
            }
        }
    }


    private fun writeFileFromIS(fis: InputStream, fos: OutputStream): Boolean {
        var os: OutputStream? = null
        return try {
            os = BufferedOutputStream(fos)
            val data = ByteArray(8192)
            var len: Int
            while (fis.read(data, 0, 8192).also { len = it } != -1) {
                os.write(data, 0, len)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            try {
                fis.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    @JvmStatic
    fun getImageFile(context: Context, uri: Any): File {
        try {
            return Glide.with(context).downloadOnly().load(uri).submit().get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        throw Exception()
    }


    fun getStringMD5(string: String): String? {
        if (TextUtils.isEmpty(string)) {
            return ""
        }
        val md5: MessageDigest
        try {
            md5 = MessageDigest.getInstance("MD5")
            val bytes = md5.digest(string.toByteArray())
            var result: String? = ""
            for (b in bytes) {
                var temp = Integer.toHexString(b.toInt() and 0xff).uppercase(Locale.getDefault())
                if (temp.length == 1) {
                    temp = "0$temp"
                }
                result += temp
            }
            return result
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    fun statusBarHeight(context: Context): Int {
        var height = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            height = context.resources.getDimensionPixelSize(resourceId)
        }
        return height
    }

    fun getImageType(file: File?): String {
        if (file == null) return ""
        var `is`: InputStream? = null
        try {
            `is` = FileInputStream(file)
            val bytes = ByteArray(12)
            if (`is`.read(bytes) != -1) {
                val type: String =
                    bytes2HexString(bytes, true).uppercase(Locale.getDefault())
                if (type.contains("FFD8FF")) {
                    return "jpg"
                } else if (type.contains("89504E47")) {
                    return "png"
                } else if (type.contains("47494638")) {
                    return "gif"
                } else if (type.contains("49492A00") || type.contains("4D4D002A")) {
                    return "tiff"
                } else if (type.contains("424D")) {
                    return "bmp"
                } else if (type.startsWith("52494646") && type.endsWith("57454250")) { //524946461c57000057454250-12个字节
                    return "webp"
                } else if (type.contains("00000100") || type.contains("00000200")) {
                    return "ico"
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return ""
    }

    private val HEX_DIGITS_UPPER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    private val HEX_DIGITS_LOWER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    fun bytes2HexString(bytes: ByteArray?, isUpperCase: Boolean): String {
        if (bytes == null) return ""
        val hexDigits = if (isUpperCase) HEX_DIGITS_UPPER else HEX_DIGITS_LOWER
        val len = bytes.size
        if (len <= 0) return ""
        val ret = CharArray(len shl 1)
        var i = 0
        var j = 0
        while (i < len) {
            ret[j++] = hexDigits[bytes[i].toInt() shr 4 and 0x0f]
            ret[j++] = hexDigits[bytes[i].toInt() and 0x0f]
            i++
        }
        return String(ret)
    }
}