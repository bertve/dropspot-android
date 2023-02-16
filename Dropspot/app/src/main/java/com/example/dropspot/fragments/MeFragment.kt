package com.example.dropspot.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.dropspot.R
import com.example.dropspot.adapters.SpotDetailsAdapter
import com.example.dropspot.data.model.AppUser
import com.example.dropspot.databinding.MeFragmentBinding
import com.example.dropspot.viewmodels.MeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MeFragment : Fragment() {

    companion object {
        private const val TAG = "me_fragment"
    }

    private val viewModel: MeViewModel by viewModel()
    private lateinit var binding: MeFragmentBinding
    private val args: MeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.me_fragment, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        val mySpotsAdapter = SpotDetailsAdapter()
        binding.mySpots.adapter = mySpotsAdapter

        val favoSpotsAdapter = SpotDetailsAdapter()
        binding.favoriteSpots.adapter = favoSpotsAdapter

        val user: AppUser = args.user
        binding.user = user
        viewModel.setUser(user)

        viewModel.getMySpots().observe(
            viewLifecycleOwner,
            Observer {
                Log.i(TAG, "mySpots:$it")
                mySpotsAdapter.submitList(it)
            }
        )

        viewModel.favoriteSpots.observe(
            viewLifecycleOwner,
            Observer {
                Log.i(TAG, "favoriteSpots:$it")
                favoSpotsAdapter.submitList(it)
            }
        )

        return binding.root
    }
}
