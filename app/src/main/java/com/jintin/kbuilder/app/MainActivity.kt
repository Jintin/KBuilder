package com.jintin.kbuilder.app

import android.app.Activity
import android.os.Bundle
import com.jintin.kbuilder.app.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val obj = MyObj("init value")
        val newObj = obj.toBuilder().apply {
            this.value1 = "updated value"
        }.build()
        println(newObj.value1) // "updated value"

        val builder = MyObjBuilder()
        builder.value1 = "dynamic value"
        val newObj2 = builder.build()
        println(newObj2.value1) // "dynamic value"

        binding.text.text = newObj.value1 + ":" + newObj2.value1
    }

}