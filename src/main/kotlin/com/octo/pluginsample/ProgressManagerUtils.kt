package com.octo.pluginsample

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

object ProgressManagerUtils {

    fun<T> runWithProgress(project: Project?, progressTitle: String, isCancellable: Boolean, method: () -> T): T {
        return ProgressManager.getInstance().run(
            object : Task.WithResult<T, Exception>(project, progressTitle, isCancellable) {
                override fun compute(indicator: ProgressIndicator): T {
                    indicator.isIndeterminate = true
                    indicator.start()
                    val result = method.invoke()
                    indicator.stop()
                    return result
                }
            }
        )
    }

}