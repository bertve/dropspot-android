package com.example.dropspot.binding

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.dropspot.R
import com.example.dropspot.data.model.AppUser
import com.google.android.material.navigation.NavigationView

@BindingAdapter("rankingImgFromScore")
fun bindRankingImgFromScore(view: ImageView, score: Int) {
    when (score) {
        0 -> view.setImageResource(R.drawable.ic_zero)
        1 -> view.setImageResource(R.drawable.ic_one)
        2 -> view.setImageResource(R.drawable.ic_two)
        3 -> view.setImageResource(R.drawable.ic_three)
        4 -> view.setImageResource(R.drawable.ic_four)
        5 -> view.setImageResource(R.drawable.ic_five)
        else -> view.setImageResource(R.drawable.ic_zero)
    }
}

@BindingAdapter("LikeImgFromLiked")
fun bindLikeImgFromSpotAlreadyLiked(view: ImageView, alreadyLiked: Boolean) {
    if (alreadyLiked) {
        view.setImageResource(R.drawable.ic_like_filled)
    } else {
        view.setImageResource(R.drawable.ic_like_outlined)
    }
}

@BindingAdapter("navViewPresentationFromUser")
fun bindNavViewFromUser(v: NavigationView, user: AppUser?) {
    if (user != null) {
        v.getHeaderView(0).findViewById<TextView>(R.id.nav_username).text = user.username
        v.getHeaderView(0).findViewById<TextView>(R.id.nav_username).visibility = View.VISIBLE
        v.getHeaderView(0).findViewById<ProgressBar>(R.id.user_loading).visibility = View.GONE
    } else {
        v.getHeaderView(0).findViewById<TextView>(R.id.nav_username).visibility = View.GONE
        v.getHeaderView(0).findViewById<ProgressBar>(R.id.user_loading).visibility = View.VISIBLE
    }
}
