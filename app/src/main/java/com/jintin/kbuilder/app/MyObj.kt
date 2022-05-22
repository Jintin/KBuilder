package com.jintin.kbuilder.app

import com.jintin.kbuilder.annotation.KBuilder

@KBuilder
data class MyObj(
    val value1: String,
    val value2: Int? = 0,
    val value3: List<Int>? = null,
    val value4: Map<String, String>? = mapOf()
)