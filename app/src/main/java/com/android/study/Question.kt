package com.android.study

import androidx.annotation.StringRes

data class Question(val string: String, @StringRes val resId: Int)