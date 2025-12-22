package com.cvte.irremote.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cvte.irremote.databinding.ItemKeyBinding
import com.cvte.irremote.model.entity.IRKey

/**
 * 按键列表适配器
 */
class KeyListAdapter(
    private val onEditClick: (IRKey, Int) -> Unit,
    private val onDeleteClick: (IRKey, Int) -> Unit
) : ListAdapter<IRKey, KeyListAdapter.KeyViewHolder>(KeyDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyViewHolder {
        val binding = ItemKeyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KeyViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: KeyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class KeyViewHolder(
        private val binding: ItemKeyBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(key: IRKey) {
            binding.tvKeyName.text = key.keyName
            binding.tvDisplayName.text = key.displayName
            binding.tvKeyCode.text = key.getFormattedKeyCode()
            
            binding.btnEdit.setOnClickListener {
                onEditClick(key, adapterPosition)
            }
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(key, adapterPosition)
            }
        }
    }
    
    private class KeyDiffCallback : DiffUtil.ItemCallback<IRKey>() {
        override fun areItemsTheSame(oldItem: IRKey, newItem: IRKey): Boolean {
            return oldItem.keyName == newItem.keyName
        }
        
        override fun areContentsTheSame(oldItem: IRKey, newItem: IRKey): Boolean {
            return oldItem == newItem
        }
    }
}
