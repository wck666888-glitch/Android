package com.cvte.irremote.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cvte.irremote.R
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * 自定义扫码界面 (仿微信风格)
 */
class CustomScannerActivity : AppCompatActivity() {

    private lateinit var capture: CaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private lateinit var btnBack: ImageView
    private lateinit var btnAlbum: TextView
    private lateinit var ivScanLine: ImageView

    // 相册选择器
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val result = decodeQRCode(bitmap)
                
                if (result != null) {
                    // 成功解析，返回结果
                    val intent = Intent()
                    intent.putExtra("SCAN_RESULT", result)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(this, "未发现二维码", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "图片解析失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scanner)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)
        btnBack = findViewById(R.id.btn_back)
        btnAlbum = findViewById(R.id.btn_album)
        ivScanLine = findViewById(R.id.iv_scan_line)

        // 初始化扫码管理器
        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        // 设置点击事件
        btnBack.setOnClickListener {
            finish()
        }

        btnAlbum.setOnClickListener {
            getContent.launch("image/*")
        }

        // 启动扫描线动画
        startScanAnimation()
    }

    private fun startScanAnimation() {
        ivScanLine.visibility = android.view.View.VISIBLE
        // 调整动画范围：从顶部 (0.0) 到底部 (1.0)
        val animation = android.view.animation.TranslateAnimation(
            android.view.animation.Animation.RELATIVE_TO_PARENT, 0.0f,
            android.view.animation.Animation.RELATIVE_TO_PARENT, 0.0f,
            android.view.animation.Animation.RELATIVE_TO_PARENT, 0.3f,
            android.view.animation.Animation.RELATIVE_TO_PARENT, 0.7f
        )
        animation.duration = 3000 // 稍微放慢速度适配全屏
        animation.repeatCount = android.view.animation.Animation.INFINITE
        animation.repeatMode = android.view.animation.Animation.RESTART
        animation.interpolator = android.view.animation.LinearInterpolator()
        ivScanLine.startAnimation(animation)
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 解析图片中的二维码
     */
    private fun decodeQRCode(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }
}
