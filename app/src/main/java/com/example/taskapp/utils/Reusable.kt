package com.example.taskapp.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.taskapp.R

fun ImageView.loadImage(context: Context, url: String) {
    Glide.with(context).load(url).error(R.drawable.ic_logo).placeholder(R.drawable.placeholder).centerCrop().into(this)
}