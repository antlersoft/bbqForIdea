package com.antlersoft.bbqForIdea;

import com.antlersoft.analyzer.SourceObject;
import com.antlersoft.query.environment.ui.ResultList;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ResultTool {
    ResultList _list;
    ResultTool(BrowseByQueryProject bbq, ToolWindow window) {
        _list = bbq.getResultList();
        _list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    Object o = _list.getSelectedValue();
                    bbq.openObjectIfPossible(_list.getSelectedValue());
                }
            }
        });
        JBScrollPane pane = new JBScrollPane(_list);
        window.getComponent().add(pane);
    }
}
