package com.github.antlersoft.bbqforidea.services

import com.intellij.openapi.project.Project
import com.github.antlersoft.bbqforidea.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
