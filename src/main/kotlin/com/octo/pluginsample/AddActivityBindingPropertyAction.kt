package com.octo.pluginsample

import android.databinding.tool.ext.joinToCamelCase
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.util.containers.isNullOrEmpty
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset


class AddActivityBindingPropertyAction : AnAction() {

    companion object {
        private val SUPPORTED_FILE_TYPES = listOf("kotlin")
        private val ACTIVITY_CLASSES = listOf("Activity()", "AppCompatActivity()")
        private const val ONCREATE_METHOD_NAME = "onCreate"
        private const val SET_CONTENT_VIEW_METHOD_PREFIX = "setContentView(R.layout."
        private const val SET_CONTENT_VIEW_METHOD_END = ")"
        private const val BINDING_CLASS_SUFFIX = "Binding"
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = SUPPORTED_FILE_TYPES.contains(e.getData(LangDataKeys.LANGUAGE)?.id)
    }

    override fun actionPerformed(e: AnActionEvent) {
        // Check if current file has an Activity
        val ktFileInfo = ProgressManagerUtils.runWithProgress(
            project = e.project,
            progressTitle = "Reading File Structure",
            isCancellable = true,
            method = {
                Thread.sleep(1000)
                val currentOffset = getCurrentOffset(e)
                retrieveFileInfo(e, currentOffset)
            }
        )

        ktFileInfo?.let { fileInfo ->
            // Build ViewBinding name
            val viewBindingName = buildBindingName(fileInfo.layoutName)

            // Build ViewBinding property
            val viewBindingProperty = "private lateinit var viewBinding: ${fileInfo.packageName}.databinding.$viewBindingName"

            // Write property at cursor position
            WriteCommandAction.runWriteCommandAction(e.project) {
                e.getRequiredData(LangDataKeys.EDITOR).document.insertString(getCurrentOffset(e), viewBindingProperty)
            }
        } ?: Messages.showMessageDialog(
            "Could not find an Activity or layout :/",
            "Add Activity Binding Result",
            AllIcons.Ide.FatalError
        )
    }

    private fun getCurrentOffset(e: AnActionEvent) =
        e.getRequiredData(LangDataKeys.EDITOR).caretModel.currentCaret.offset

    private fun retrieveFileInfo(e: AnActionEvent, currentOffset: Int): AddActivityBindingPropertyFileInfo? =
        (e.getRequiredData(LangDataKeys.PSI_FILE) as? KtFile)?.let { ktFile ->
            // Find all activities
            getActivities(e)?.let { activities ->
                // But keep only one
                retrieveRelevantActivity(activities, currentOffset)?.let { currentActivity ->
                    // Find layout name
                    findLayoutFileName(currentActivity)?.let { layoutName ->
                        AddActivityBindingPropertyFileInfo(
                            ktFile = ktFile,
                            packageName = ktFile.packageDirective?.qualifiedName!!,
                            currentActivity = currentActivity,
                            layoutName = layoutName,
                        )
                    }
                }
            }
        }

    private fun getActivities(e: AnActionEvent): List<KtClass>? =
        (e.getRequiredData(LangDataKeys.PSI_FILE) as? KtFile)?.declarations
            ?.filterIsInstance<KtClass>()
            ?.filter { ktClass ->
                ktClass.getSuperTypeList()?.entries?.any {
                    ACTIVITY_CLASSES.contains(it.text)
                } ?: false
            }

    private fun retrieveRelevantActivity(activities: List<KtClass>, currentOffset: Int) =
        if (!activities.isNullOrEmpty()) {
            activities.firstOrNull {
                it.startOffset < currentOffset && it.endOffset > currentOffset
            }
        } else activities.firstOrNull()

    private fun findLayoutFileName(ktClass: KtClass): String? =
        retrieveOnCreateMethods(ktClass)?.flatMap { method ->
            retrieveSetContentViewLine(method)?.map { setContentViewLine ->
                setContentViewLine
                    .substringAfter(SET_CONTENT_VIEW_METHOD_PREFIX)
                    .substringBefore(SET_CONTENT_VIEW_METHOD_END)
            } ?: emptyList()
        }?.firstOrNull()

    private fun retrieveOnCreateMethods(ktClass: KtClass): List<KtNamedFunction>? =
        ktClass.body?.functions?.filter { method ->
            method.name == ONCREATE_METHOD_NAME
        }

    private fun retrieveSetContentViewLine(method: KtNamedFunction): List<String>? =
        method.bodyExpression?.children
            ?.map { it.text }
            ?.filter { psiElementText ->
                psiElementText.startsWith(SET_CONTENT_VIEW_METHOD_PREFIX)
            }

    private fun buildBindingName(layoutName: String) = layoutName
        .split("_")
        .joinToCamelCase() + BINDING_CLASS_SUFFIX
}

private data class AddActivityBindingPropertyFileInfo(
    val ktFile: KtFile,
    val packageName: String,
    val currentActivity: KtClass,
    val layoutName: String,
)