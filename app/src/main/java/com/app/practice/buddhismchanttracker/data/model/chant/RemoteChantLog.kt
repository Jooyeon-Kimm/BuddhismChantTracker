package com.app.practice.buddhismchanttracker.data.model.chant

import com.app.practice.buddhismchanttracker.ui.home.CountType

data class RemoteChantLog(
    val timestamp: Long = 0L,
    val endTimestamp: Long? = null,
    val delta: Int = 0,
    val total: Int = 0,
    val source: String = CountType.MANUAL_SMALL.name,
    val ymd: String = ""
)
