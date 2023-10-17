package com.hujiejeff.library.base.qrcode

import android.graphics.Bitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import com.king.zxing.DecodeFormatManager

object ZxingHelper {

    /**
     * 解析多个二维码
     */
    @JvmStatic
    fun decodeMultiQR(srcBitmap: Bitmap): Array<Result> {
        var result: Array<Result> = emptyArray()
        val width = srcBitmap.width
        val height = srcBitmap.height
        val pixels = IntArray(width * height)
        srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        // 新建一个RGBLuminanceSource对象
        val source = RGBLuminanceSource(width, height, pixels)
        // 将图片转换成二进制图片
        val binaryBitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
        val reader = QRCodeMultiReader() // 初始化解析对象
        try {
            result = reader.decodeMultiple(
                binaryBitmap,
                DecodeFormatManager.ALL_HINTS
            ) // 解析获取一个Result数组
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
        return result
    }

    /*// 生成二维码
    CodeUtils.createQRCode(content,600,logo);
    // 生成条形码
    CodeUtils.createBarCode(content, BarcodeFormat.CODE_128,800,200);
    // 解析条形码/二维码
    CodeUtils.parseCode(bitmap);
    // 解析二维码
    CodeUtils.parseQRCode(bitmap);*/
}