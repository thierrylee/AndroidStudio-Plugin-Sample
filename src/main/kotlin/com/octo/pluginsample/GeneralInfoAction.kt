package com.octo.pluginsample

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.ui.Messages

class GeneralInfoAction : AnAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val messageBuilder = StringBuilder()

        // Add project root path
        val rootPath = getProjectRootPath(e)
        messageBuilder.append("Root path :\n$rootPath")
        messageBuilder.append("\n\n")

        // Add current filePath
        val filePath = getCurrentFilePath(e)
        messageBuilder.append("Current file :\n$filePath")
        messageBuilder.append("\n\n")

        // Add file type
        val fileType = getCurrentFileType(e)
        messageBuilder.append("File type :\n$fileType")
        messageBuilder.append("\n\n")

        Messages.showInfoMessage(messageBuilder.toString(), "General Info Result")
        println("General Info Result\n\n$messageBuilder")
    }

    private fun getCurrentFileType(e: AnActionEvent) = e.getRequiredData(LangDataKeys.LANGUAGE)

    private fun getCurrentFilePath(e: AnActionEvent) =
        e.getRequiredData(LangDataKeys.VIRTUAL_FILE).path

    private fun getProjectRootPath(e: AnActionEvent) = e.project?.basePath

}