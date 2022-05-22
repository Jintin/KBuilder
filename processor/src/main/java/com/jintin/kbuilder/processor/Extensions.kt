package com.jintin.kbuilder.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

fun KSDeclaration.asNameList(): List<String> {
    val list = mutableListOf<String>()
    var definition: KSDeclaration? = this
    while (definition != null) {
        list.add(0, definition.simpleName.asString())
        if (definition is KSTypeParameter) {
            break
        }
        definition = definition.parentDeclaration
    }
    return list
}

//fun List<KSTypeParameter>.asString(): String {
//    return if (isNotEmpty()) {
//        joinToString(prefix = "<", postfix = ">") { it.name.asString() }
//    } else {
//        ""
//    }
//}

fun List<TypeVariableName>.asString(): String {
    return if (isNotEmpty()) {
        joinToString(prefix = "<", postfix = ">") { it.name }
    } else {
        ""
    }
}

fun ClassName.asParameterized(typeVariables: List<TypeName>?): TypeName {
    return if (typeVariables == null || typeVariables.isEmpty()) {
        this
    } else {
        parameterizedBy(typeVariables)
    }
}

fun KSTypeReference.asTypeName(logger: KSPLogger, forceNullable: Boolean = false): TypeName {
    val declaration = this.resolve().declaration
    val className = ClassName(
        declaration.packageName.asString(),
        declaration.asNameList()
    ).asParameterized(this.element?.typeArguments?.mapNotNull { it.type?.asTypeName(logger) })
    return className.copy(nullable = forceNullable || this.resolve().nullability == Nullability.NULLABLE)
}

fun KSValueParameter.asVariableDefinition(
    logger: KSPLogger,
    forceNullable: Boolean = false
): VariableDefinition {
    return VariableDefinition(
        this.name?.asString().orEmpty(),
        this.type.asTypeName(logger, forceNullable),
        this.type.resolve().nullability != Nullability.NOT_NULL
    )
}