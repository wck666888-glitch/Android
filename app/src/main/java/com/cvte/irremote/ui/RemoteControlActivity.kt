package com.cvte.irremote.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cvte.irremote.R
import com.cvte.irremote.databinding.ActivityRemoteControlBinding
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.utils.PreferenceManager
import com.cvte.irremote.viewmodel.RemoteControlViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * 遥控器主界面 Activity
 * 
 * 显示遥控器按键布局，处理按键点击事件
 */
class RemoteControlActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRemoteControlBinding
    private val viewModel: RemoteControlViewModel by viewModels()
    private lateinit var preferenceManager: PreferenceManager
    private var vibrator: Vibrator? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoteControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager.getInstance(this)
        initVibrator()
        setupViews()
        observeViewModel()
    }
    
    /**
     * 初始化震动器
     */
    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    /**
     * 设置视图和点击事件
     */
    private fun setupViews() {
        // 设置按钮
        binding.btnSettings.setOnClickListener { view ->
            showSettingsMenu(view)
        }
        
        // 电源键
        binding.btnPower.setOnClickListener { 
            vibrateAndEmit { viewModel.emitPower() }
        }
        
        // 数字键
        binding.btn0.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(0) } }
        binding.btn1.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(1) } }
        binding.btn2.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(2) } }
        binding.btn3.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(3) } }
        binding.btn4.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(4) } }
        binding.btn5.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(5) } }
        binding.btn6.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(6) } }
        binding.btn7.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(7) } }
        binding.btn8.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(8) } }
        binding.btn9.setOnClickListener { vibrateAndEmit { viewModel.emitNumber(9) } }
        
        // 方向键
        binding.btnUp.setOnClickListener { vibrateAndEmit { viewModel.emitUp() } }
        binding.btnDown.setOnClickListener { vibrateAndEmit { viewModel.emitDown() } }
        binding.btnLeft.setOnClickListener { vibrateAndEmit { viewModel.emitLeft() } }
        binding.btnRight.setOnClickListener { vibrateAndEmit { viewModel.emitRight() } }
        binding.btnOk.setOnClickListener { vibrateAndEmit { viewModel.emitOk() } }
        
        // 音量/频道键
        binding.btnVolUp.setOnClickListener { vibrateAndEmit { viewModel.emitVolumeUp() } }
        binding.btnVolDown.setOnClickListener { vibrateAndEmit { viewModel.emitVolumeDown() } }
        binding.btnMute.setOnClickListener { vibrateAndEmit { viewModel.emitMute() } }
        binding.btnChUp.setOnClickListener { vibrateAndEmit { viewModel.emitChannelUp() } }
        binding.btnChDown.setOnClickListener { vibrateAndEmit { viewModel.emitChannelDown() } }
        
        // 功能键
        binding.btnMenu.setOnClickListener { vibrateAndEmit { viewModel.emitMenu() } }
        binding.btnBack.setOnClickListener { vibrateAndEmit { viewModel.emitBack() } }
        binding.btnHome.setOnClickListener { vibrateAndEmit { viewModel.emitHome() } }
        binding.btnInfo.setOnClickListener { vibrateAndEmit { viewModel.emitInfo() } }
        binding.btnSubtitle.setOnClickListener { vibrateAndEmit { viewModel.emitSubtitle() } }
        
        // 颜色快捷键
        binding.btnRed.setOnClickListener { vibrateAndEmit { viewModel.emitRed() } }
        binding.btnGreen.setOnClickListener { vibrateAndEmit { viewModel.emitGreen() } }
        binding.btnYellow.setOnClickListener { vibrateAndEmit { viewModel.emitYellow() } }
        binding.btnBlue.setOnClickListener { vibrateAndEmit { viewModel.emitBlue() } }
        
        // 媒体/功能键
        binding.btnMedia.setOnClickListener { vibrateAndEmit { viewModel.emitMedia() } }
        binding.btnPmode.setOnClickListener { vibrateAndEmit { viewModel.emitPictureMode() } }
        binding.btnMts.setOnClickListener { vibrateAndEmit { viewModel.emitMts() } }
        binding.btnRecord.setOnClickListener { vibrateAndEmit { viewModel.emitRecord() } }
        
        // 辅助功能键
        binding.btnHelp.setOnClickListener { vibrateAndEmit { viewModel.emitHelp() } }
        binding.btnZoom.setOnClickListener { vibrateAndEmit { viewModel.emitZoom() } }
        binding.btnKp1.setOnClickListener { vibrateAndEmit { viewModel.emitKp1() } }
        
        // 工厂测试键
        binding.btnFacReset.setOnClickListener { vibrateAndEmit { viewModel.emitFacReset() } }
        binding.btnFacMenu.setOnClickListener { vibrateAndEmit { viewModel.emitFacMenu() } }
        binding.btnFacVersion.setOnClickListener { vibrateAndEmit { viewModel.emitFacVersion() } }
        binding.btnFacAuto.setOnClickListener { vibrateAndEmit { viewModel.emitFacAutoTuning() } }
        binding.btnFacAging.setOnClickListener { vibrateAndEmit { viewModel.emitFacAging() } }
        binding.btnFacAdc.setOnClickListener { vibrateAndEmit { viewModel.emitFacAdc() } }
        binding.btnFacHdcp.setOnClickListener { vibrateAndEmit { viewModel.emitFacEraseHdcp() } }
        binding.btnFacMac.setOnClickListener { vibrateAndEmit { viewModel.emitFacEraseMac() } }
        binding.btnFacCiplus.setOnClickListener { vibrateAndEmit { viewModel.emitFacEraseCiplus() } }
        binding.btnFacF1.setOnClickListener { vibrateAndEmit { viewModel.emitFacF1() } }
        binding.btnFacFnb.setOnClickListener { vibrateAndEmit { viewModel.emitFacFnB() } }
        binding.btnFacTouchpad.setOnClickListener { vibrateAndEmit { viewModel.emitFacTouchpad() } }
    }
    
    /**
     * 震动并执行发射
     */
    private fun vibrateAndEmit(emitAction: () -> Unit) {
        // 触觉反馈
        if (preferenceManager.isVibrateOnEmit()) {
            vibrate()
        }
        
        // 执行发射
        emitAction()
    }
    
    /**
     * 触发震动
     */
    private fun vibrate() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(30)
            }
        }
    }
    
    /**
     * 观察 ViewModel 状态
     */
    private fun observeViewModel() {
        // 观察配置变化
        viewModel.currentConfig.observe(this) { config ->
            config?.let {
                binding.tvConfigName.text = it.name
            }
        }
        
        // 观察发射结果
        viewModel.emitResult.observe(this) { result ->
            if (preferenceManager.isShowToast()) {
                val message = if (result.success) {
                    getString(R.string.msg_ir_sent, result.keyName)
                } else {
                    "${result.keyName}: ${result.message}"
                }
                
                if (result.success) {
                    binding.tvStatus.text = message
                } else {
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        
        // 观察IR支持状态
        viewModel.isIRSupported.observe(this) { supported ->
            if (!supported) {
                showIRNotSupportedWarning()
            }
        }
        
        // 观察状态消息
        viewModel.statusMessage.observe(this) { message ->
            binding.tvStatus.text = message
        }
    }
    
    /**
     * 显示IR不支持警告
     */
    private fun showIRNotSupportedWarning() {
        AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(R.string.msg_ir_not_supported)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        
        binding.tvStatus.text = getString(R.string.msg_ir_not_supported)
    }
    
    /**
     * 显示配置选择对话框
     */
    private fun showConfigDialog() {
        val configs = viewModel.getAllConfigs()
        val configNames = configs.map { it.name }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle(R.string.config_list_title)
            .setItems(configNames) { _, which ->
                viewModel.switchConfig(configs[which])
            }
            .setPositiveButton(R.string.config_editor_title) { _, _ ->
                // 打开配置编辑器
                startActivity(Intent(this, ConfigEditorActivity::class.java))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    /**
     * 显示设置菜单
     */
    /**
     * 显示设置菜单
     */
    private fun showSettingsMenu(view: View) {
        val popup = android.widget.PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_remote, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_config -> {
                    showConfigDialog()
                    true
                }
                R.id.action_sync -> {
                    viewModel.syncConfigs()
                    true
                }
                R.id.action_scan_qr -> {
                    startQRScanner()
                    true
                }
                R.id.action_about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    /**
     * 启动二维码扫描
     */
    private fun startQRScanner() {
        val integrator = com.google.zxing.integration.android.IntentIntegrator(this)
        integrator.setCaptureActivity(CustomScannerActivity::class.java)
        integrator.setDesiredBarcodeFormats(com.google.zxing.integration.android.IntentIntegrator.QR_CODE)
        integrator.setPrompt("请扫描红外配置二维码")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = com.google.zxing.integration.android.IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // 处理扫描结果
                viewModel.fetchAndApplyConfig(result.contents)
            } else {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    
    /**
     * 显示关于对话框
     */
    private fun showAboutDialog() {
        val version = packageManager.getPackageInfo(packageName, 0).versionName
        
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(
                getString(R.string.about_version, version) + "\n\n" +
                getString(R.string.about_description)
            )
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
