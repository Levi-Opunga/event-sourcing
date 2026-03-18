package com.codekxlabs.eventsourcing.processor

/**
 * Created on 7/1/25
 * @author Levi Opunga
 **/


import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import com.codekxlabs.eventsourcing.annotations.EventReceiver
import com.codekxlabs.eventsourcing.annotations.EventSource

class EventSourcingProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val eventSourceSymbols = resolver.getSymbolsWithAnnotation(EventSource::class.qualifiedName!!)
        val eventReceiverSymbols = resolver.getSymbolsWithAnnotation(EventReceiver::class.qualifiedName!!)

        val ret = mutableListOf<KSAnnotated>()

        // Process EventSource classes
        eventSourceSymbols
            .filterIsInstance<KSClassDeclaration>()
            .forEach { classDeclaration ->
                if (!classDeclaration.validate()) {
                    ret.add(classDeclaration)
                    return@forEach
                }
                processEventSourceClass(classDeclaration)
            }

        // Process EventReceiver classes
        eventReceiverSymbols
            .filterIsInstance<KSClassDeclaration>()
            .forEach { classDeclaration ->
                if (!classDeclaration.validate()) {
                    ret.add(classDeclaration)
                    return@forEach
                }
                processEventReceiverClass(classDeclaration)
            }

        return ret
    }

    private fun processEventSourceClass(classDeclaration: KSClassDeclaration) {
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()

        logger.info("Processing EventSource class: $className")

        val fileBuilder = FileSpec.builder(packageName, "${className}Extensions")

        // Generate extension properties for each public method
        classDeclaration.getAllFunctions()
            .filter { it.isPublic() && !it.simpleName.asString().startsWith("<") }

            .forEach { function ->
                val functionName = function.simpleName.asString()
                val constantName = "fn_${functionName.uppercase()}"
                val constantValue = "$className.$functionName"

                // Generate extension property
                val propertySpec = PropertySpec.builder(constantName, String::class)
                    .receiver(classDeclaration.toClassName())
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return %S", constantValue)
                            .build()
                    )
                    .addKdoc("Generated event source stub for method: $functionName")
                    .build()

                fileBuilder.addProperty(propertySpec)
            }

        val file = fileBuilder.build()
        file.writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }

    private fun processEventReceiverClass(classDeclaration: KSClassDeclaration) {
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()

        logger.info("Processing EventReceiver class: $className")

        val fileBuilder = FileSpec.builder(packageName, "${className}EventHandlers")

        // Generate constants for event handler methods
        classDeclaration.getAllFunctions()
            .filter { it.isPublic() && !it.simpleName.asString().startsWith("<") }
            .filter { it.isPublic() }
            .filter { function ->
                // Check if method has event-related parameters or annotations
                function.parameters.any { param ->
                    param.type.resolve().declaration.simpleName.asString().contains("Event", ignoreCase = true)
                }
            }
            .forEach { function ->
                val functionName = function.simpleName.asString()
                val constantName = "HANDLER_${functionName.uppercase()}"
                val constantValue = "$className.$functionName"

                val propertySpec = PropertySpec.builder(constantName, String::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", constantValue)
                    .addKdoc("Generated event receiver stub for handler: $functionName")
                    .build()

                fileBuilder.addProperty(propertySpec)
            }

        val file = fileBuilder.build()
        file.writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }
}


// Helper extension functions
private fun KSClassDeclaration.toClassName(): ClassName {
    return ClassName(packageName.asString(), simpleName.asString())
}

private fun KSFunctionDeclaration.isPublic(): Boolean {
    return !modifiers.contains(Modifier.PRIVATE) && !modifiers.contains(Modifier.INTERNAL)
}

