package com.jintin.kbuilder.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.jintin.kbuilder.annotation.KBuilder
import com.squareup.kotlinpoet.*

class BuilderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(KBuilder::class.qualifiedName.orEmpty())
        val visitor = BuilderVisitor(codeGenerator, logger)
        symbols.filterIsInstance<KSClassDeclaration>()
            .filter(KSNode::validate)
            .forEach {
                it.accept(visitor, Unit)
            }
        return symbols.filterNot { it.validate() }.toList()
    }

    class BuilderVisitor(
        private val codeGenerator: CodeGenerator,
        private val logger: KSPLogger
    ) : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val packageName = classDeclaration.packageName.asString()
            val builderName = classDeclaration.simpleName.asString() + "Builder"
            val variables = classDeclaration.primaryConstructor?.parameters?.map {
                it.asVariableDefinition(logger, true)
            }.orEmpty()
            val genericTypes = classDeclaration.typeParameters.map {
                TypeVariableName(it.name.getShortName())
            }
            val names = classDeclaration.asNameList()
            val fileSpec = FileSpec.builder(packageName, builderName).apply {
                addType(
                    TypeSpec
                        .classBuilder(builderName)
                        .addTypeVariables(genericTypes)
                        .addProperties(variables.map {
                            PropertySpec.builder(it.name, it.typeName)
                                .mutable(true)
                                .initializer("null")
                                .build()
                        })
                        .addFunction(
                            genBuildFunction(
                                packageName,
                                names,
                                genericTypes,
                                variables
                            )
                        )
                        .build()
                )
                addFunction(
                    genToBuilderFunction(
                        packageName,
                        builderName,
                        names,
                        genericTypes,
                        variables
                    )
                )
            }.build()
            codeGenerator.createNewFile(
                dependencies = Dependencies(true),
                packageName = packageName,
                fileName = builderName
            ).use {
                it.writer().use(fileSpec::writeTo)
            }
        }

        private fun genBuildFunction(
            packageName: String,
            names: List<String>,
            genericTypes: List<TypeVariableName>,
            variables: List<VariableDefinition>
        ): FunSpec {
            return FunSpec.builder("build")
                .returns(
                    ClassName(
                        packageName,
                        names
                    ).asParameterized(genericTypes)
                )
                .addCode(
                    genBuildBlock(
                        variables,
                        names.joinToString("."),
                        genericTypes
                    )
                )
                .build()
        }

        private fun genToBuilderFunction(
            packageName: String,
            builderName: String,
            names: List<String>,
            genericTypes: List<TypeVariableName>,
            variables: List<VariableDefinition>
        ): FunSpec {
            return FunSpec.builder("toBuilder")
                .addTypeVariables(genericTypes)
                .receiver(
                    ClassName(packageName, names)
                        .asParameterized(genericTypes)
                )
                .returns(
                    ClassName(packageName, builderName)
                        .asParameterized(genericTypes)
                )
                .addCode(
                    genToBuilderBlock(
                        variables.map { it.name },
                        builderName,
                        genericTypes
                    )
                )
                .build()
        }

        private fun genToBuilderBlock(
            names: List<String>,
            builderName: String,
            genericTypes: List<TypeVariableName>
        ): String {
            logger.logging("generate toBuilder block of $builderName")
            return with(StringBuilder()) {
                appendLine("return $builderName${genericTypes.asString()}().also {")
                names.forEach {
                    appendLine("  it.$it = $it")
                }
                appendLine("}")
                toString()
            }
        }

        private fun genBuildBlock(
            variables: List<VariableDefinition>,
            targetName: String,
            genericTypes: List<TypeVariableName>
        ): String {
            logger.logging("generate build block of $targetName")
            return with(StringBuilder()) {
                variables
                    .filter { !it.isNullable }
                    .forEach {
                        appendLine("val ${it.name} = ${it.name} ?: throw RuntimeException(\"${it.name} is not set yet!\")")
                    }
                appendLine("return $targetName${genericTypes.asString()}(")
                variables.forEach {
                    appendLine("  ${it.name} = ${it.name},")
                }
                appendLine(")")
                toString()
            }
        }
    }
}