package com.antlersoft.bbqForIdea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class ProjectListener implements ProjectManagerListener {
    /**
     * Invoked on project open.
     *
     * @param project opening project
     */
    @Override
    public void projectOpened(@NotNull Project project) {
        ProjectManagerListener.super.projectOpened(project);
        // Presumably we can get build notifications by subscribing to the message bus, not implemented yet
        // project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener());
    }
}
