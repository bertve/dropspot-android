package com.example.dropspot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.databinding.ListItemSpotDetailBinding
import com.example.dropspot.fragments.MeFragmentDirections

class SpotDetailsAdapter :
    ListAdapter<SpotDetail, SpotDetailsAdapter.SpotDetailViewHolder>(SpotDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotDetailViewHolder {
        return SpotDetailViewHolder(
            ListItemSpotDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SpotDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SpotDetailViewHolder(private val binding: ListItemSpotDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.setClickListener {
                binding.spotDetail?.let { spotDetail ->
                    navigateToSpotDetail(spotDetail, it)
                }
            }
        }

        private fun navigateToSpotDetail(spotDetail: SpotDetail, v: View) {
            val direction =
                MeFragmentDirections.actionMeFragmentToSpotDetailFragment(spotDetail.spotId)
            v.findNavController().navigate(direction)
        }

        fun bind(spotDetail: SpotDetail) {
            binding.spotDetail = spotDetail
        }
    }

    class SpotDetailDiffCallback : DiffUtil.ItemCallback<SpotDetail>() {
        override fun areItemsTheSame(oldItem: SpotDetail, newItem: SpotDetail): Boolean {
            return oldItem.spotId == newItem.spotId
        }

        override fun areContentsTheSame(oldItem: SpotDetail, newItem: SpotDetail): Boolean {
            return oldItem == newItem
        }
    }
}
