package com.antlersoft.bbqForIdea;

import com.antlersoft.query.environment.ui.QueryFrame;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

public class AnalyzePathAction extends AnAction {
    private final FileChooserDescriptor _descriptor = new FileChooserDescriptor(true, true, true, true, false, true);
    private boolean _isAnalyzing;

    /**
     * Displays a dialog box to run analyzer within a path
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        final BrowseByQueryProject bbq = project.getService(BrowseByQueryProject.class);
        if (bbq == null) {
            return;
        }
        _isAnalyzing = true;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final ArrayList<VirtualFile> files = new ArrayList<>();
                FileChooser.chooseFile(_descriptor, project, null, new Consumer<VirtualFile>() {

                    /**
                     * @param virtualFile consequently takes value of each element of the set this processor is passed to for processing.
                     *                    t is supposed to be a not-null value.
                     */
                    @Override
                    public void consume(VirtualFile virtualFile) {
                        files.add(virtualFile);
                    }
                });
                new Thread( new Runnable() {
                    public void run()
                    {
                        try {
                            bbq.analyzeVirtualFiles(files);
                        } catch (Exception excp) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    bbq.queryWindow.displayMessage("Error analyzing files", excp.getLocalizedMessage());
                                }
                            });
                        }
                        finally {
                            _isAnalyzing = false;
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * Updates the state of the action. Default implementation does nothing.
     * Override this method to provide the ability to dynamically change action's
     * state and(or) presentation depending on the context (For example
     * when your action state depends on the selection you can check for
     * selection and change the state accordingly).<p></p>
     * <p>
     * This method can be called frequently, and on UI thread.
     * This means that this method is supposed to work really fast,
     * no real work should be done at this phase. For example, checking selection in a tree or a list,
     * is considered valid, but working with a file system or PSI (especially resolve) is not.
     * If you cannot determine the state of the action fast enough,
     * you should do it in the {@link #actionPerformed(AnActionEvent)} method and notify
     * the user that action cannot be executed if it's the case.<p></p>
     * <p>
     * If the action is added to a toolbar, its "update" can be called twice a second, but only if there was
     * any user activity or a focus transfer. If your action's availability is changed
     * in absence of any of these events, please call {@code ActivityTracker.getInstance().inc()} to notify
     * action subsystem to update all toolbar actions when your subsystem's determines that its actions' visibility might be affected.
     *
     * @param e Carries information on the invocation place and data available
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(!_isAnalyzing);
    }
}
