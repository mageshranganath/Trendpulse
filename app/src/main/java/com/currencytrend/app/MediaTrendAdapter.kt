package com.trendpulse.app

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trendpulse.app.databinding.ItemRateBinding

class MediaTrendAdapter : ListAdapter<MediaTrendItem, MediaTrendAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MediaTrendItem>() {
            override fun areItemsTheSame(a: MediaTrendItem, b: MediaTrendItem): Boolean =
                a.title == b.title && a.subtitle == b.subtitle

            override fun areContentsTheSame(a: MediaTrendItem, b: MediaTrendItem): Boolean = a == b
        }

        private val ROW_EVEN = Color.parseColor("#F0F4FF")
        private val ROW_ODD = Color.WHITE
    }

    inner class ViewHolder(private val binding: ItemRateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaTrendItem, position: Int) {
            binding.textDate.text = item.title
            binding.textRate.text = item.subtitle
            binding.textRate.gravity = Gravity.START
            binding.textRate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorTextPrimary))
            binding.textRate.setTypeface(null, android.graphics.Typeface.NORMAL)
            val colorRes = if (position % 2 == 0) R.color.colorRowEven else R.color.colorRowOdd
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, colorRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemRateBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}
