package com.cvte.irremote.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cvte.irremote.databinding.ItemConfigBinding
import com.cvte.irremote.model.entity.IRConfig

/**
 * 配置列表适配器
 */
class ConfigListAdapter(
    private val onItemClick: (IRConfig) -> Unit,
    private val onEditClick: (IRConfig) -> Unit,
    private val onDeleteClick: (IRConfig) -> Unit
) : ListAdapter<IRConfig, ConfigListAdapter.ConfigViewHolder>(ConfigDiffCallback()) {
    
    private var currentConfigId: String? = null
    
    fun setCurrentConfigId(configId: String?) {
        val oldId = currentConfigId
        currentConfigId = configId
        
        // 刷新变化的item
        currentList.forEachIndexed { index, config ->
            if (config.id == oldId || config.id == configId) {
                notifyItemChanged(index)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigViewHolder {
        val binding = ItemConfigBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConfigViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ConfigViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ConfigViewHolder(
        private val binding: ItemConfigBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(config: IRConfig) {
            val isSelected = config.id == currentConfigId
            
            binding.tvConfigName.text = config.name
            binding.tvProtocol.text = "协议: ${IRConfig.getProtocolName(config.protocol)}"
            binding.tvKeyCount.text = "按键: ${config.keys.size}"
            
            // 默认标签
            binding.tvDefault.visibility = if (config.isDefault) View.VISIBLE else View.GONE
            
            // 选中状态
            binding.ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.cardConfig.isChecked = isSelected
            
            // 点击事件
            binding.root.setOnClickListener {
                onItemClick(config)
            }
            
            binding.btnEdit.setOnClickListener {
                onEditClick(config)
            }
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(config)
            }
            
            // 默认配置不能删除
            binding.btnDelete.visibility = if (config.isDefault) View.GONE else View.VISIBLE
        }
    }
    
    private class ConfigDiffCallback : DiffUtil.ItemCallback<IRConfig>() {
        override fun areItemsTheSame(oldItem: IRConfig, newItem: IRConfig): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: IRConfig, newItem: IRConfig): Boolean {
            return oldItem == newItem
        }
    }
}
