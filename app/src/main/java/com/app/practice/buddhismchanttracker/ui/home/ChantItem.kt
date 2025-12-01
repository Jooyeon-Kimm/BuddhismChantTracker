package com.app.practice.buddhismchanttracker.ui.home

data class ChantItem(
    val id: Long = System.currentTimeMillis(), // 간단히 고유 값 용도
    val text: String,                          // 체크리스트 내용
    val checked: Boolean = false               // 체크 여부
)
