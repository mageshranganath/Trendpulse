package com.trendpulse.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trendpulse.app.databinding.ItemRateBinding
import com.trendpulse.app.models.CurrencyRate

class RateAdapter : ListAdapter<CurrencyRate, RateAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CurrencyRate>() {
            override fun areItemsTheSame(a: CurrencyRate, b: CurrencyRate) = a.date == b.date
            override fun areContentsTheSame(a: CurrencyRate, b: CurrencyRate) = a == b
        }
        private val ROW_EVEN = Color.parseColor("#F0F4FF")
        private val ROW_ODD  = Color.WHITE
    }

    inner class ViewHolder(private val binding: ItemRateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CurrencyRate, position: Int) {
            binding.textDate.text = item.date
            binding.textRate.text = "%.4f".format(item.rate)
            binding.root.setBackgroundColor(if (position % 2 == 0) ROW_EVEN else ROW_ODD)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRateBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position)
}
