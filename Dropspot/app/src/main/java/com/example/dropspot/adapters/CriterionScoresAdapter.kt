package com.example.dropspot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dropspot.R
import com.example.dropspot.data.model.CriterionScore
import com.example.dropspot.databinding.ListItemRatingBinding
import com.example.dropspot.utils.Variables
import com.example.dropspot.viewmodels.SpotDetailViewModel
import com.google.android.material.snackbar.Snackbar

class CriterionScoresAdapter(
    private val spotDetailViewModel: SpotDetailViewModel,
    private val context: Context
) :
    ListAdapter<CriterionScore, CriterionScoresAdapter.CriterionScoreViewHolder>(
        CriterionScoreDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CriterionScoreViewHolder {
        return CriterionScoreViewHolder(
            ListItemRatingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CriterionScoreViewHolder, position: Int) {
        val criterionScore = getItem(position)
        holder.bind(criterionScore, spotDetailViewModel, context)
    }

    class CriterionScoreViewHolder(
        private val binding: ListItemRatingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            criterionScore: CriterionScore,
            spotDetailViewModel: SpotDetailViewModel,
            context: Context
        ) {
            binding.criterionScore = criterionScore
            binding.ratingBar.onRatingBarChangeListener =
                RatingBar.OnRatingBarChangeListener { _, rating, fromUser ->
                    if (fromUser) {
                        if (Variables.isNetworkConnected.value!!) {
                            spotDetailViewModel.vote(criterionScore.criterionId, rating.toDouble())
                        } else {
                            Snackbar.make(
                                binding.root,
                                context.resources.getString(R.string.vote_failed) +
                                    context.resources.getString(R.string.no_connection),
                                Snackbar.LENGTH_SHORT
                            ).show()
                            binding.ratingBar.rating = 0f
                        }
                    }
                }
        }
    }

    class CriterionScoreDiffCallback : DiffUtil.ItemCallback<CriterionScore>() {
        override fun areItemsTheSame(oldItem: CriterionScore, newItem: CriterionScore): Boolean {
            return oldItem.criterionId == newItem.criterionId
        }

        override fun areContentsTheSame(oldItem: CriterionScore, newItem: CriterionScore): Boolean {
            return oldItem == newItem
        }
    }
}
