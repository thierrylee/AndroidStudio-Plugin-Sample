package com.octo.pluginsample

import android.databinding.tool.ext.joinToCamelCase
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.refactoring.RefactoringFactory
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

@ExperimentalStdlibApi
class SnakeCaseToCamelCaseAction : AnAction() {

    companion object {
        private val SUPPORTED_FILE_TYPES = listOf("kotlin")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = SUPPORTED_FILE_TYPES.contains(e.getData(LangDataKeys.LANGUAGE)?.id)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val messageBuilder = StringBuilder()

        // Read classes and method list
        val fileInfo = ProgressManagerUtils.runWithProgress(
            project = e.project,
            progressTitle = "Reading File Structure",
            isCancellable = true,
            method = {
                getFileInfo(e)
            }
        )

        // Rename methods
        fileInfo?.filterIsInstance<SnakeCaseToCamelCase.RenamedMethod>()?.forEach { info ->
            RefactoringFactory.getInstance(e.project)
                .createRename(info.method, info.newMethodName)
                .run()
        }

        // Build message
        fileInfo?.forEach { info ->
            when (info) {
                is SnakeCaseToCamelCase.ClassStart -> {
                    messageBuilder.append("Class :\n${info.className}")
                    messageBuilder.append("\n\n")
                    messageBuilder.append("Methods :")
                }
                is SnakeCaseToCamelCase.Method -> messageBuilder.append(info.methodName)
                is SnakeCaseToCamelCase.RenamedMethod -> messageBuilder.append("${info.newMethodName} (was ${info.method.name})")
                SnakeCaseToCamelCase.ClassEnd -> messageBuilder.append("\n")
            }
            messageBuilder.append("\n")
        }

        Messages.showInfoMessage(messageBuilder.toString(), "Snake Case To Camel Case Result")
        println("Snake Case To Camel Case Result\n\n$messageBuilder")
    }

    private fun getFileInfo(e: AnActionEvent): List<SnakeCaseToCamelCase>? =
        (e.getRequiredData(LangDataKeys.PSI_FILE) as? KtFile)?.declarations?.filterIsInstance<KtClass>()
            ?.flatMap { ktClass ->
                listOf(
                    SnakeCaseToCamelCase.ClassStart(className = ktClass.name),
                    *getMethodsInfo(ktClass = ktClass),
                    SnakeCaseToCamelCase.ClassEnd
                )
            }

    private fun getMethodsInfo(ktClass: KtClass): Array<SnakeCaseToCamelCase> =
        ktClass.body?.functions?.map { method ->
            buildNewMethodNameIfNecessary(method)?.let { newMethodName ->
                SnakeCaseToCamelCase.RenamedMethod(
                    method = method,
                    oldMethodName = method.name!!,
                    newMethodName = newMethodName
                )
            } ?: SnakeCaseToCamelCase.Method(methodName = method.name)
        }?.toTypedArray() ?: emptyArray()

    private fun buildNewMethodNameIfNecessary(method: KtNamedFunction): String? {
        val camelCaseMethodName = method.name
            ?.split("_")
            ?.joinToCamelCase()
            ?.replaceFirstChar { it.lowercaseChar() }
        return if (camelCaseMethodName != method.name) {
            camelCaseMethodName
        } else null
    }
}

private sealed class SnakeCaseToCamelCase {
    data class ClassStart(val className: String?) : SnakeCaseToCamelCase()
    data class Method(val methodName: String?) : SnakeCaseToCamelCase()
    data class RenamedMethod(
        val method: KtNamedFunction,
        val oldMethodName: String,
        val newMethodName: String
    ) : SnakeCaseToCamelCase()

    object ClassEnd : SnakeCaseToCamelCase()
}