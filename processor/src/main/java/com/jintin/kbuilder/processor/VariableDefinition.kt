package com.jintin.kbuilder.processor

import com.squareup.kotlinpoet.TypeName

data class VariableDefinition(
    val name: String,
    val typeName: TypeName,
    val isNullable: Boolean
)