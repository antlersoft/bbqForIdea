package com.antlersoft.bbqForIdea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by mike on 3/26/18.
 */
public class QueryFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BrowseByQueryProject bbq = project.getComponent(BrowseByQueryProject.class);
        bbq.queryWindow = new QueryTool(bbq, toolWindow);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}