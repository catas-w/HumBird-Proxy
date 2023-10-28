package com.catas.wicked.proxy.gui.componet.richtext;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.CodeArea;

public class CodeAreaContextMenu extends ContextMenu {
    private MenuItem fold, unfold, print;

    public CodeAreaContextMenu()
    {
        fold = new MenuItem( "Fold selected text" );
        fold.setOnAction( AE -> { hide(); fold(); } );

        unfold = new MenuItem( "Unfold from cursor" );
        unfold.setOnAction( AE -> { hide(); unfold(); } );

        print = new MenuItem( "Print" );
        print.setOnAction( AE -> { hide(); print(); } );

        getItems().addAll( fold, unfold, print );
    }

    /**
     * Folds multiple lines of selected text, only showing the first line and hiding the rest.
     */
    private void fold() {
        ((CodeArea) getOwnerNode()).foldSelectedParagraphs();
    }

    /**
     * Unfold the CURRENT line/paragraph if it has a fold.
     */
    private void unfold() {
        CodeArea area = (CodeArea) getOwnerNode();
        area.unfoldParagraphs( area.getCurrentParagraph() );
    }

    private void print() {
        System.out.println( ((CodeArea) getOwnerNode()).getText() );
    }
}

