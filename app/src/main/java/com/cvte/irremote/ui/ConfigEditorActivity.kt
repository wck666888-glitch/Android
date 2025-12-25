package com.cvte.irremote.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.cvte.irremote.R
import com.cvte.irremote.databinding.ActivityConfigEditorBinding
import com.cvte.irremote.model.entity.IRKey
import com.cvte.irremote.viewmodel.ConfigEditorViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

/**
 * 配置编辑器 Activity
 * 
 * 用于编辑和管理IR配置
 */
class ConfigEditorActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_CONFIG_ID = "config_id"
    }
    
    private lateinit var binding: ActivityConfigEditorBinding
    private val viewModel: ConfigEditorViewModel by viewModels()
    private lateinit var keyAdapter: KeyListAdapter
    
    // SAF file picker for export
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { saveConfigToUri(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupViews()
        observeViewModel()
        
        // 加载配置
        val configId = intent.getStringExtra(EXTRA_CONFIG_ID)
        viewModel.loadConfig(configId)
    }
    
    /**
     * 设置工具栏
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    
    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        keyAdapter = KeyListAdapter(
            onEditClick = { key, position ->
                showKeyEditDialog(key, position)
            },
            onDeleteClick = { _, position ->
                showDeleteConfirmDialog(position)
            }
        )
        
        binding.rvKeys.apply {
            layoutManager = LinearLayoutManager(this@ConfigEditorActivity)
            adapter = keyAdapter
        }
    }
    
    /**
     * 设置视图
     */
    private fun setupViews() {
        // 添加按键
        binding.btnAddKey.setOnClickListener {
            showKeyEditDialog(null, -1)
        }
        
        // 保存配置
        binding.btnSave.setOnClickListener {
            saveConfig()
        }
        
        // 导入配置
        binding.btnImport.setOnClickListener {
            showImportDialog()
        }
        
        // 导出配置
        binding.btnExport.setOnClickListener {
            exportConfig()
        }
    }
    
    /**
     * 观察ViewModel
     */
    private fun observeViewModel() {
        // 观察配置变化
        viewModel.config.observe(this) { config ->
            config?.let {
                binding.etConfigName.setText(it.name)
                binding.etProtocol.setText(it.getFormattedProtocol())
                binding.etHeader.setText(it.getFormattedHeader())
            }
        }
        
        // 观察按键列表
        viewModel.keys.observe(this) { keys ->
            keyAdapter.submitList(keys)
        }
        
        // 观察保存结果
        viewModel.saveResult.observe(this) { success ->
            if (success) {
                Snackbar.make(binding.root, R.string.msg_config_saved, Snackbar.LENGTH_SHORT).show()
            }
        }
        
        // 观察消息
        viewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 保存配置
     */
    private fun saveConfig() {
        val name = binding.etConfigName.text?.toString() ?: ""
        val protocolStr = binding.etProtocol.text?.toString() ?: "0x01"
        val headerStr = binding.etHeader.text?.toString() ?: "0x8890"
        
        val protocol = try {
            IRKey.parseKeyCode(protocolStr)
        } catch (e: Exception) {
            Toast.makeText(this, "协议格式错误", Toast.LENGTH_SHORT).show()
            return
        }
        
        val header = try {
            IRKey.parseKeyCode(headerStr)
        } catch (e: Exception) {
            Toast.makeText(this, "Header格式错误", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.saveConfig(name, protocol, header)
    }
    
    /**
     * 显示按键编辑对话框
     */
    private fun showKeyEditDialog(key: IRKey?, position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_key_edit, null)
        val etKeyName = dialogView.findViewById<TextInputEditText>(R.id.etKeyName)
        val etKeyCode = dialogView.findViewById<TextInputEditText>(R.id.etKeyCode)
        val etDisplayName = dialogView.findViewById<TextInputEditText>(R.id.etDisplayName)
        
        // 填充现有数据
        key?.let {
            etKeyName.setText(it.keyName)
            etKeyCode.setText(it.getFormattedKeyCode())
            etDisplayName.setText(it.displayName)
        }
        
        val title = if (key == null) R.string.config_add_key else R.string.key_name
        
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                val keyName = etKeyName.text?.toString() ?: ""
                val keyCodeStr = etKeyCode.text?.toString() ?: ""
                val displayName = etDisplayName.text?.toString() ?: ""
                
                val keyCode = try {
                    IRKey.parseKeyCode(keyCodeStr)
                } catch (e: Exception) {
                    Toast.makeText(this, R.string.error_invalid_code, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (key == null) {
                    viewModel.addKey(keyName, keyCode, displayName)
                } else {
                    viewModel.updateKey(position, keyName, keyCode, displayName)
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
    
    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.config_delete)
            .setMessage("确定要删除这个按键吗？")
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                viewModel.deleteKey(position)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
    
    /**
     * 显示导入对话框
     */
    private fun showImportDialog() {
        val editText = EditText(this).apply {
            hint = "粘贴JSON配置"
            minLines = 5
            maxLines = 10
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.config_import)
            .setView(editText)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                val json = editText.text.toString()
                if (json.isNotBlank()) {
                    viewModel.importConfig(json)
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
    
    /**
     * 导出配置
     */
    private fun exportConfig() {
        val config = viewModel.config.value
        if (config == null) {
            Toast.makeText(this, "没有可导出的配置", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示导出选项对话框
        AlertDialog.Builder(this)
            .setTitle(R.string.config_export)
            .setItems(arrayOf("复制到剪贴板", "保存到文件")) { _, which ->
                when (which) {
                    0 -> exportToClipboard()
                    1 -> exportToFile(config.name)
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
    
    /**
     * 导出到剪贴板
     */
    private fun exportToClipboard() {
        val json = viewModel.exportConfig()
        if (json != null) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("IR Config", json)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, R.string.msg_config_exported, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 导出到文件
     */
    private fun exportToFile(configName: String) {
        val fileName = "${configName.replace(" ", "_")}.json"
        createDocumentLauncher.launch(fileName)
    }
    
    /**
     * 保存配置到指定 URI
     */
    private fun saveConfigToUri(uri: Uri) {
        val json = viewModel.exportConfig()
        if (json == null) {
            Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            Toast.makeText(this, "配置已保存到文件", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

