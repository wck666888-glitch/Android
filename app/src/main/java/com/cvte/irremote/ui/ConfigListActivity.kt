package com.cvte.irremote.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cvte.irremote.R
import com.cvte.irremote.databinding.ActivityConfigListBinding
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.viewmodel.ConfigListViewModel

/**
 * 配置列表 Activity
 * 
 * 显示所有可用的IR配置，支持切换、编辑、删除
 */
class ConfigListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConfigListBinding
    private val viewModel: ConfigListViewModel by viewModels()
    private lateinit var configAdapter: ConfigListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupViews()
        observeViewModel()
        
        // 加载配置列表
        viewModel.loadConfigs()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadConfigs()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        configAdapter = ConfigListAdapter(
            onItemClick = { config ->
                // 切换到该配置
                viewModel.selectConfig(config)
                setResult(RESULT_OK)
                finish()
            },
            onEditClick = { config ->
                // 编辑配置
                val intent = Intent(this, ConfigEditorActivity::class.java)
                intent.putExtra(ConfigEditorActivity.EXTRA_CONFIG_ID, config.id)
                startActivity(intent)
            },
            onDeleteClick = { config ->
                showDeleteConfirmDialog(config)
            }
        )
        
        binding.rvConfigs.apply {
            layoutManager = LinearLayoutManager(this@ConfigListActivity)
            adapter = configAdapter
        }
    }
    
    private fun setupViews() {
        // 添加新配置
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, ConfigEditorActivity::class.java))
        }
        
        // 同步远程配置
        binding.btnSync.setOnClickListener {
            viewModel.syncRemoteConfigs()
        }
    }
    
    private fun observeViewModel() {
        viewModel.configs.observe(this) { configs ->
            configAdapter.submitList(configs)
        }
        
        viewModel.currentConfigId.observe(this) { currentId ->
            configAdapter.setCurrentConfigId(currentId)
        }
        
        viewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        
        viewModel.isLoading.observe(this) { loading ->
            // 可以显示加载状态
        }
    }
    
    private fun showDeleteConfirmDialog(config: IRConfig) {
        if (config.isDefault) {
            Toast.makeText(this, "无法删除默认配置", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.config_delete)
            .setMessage("确定要删除配置 \"${config.name}\" 吗？")
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                viewModel.deleteConfig(config.id)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
}
