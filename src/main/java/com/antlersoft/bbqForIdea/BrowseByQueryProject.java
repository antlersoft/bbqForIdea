package com.antlersoft.bbqForIdea;

import com.antlersoft.analyzer.IndexAnalyzeDB;

import com.antlersoft.analyzer.query.QueryParser;
import com.antlersoft.query.environment.AnalyzerQuery;
import com.antlersoft.query.environment.QueryException;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;

import com.sun.net.httpserver.HttpServer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by mike on 4/8/17.
 */
@State(name="BrowseByQueryProject")
public class BrowseByQueryProject implements ProjectComponent, PersistentStateComponent<BrowseByQueryProjectState> {
    static final String BrowseByQueryGroup = "BrowseByQueryGroup";
    private Project _project;
    IndexAnalyzeDB db;
    AnalyzerQuery query;
    QueryTool queryWindow;
    private BrowseByQueryProjectState _state;

    public BrowseByQueryProject(Project project) {
        _project = project;
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "BrowseByQueryProject";
    }

    /**
     * _db and _query should be defined at the end of this method
     */
    @Override
    public void projectOpened() {
        db = new IndexAnalyzeDB();
        if (_state == null) {
            _state = new BrowseByQueryProjectState();
        }
        String databaseFolder = _state.databaseFolder;
        if (databaseFolder == null) {
            String projectFolder = _project.getBasePath();
            if (projectFolder == null) {
                return;
            }
            File dataFolderFile = new File(projectFolder, "bbqDb");
            try {
                db.openDB(dataFolderFile);
                query = new AnalyzerQuery(new QueryParser());
                if (_state.environmentState != null) {
                    StringReader reader = new StringReader(_state.environmentState);
                    query.readEnvironment(reader);
                }
            } catch (Exception e) {
                Notifications.Bus.notify(new Notification(BrowseByQueryGroup, "BBQ Exception Opening Project", e.getLocalizedMessage(), NotificationType.ERROR), _project);
            }
        }

    }

    @Override
    public void projectClosed() {
        updateState();
        if (db != null) {
            try {
                db.closeDB();
            } catch (Exception e) {
                Notifications.Bus.notify(new Notification(BrowseByQueryGroup, "BBQ Exception Closing Project", e.getLocalizedMessage(), NotificationType.ERROR), _project);
            }
        }
        db = null;
        query = null;
    }

    @Nullable
    @Override
    public BrowseByQueryProjectState getState() {
        return _state;
    }

    @Override
    public void loadState(BrowseByQueryProjectState projectComponent) {
        _state = projectComponent;
    }

    //@Override
    public void noStateLoaded() {
        // Placeholder state - empty object
        _state = new BrowseByQueryProjectState();
    }

    private void updateState() {
        if (query != null) {
            StringWriter sw = new StringWriter(1000);
            try {
                query.writeEnvironment(sw);
                _state.environmentState = sw.toString();
            } catch (IOException e) {
                Notifications.Bus.notify(new Notification(BrowseByQueryGroup, "BBQ Exception Getting Query State",
                        e.getLocalizedMessage(), NotificationType.ERROR), _project);
            } catch (QueryException qe) {
                Notifications.Bus.notify(new Notification(BrowseByQueryGroup, "BBQ Exception Getting Query State",
                        qe.getLocalizedMessage(), NotificationType.ERROR), _project);
            }
        }
    }
}
