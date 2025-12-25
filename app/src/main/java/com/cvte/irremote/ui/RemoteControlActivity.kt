package com.cvte.irremote.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cvte.irremote.R
import com.cvte.irremote.databinding.ActivityRemoteControlBinding
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.utils.PreferenceManager
import com.cvte.irremote.viewmodel.RemoteControlViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar

/**
 * 遥控器主界面 Activity
 * 
 * 显示遥控器按键布局，处理按键点击事件
 */
class RemoteControlActivity : AppCompatActivity() {
    
    /**
     * 特殊按键描述信息数据类
     */
    data class KeyDescription(
        val titleResId: Int,
        val descResId: Int,
        val usageResId: Int
    )

    private lateinit var binding: ActivityRemoteControlBinding
    private val viewModel: RemoteControlViewModel by viewModels()
    private lateinit var preferenceManager: PreferenceManager
    private var vibrator: Vibrator? = null
    
    // 顶部通知自动隐藏 Handler
    private val notificationHandler = Handler(Looper.getMainLooper())
    private val hideNotificationRunnable = Runnable {
        binding.tvTopNotification.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.tvTopNotification.visibility = View.GONE
            }
            .start()
    }
    
    // 特殊按键描述信息映射表
    private val specialKeys: Map<Int, KeyDescription> by lazy {
        mapOf(
            // 工厂测试键
            R.id.btnFacReset to KeyDescription(R.string.key_title_fac_reset, R.string.key_desc_fac_reset, R.string.key_usage_fac_reset),
            R.id.btnFacMenu to KeyDescription(R.string.key_title_fac_menu, R.string.key_desc_fac_menu, R.string.key_usage_fac_menu),
            R.id.btnFacVersion to KeyDescription(R.string.key_title_fac_version, R.string.key_desc_fac_version, R.string.key_usage_fac_version),
            R.id.btnFacAuto to KeyDescription(R.string.key_title_fac_auto, R.string.key_desc_fac_auto, R.string.key_usage_fac_auto),
            R.id.btnFacAging to KeyDescription(R.string.key_title_fac_aging, R.string.key_desc_fac_aging, R.string.key_usage_fac_aging),
            R.id.btnFacAdc to KeyDescription(R.string.key_title_fac_adc, R.string.key_desc_fac_adc, R.string.key_usage_fac_adc),
            R.id.btnFacHdcp to KeyDescription(R.string.key_title_fac_hdcp, R.string.key_desc_fac_hdcp, R.string.key_usage_fac_hdcp),
            R.id.btnFacMac to KeyDescription(R.string.key_title_fac_mac, R.string.key_desc_fac_mac, R.string.key_usage_fac_mac),
            R.id.btnFacCiplus to KeyDescription(R.string.key_title_fac_ciplus, R.string.key_desc_fac_ciplus, R.string.key_usage_fac_ciplus),
            R.id.btnFacF1 to KeyDescription(R.string.key_title_fac_f1, R.string.key_desc_fac_f1, R.string.key_usage_fac_f1),
            R.id.btnFacFnb to KeyDescription(R.string.key_title_fac_fnb, R.string.key_desc_fac_fnb, R.string.key_usage_fac_fnb),
            R.id.btnFacTouchpad to KeyDescription(R.string.key_title_fac_touchpad, R.string.key_desc_fac_touchpad, R.string.key_usage_fac_touchpad),
            // 功能键
            R.id.btnMedia to KeyDescription(R.string.key_title_media, R.string.key_desc_media, R.string.key_usage_media),
            R.id.btnPmode to KeyDescription(R.string.key_title_pmode, R.string.key_desc_pmode, R.string.key_usage_pmode),
            R.id.btnMts to KeyDescription(R.string.key_title_mts, R.string.key_desc_mts, R.string.key_usage_mts),
            R.id.btnRecord to KeyDescription(R.string.key_title_record, R.string.key_desc_record, R.string.key_usage_record),
            R.id.btnInfo to KeyDescription(R.string.key_title_info, R.string.key_desc_info, R.string.key_usage_info),
            R.id.btnKp1 to KeyDescription(R.string.key_title_kp1, R.string.key_desc_kp1, R.string.key_usage_kp1),
            R.id.btnHelp to KeyDescription(R.string.key_title_help, R.string.key_desc_help, R.string.key_usage_help),
            R.id.btnZoom to KeyDescription(R.string.key_title_zoom, R.string.key_desc_zoom, R.string.key_usage_zoom),
            R.id.btnSubtitle to KeyDescription(R.string.key_title_subtitle, R.string.key_desc_subtitle, R.string.key_usage_subtitle),
            // 彩色快捷键
            R.id.btnRed to KeyDescription(R.string.key_title_red, R.string.key_desc_red, R.string.key_usage_red),
            R.id.btnGreen to KeyDescription(R.string.key_title_green, R.string.key_desc_green, R.string.key_usage_green),
            R.id.btnYellow to KeyDescription(R.string.key_title_yellow, R.string.key_desc_yellow, R.string.key_usage_yellow),
            R.id.btnBlue to KeyDescription(R.string.key_title_blue, R.string.key_desc_blue, R.string.key_usage_blue)
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoteControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager.getInstance(this)
        initVibrator()
        setupViews()
        setupSpecialKeyListeners()
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
     * 设置特殊按键长按监听器
     */
    private fun setupSpecialKeyListeners() {
        specialKeys.forEach { (viewId, keyDescription) ->
            findViewById<View>(viewId)?.setOnLongClickListener {
                showKeyDescriptionDialog(keyDescription)
                true
            }
        }
    }
    
    /**
     * 显示按键描述对话框
     */
    private fun showKeyDescriptionDialog(keyDescription: KeyDescription) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_key_description, null)
        
        view.findViewById<TextView>(R.id.tvKeyTitle)?.text = getString(keyDescription.titleResId)
        view.findViewById<TextView>(R.id.tvKeyDescription)?.text = getString(keyDescription.descResId)
        view.findViewById<TextView>(R.id.tvKeyUsage)?.text = getString(keyDescription.usageResId)
        
        dialog.setContentView(view)
        dialog.show()
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
                
                // 显示顶部通知
                showTopNotification(message)
                
                // 同步更新底部状态
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
            // 同步显示顶部通知（用于同步状态等）
            showTopNotification(message)
        }
    }
    
    /**
     * 显示顶部通知栏（自动消失）
     */
    private fun showTopNotification(message: String) {
        // 移除之前的隐藏任务
        notificationHandler.removeCallbacks(hideNotificationRunnable)
        
        // 设置文本并显示
        binding.tvTopNotification.text = message
        binding.tvTopNotification.alpha = 1f
        binding.tvTopNotification.visibility = View.VISIBLE
        
        // 2秒后自动隐藏
        notificationHandler.postDelayed(hideNotificationRunnable, 2000)
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
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.config_list_title)
            .setPositiveButton(R.string.config_editor_title) { _, _ ->
                // 打开配置编辑器，传入当前配置ID
                val intent = Intent(this, ConfigEditorActivity::class.java)
                viewModel.currentConfig.value?.let { config ->
                    intent.putExtra(ConfigEditorActivity.EXTRA_CONFIG_ID, config.id)
                }
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        
        // 使用自定义 ListView 支持长按和滚动
        val listView = android.widget.ListView(this)
        listView.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, configNames)
        
        // 单击切换配置
        listView.setOnItemClickListener { _, _, position, _ ->
            viewModel.switchConfig(configs[position])
            dialog.dismiss()
        }
        
        // 长按显示删除选项
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val config = configs[position]
            if (config.isDefault) {
                Toast.makeText(this, "无法删除默认配置", Toast.LENGTH_SHORT).show()
            } else {
                showDeleteConfigDialog(config, dialog)
            }
            true
        }
        
        // 将 ListView 放入容器并设置固定最大高度
        val container = android.widget.FrameLayout(this)
        container.addView(listView)
        
        // 设置最大高度（约6个项目）
        val itemHeight = (48 * resources.displayMetrics.density).toInt()
        val maxHeight = 6 * itemHeight
        val actualHeight = if (configs.size > 6) maxHeight else configs.size * itemHeight
        
        val padding = (16 * resources.displayMetrics.density).toInt()
        dialog.setView(container, padding, padding, padding, padding)
        dialog.show()
        
        // 在 dialog 显示后设置 ListView 高度
        listView.layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            actualHeight
        )
    }
    
    /**
     * 显示删除配置确认对话框
     */
    private fun showDeleteConfigDialog(config: IRConfig, parentDialog: AlertDialog) {
        AlertDialog.Builder(this)
            .setTitle("删除配置")
            .setMessage("确定要删除配置 \"${config.name}\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                val repository = com.cvte.irremote.model.repository.ConfigRepository.getInstance(this)
                if (repository.deleteConfig(config.id)) {
                    Toast.makeText(this, "配置已删除", Toast.LENGTH_SHORT).show()
                    parentDialog.dismiss()
                    // 重新显示更新后的列表
                    showConfigDialog()
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
                }
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
