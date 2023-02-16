package com.example.dropspot.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dropspot.R
import com.example.dropspot.activities.MainActivity
import com.example.dropspot.adapters.CriterionScoresAdapter
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.databinding.FragmentSpotDetailBinding
import com.example.dropspot.utils.Variables
import com.example.dropspot.viewmodels.SpotDetailViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class SpotDetailFragment : Fragment() {

    companion object {
        private val TAG = "spot_detail_fragment"
    }

    private lateinit var binding: FragmentSpotDetailBinding
    private val spotDetailViewModel: SpotDetailViewModel by viewModel()
    private val args: SpotDetailFragmentArgs by navArgs()
    private lateinit var criterionScoresAdapter: CriterionScoresAdapter
    private var currentSpotDetail: SpotDetail? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpotDetailBinding.inflate(inflater)
        binding.vm = spotDetailViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        loadSpotDetail()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        criterionScoresAdapter =
            CriterionScoresAdapter(
                spotDetailViewModel,
                requireContext()
            )
        binding.ratingList.adapter = criterionScoresAdapter

        // maps navigate intent
        binding.navigateIcon.setOnClickListener {
            currentSpotDetail?.let {
                val gmmIntentUri = Uri.parse(
                    "google.navigation:q=${this.currentSpotDetail!!.latitude}" +
                        ",${this.currentSpotDetail!!.longitude}"
                )

                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

                mapIntent.setPackage("com.google.android.apps.maps")

                mapIntent.resolveActivity(requireActivity().packageManager)?.let {
                    startActivity(mapIntent)
                }
            }
        }

        // de/favorite spot
        binding.likeIcon.setOnClickListener {
            currentSpotDetail?.let {
                if (!it.liked) favoriteSpot(it) else unFavoriteSpot(it)
            }
        }

        // vote response
        spotDetailViewModel.voteSuccess.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    if (it.success) Snackbar.make(requireView(), it.message, Snackbar.LENGTH_SHORT)
                        .show()
                    else Snackbar.make(
                        requireView(),
                        resources.getString(R.string.vote_failed) +
                            it.message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        )

        // favor responses
        spotDetailViewModel.favoriteSuccess.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    if (!it.success) Snackbar.make(
                        requireView(),
                        resources.getString(R.string.favor_failed) +
                            it.message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        )

        spotDetailViewModel.unFavoriteSuccess.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    if (!it.success) Snackbar.make(
                        requireView(),
                        resources.getString(R.string.un_favor_failed) +
                            it.message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        )

        // delete response
        spotDetailViewModel.deleteSuccess.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    if (it.success) {
                        findNavController().navigateUp()
                    } else {
                        Snackbar
                            .make(
                                requireView(),
                                resources.getString(R.string.delete_failed) +
                                    it.message,
                                Snackbar.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            }
        )
    }

    private fun unFavoriteSpot(spotDetail: SpotDetail) {
        if (Variables.isNetworkConnected.value!!) {
            spotDetailViewModel.unFavoriteSpot(spotDetail)
        } else {
            Snackbar.make(
                requireView(),
                resources.getString(R.string.un_favor_failed) +
                    requireContext().resources.getString(R.string.no_connection),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun favoriteSpot(spotDetail: SpotDetail) {
        if (Variables.isNetworkConnected.value!!) {
            spotDetailViewModel.favoriteSpot(spotDetail)
        } else {
            Snackbar.make(
                requireView(),
                resources.getString(R.string.favor_failed) +
                    requireContext().resources.getString(R.string.no_connection),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadSpotDetail() {
        Log.i(TAG, "loading spotDetail")
        val spotId = args.spotId
        spotDetailViewModel.setSpotId(spotId)
        val liveData = spotDetailViewModel.getSpotDetail()
        liveData.observe(
            viewLifecycleOwner,
            {
                Log.i(TAG, "spot_detail: $it")
                it?.let {
                    binding.spotDetail = it
                    currentSpotDetail = it
                    binding.navigateIcon.alpha = 1F
                    criterionScoresAdapter.submitList(it.criteriaScore)
                    updateToolbarIfOwner(it)
                }
            }
        )
    }

    private fun updateToolbarIfOwner(spotDetail: SpotDetail) {
        val toolbar: MaterialToolbar = (activity as MainActivity).binding.toolbar
        toolbar.menu.clear()
        if (!spotDetail.owner) {
            Log.i(TAG, "toolbar cleared")
            return
        }
        Log.i(TAG, "toolbar updated")
        toolbar.inflateMenu(R.menu.edit_spot_detail_menu)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    val direction = SpotDetailFragmentDirections
                        .actionSpotDetailFragmentToEditSpotDetailFragment(spotDetail)
                    findNavController().navigate(direction)
                    true
                }
                R.id.delete -> {
                    deleteSpot(spotDetail)
                    true
                }
                else -> false
            }
        }
    }

    private fun deleteSpot(spotDetail: SpotDetail) {
        if (Variables.isNetworkConnected.value!!) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.delete))
                .setMessage(resources.getString(R.string.delete_spot, spotDetail.spotName))
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    spotDetailViewModel.deleteSpot(spotDetail)
                }.setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                .show()
        } else {
            Snackbar.make(
                requireView(),
                resources.getString(R.string.delete_failed) +
                    resources.getString(R.string.no_connection),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStop() {
        super.onStop()
        val toolbar: MaterialToolbar = (activity as MainActivity).binding.toolbar
        toolbar.menu.clear()
    }

    override fun onStart() {
        super.onStart()
        currentSpotDetail?.let {
            updateToolbarIfOwner(it)
        }
    }
}
