package com.antlersoft.bbqForIdea;

import com.antlersoft.analyzer.DBClass;
import com.antlersoft.analyzer.IndexAnalyzeDB;
import com.antlersoft.analyzer.SourceObject;
import com.antlersoft.analyzer.query.QueryParser;
import com.antlersoft.query.environment.AnalyzerQuery;
import com.antlersoft.query.environment.QueryException;
import com.antlersoft.query.environment.ui.ResultList;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by mike on 4/8/17.
 */
@State(name="BrowseByQueryProject")
public class BrowseByQueryProject implements Disposable, PersistentStateComponent<BrowseByQueryProjectState> {
    static final String BrowseByQueryGroup = "BrowseByQueryGroup";
    private Project _project;
    private Object _initializationLock = new Object();
    private boolean _initialized = false;
    private ResultList _resultList;
    IndexAnalyzeDB db;
    AnalyzerQuery query;
    QueryTool queryWindow;
    private BrowseByQueryProjectState _state;

    public BrowseByQueryProject(Project project) {
        _project = project;
    }

    public ResultList getResultList() {
        initializeProject();
        return _resultList;
    }

    public void analyzeVirtualFiles(List<VirtualFile> vFiles)
    throws Exception {
        initializeProject();
        File[] files = new File[vFiles.size()];
        int i = 0;
        for (VirtualFile vf : vFiles) {
            files[i++] = new File(vf.getPath());
        }
        try {
            db.analyze(files);
        } catch (Exception e) {
            if (queryWindow != null) {
                queryWindow.displayMessage("Error analyzing files", e.getLocalizedMessage());
            }
        }
    }

    void openObjectIfPossible(Object o) {
        if (o instanceof SourceObject) {
            SourceObject source = (SourceObject)o;
            DBClass dbc = source.getDBClass();
            String sourceFile = dbc.getSourceFile();
            if (sourceFile == null) {
                for (; dbc.getContainingClass() != null; dbc=dbc.getContainingClass());
                sourceFile = dbc.getName().replace('.','/')+".java";
            }
            queryWindow.displayMessage("Trying to display","Class: "+dbc.getName()+" source: "+sourceFile);
            PsiFile[] files = FilenameIndex.getFilesByName(_project, sourceFile, GlobalSearchScope.everythingScope(_project));
            if (files != null && files.length > 0) {
                files[0].navigate(true);
                if (source.getLineNumber() > 0 && source.getLineNumber() < 100000) {
                    Document doc = FileDocumentManager.getInstance().getDocument(files[0].getVirtualFile());
                    if (doc != null) {
                        final Editor[] editors = EditorFactory.getInstance().getEditors(doc, _project);
                        int startOffset = doc.getLineStartOffset(source.getLineNumber());
                        if (editors.length > 0) {
                            Editor editor = editors[0];
                            LogicalPosition lp = editor.offsetToLogicalPosition(startOffset);
                            editor.getScrollingModel().scrollTo(lp, ScrollType.MAKE_VISIBLE);
                        }
                    }
                }
            }
        }
    }

    /**
     * _db and _query should be defined at the end of this method
     */
    public void initializeProject() {
        synchronized (_initializationLock) {
            if (_initialized) {
                return;
            }
            _resultList = new ResultList();
            db = new IndexAnalyzeDB();
            if (_state == null) {
                _state = new BrowseByQueryProjectState();
            }
            String databaseFolder = _state.databaseFolder;
            if (databaseFolder == null) {
                String projectFolder = new File(_project.getProjectFilePath()).getParent();
                if (projectFolder == null) {
                    return;
                }
                _state.databaseFolder = new File(projectFolder, "bbqDb").getAbsolutePath();
            }
            try {
                db.openDB(new File(_state.databaseFolder));
                query = new AnalyzerQuery(new QueryParser());
                if (_state.environmentState != null) {
                    StringReader reader = new StringReader(_state.environmentState);
                    query.readEnvironment(reader);
                }
                _initialized = true;
            } catch (Exception e) {
                Notifications.Bus.notify(new Notification(BrowseByQueryGroup, "BBQ Exception Opening Project", e.getLocalizedMessage(), NotificationType.ERROR), _project);
            }
        }
    }

    @Override
    public void dispose() {
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

    @Override
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
