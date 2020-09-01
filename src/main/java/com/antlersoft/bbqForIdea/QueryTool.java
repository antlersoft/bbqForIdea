package com.antlersoft.bbqForIdea;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.*;

import com.antlersoft.query.environment.ui.HistoryList;
import com.antlersoft.query.environment.ui.ResultList;
import com.antlersoft.query.environment.ParseException;
import com.antlersoft.query.SelectionSetExpression;
import com.antlersoft.query.SetExpression;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;

/**
 * Created by mike on 3/27/18.
 */
public class QueryTool {
    JTextArea queryArea;
    BrowseByQueryProject project;
    ResultList resultList;
    HistoryList historyList;

    QueryTool(BrowseByQueryProject project, ToolWindow window)
    {
        this.project = project;
        queryArea = new JTextArea(6, 30);
        QueryAction queryAction = new QueryAction();
        resultList = new ResultList();
        historyList = new HistoryList(queryArea, queryAction);
        JButton queryButton=new JButton( queryAction);
        Dimension buttonDimension=queryButton.getPreferredSize();
        queryButton.setMinimumSize( buttonDimension);
        queryButton.setMaximumSize( buttonDimension);
        JBScrollPane queryScroll=new JBScrollPane(queryArea);
        JBScrollPane historyScroll=new JBScrollPane(historyList);
        Box queryBox = Box.createVerticalBox();
        queryBox.add(queryButton);
        queryBox.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryScroll, historyScroll));
        window.getComponent().add(queryBox);
    }

    void displayMessage(String title, String text)
    {
        Notifications.Bus.notify(new Notification(BrowseByQueryProject.BrowseByQueryGroup, title, text, NotificationType.ERROR));
    }

    class QueryAction extends AbstractAction
    {
        QueryAction()
        {
            super("Query");
        }
        public void actionPerformed( ActionEvent ae)
        {
            try
            {
                String line=queryArea.getText();
                if ( line==null || line.length()==0)
                    return;
                project.query.setLine( line);
                SelectionSetExpression querySelection = project.query.getCurrentSelection();
                querySelection.clear();
                for (Object o : resultList.getSelectedValues())
                {
                    querySelection.add(o);
                }
                SetExpression se=project.query.getExpression();
                historyList.addQuery( line);
                Enumeration<?> e=se.evaluate( project.db.getDataSource());
                ArrayList<Object> resultSorter = new ArrayList<Object>();
                while ( e.hasMoreElements())
                    resultSorter.add(e.nextElement());
                Collections.sort(resultSorter, new Comparator<Object>() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        return o1.toString().compareTo(o2.toString());
                    }

                });
                resultList.setContents(resultSorter);
            }
            catch ( ParseException pe)
            {
                displayMessage( "Parse Error", pe.getMessage());
            }
            catch ( Exception unkn)
            {
                displayMessage( "Query Error", unkn.getLocalizedMessage());
            }
        }
    }
}
