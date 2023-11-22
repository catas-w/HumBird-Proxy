package com.catas.wicked.proxy.gui.componet.richtext;

import com.catas.wicked.common.constant.CodeStyle;
import com.catas.wicked.proxy.gui.componet.highlight.Formatter;
import com.catas.wicked.proxy.gui.componet.highlight.Highlighter;
import com.catas.wicked.proxy.gui.componet.highlight.HighlighterFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.ListModification;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * codeArea wrapped in a scrollPane
 * support text highlight
 */
public class DisplayCodeArea extends VirtualizedScrollPane<CodeArea> {

    private VisibleParagraphStyler<Collection<String>, String, Collection<String>> visibleParagraphStyler;

    private TextStyler textStyler;

    private CodeArea codeArea;

    private StringProperty codeStyle = new SimpleStringProperty(CodeStyle.PLAIN.name());

    private static final String STYLE = "display-code-area";

    public DisplayCodeArea(CodeArea codeArea) {
        super(codeArea);
        this.codeArea = codeArea;
        initCodeArea();
    }

    public DisplayCodeArea() {
        super(new CodeArea());
        this.codeArea = this.getContent();
        initCodeArea();
    }

    public String getCodeStyle() {
        return codeStyle.get();
    }

    public StringProperty codeStyleProperty() {
        return codeStyle;
    }

    public void setCodeStyle(String codeStyle) {
        // this.codeStyle.set(codeStyle);
        setCodeStyle(CodeStyle.valueOf(codeStyle));
    }

    /**
     * switch text highlight
     * @param codeStyle highlightStyle
     */
    public void setCodeStyle(CodeStyle codeStyle) {
        this.codeStyle.set(codeStyle.name());
        Highlighter<Collection<String>> highlighter = HighlighterFactory.getHighlightComputer(codeStyle);
        // this.visibleParagraphStyler.setHighlightComputer(highlighter);
        this.textStyler.setHighlightComputer(highlighter);
    }

    public void replaceText(int start, int end, String text) {
        Highlighter<Collection<String>> highlighter = getCurrentHighlighter();
        if (highlighter instanceof Formatter) {
            text = ((Formatter) highlighter).format(text);
        }
        String finalText = text;
        Platform.runLater(() -> {
            codeArea.replaceText(start, end, finalText);
        });
    }

    public void replaceText(String text) {
        Highlighter<Collection<String>> highlighter = getCurrentHighlighter();
        if (highlighter instanceof Formatter) {
            text = ((Formatter) highlighter).format(text);
        }
        String finalText = text;
        Platform.runLater(() -> {
            codeArea.replaceText(finalText);
        });
    }

    public void appendText(String text) {
        Platform.runLater(() -> {
            codeArea.appendText(text);
        });
    }

    private Highlighter<Collection<String>> getCurrentHighlighter() {
        return HighlighterFactory.getHighlightComputer(CodeStyle.valueOf(getCodeStyle()));
    }

    private void initCodeArea() {
        AnchorPane.setLeftAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setTopAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);

        this.getStyleClass().add(STYLE);
        codeArea.setEditable(false);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setContextMenu(new CodeAreaContextMenu());

        // this.visibleParagraphStyler =new VisibleParagraphStyler<>(codeArea, getCurrentHighlighter());
        // codeArea.getVisibleParagraphs().addModificationObserver(visibleParagraphStyler);

        this.textStyler = new TextStyler(codeArea, getCurrentHighlighter());
        codeArea.textProperty().addListener(textStyler);

        // auto-indent: insert previous line's indents on enter
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );
        codeArea.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
        {
            if ( KE.getCode() == KeyCode.ENTER ) {
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher( codeArea.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
                if ( m0.find() ) Platform.runLater( () -> codeArea.insertText( caretPosition, m0.group() ) );
            }
        });
    }

    /**
     * set style by listener
     */
    private static class TextStyler implements ChangeListener<String> {

        private final CodeArea area;

        private Highlighter<Collection<String>> highlightComputer;

        public TextStyler(CodeArea area, Highlighter<Collection<String>> highlightComputer) {
            this.area = area;
            this.highlightComputer = highlightComputer;
        }

        public void setHighlightComputer(Highlighter<Collection<String>> highlightComputer) {
            this.highlightComputer = highlightComputer;
        }

        @Override
        public void changed(ObservableValue observable, String oldValue, String newValue) {
            // TODO efficiency
            area.setStyleSpans(0, highlightComputer.computeHighlight(newValue));
        }
    }

    /**
     * set style for visible paragraph
     */
    private static class VisibleParagraphStyler<PS, SEG, S> implements
            Consumer<ListModification<? extends Paragraph<PS, SEG, S>>>
    {
        private final GenericStyledArea<PS, SEG, S> area;

        private Highlighter<S> highlightComputer;

        private int prevParagraph, prevTextLength;

        public VisibleParagraphStyler(GenericStyledArea<PS, SEG, S> area, Highlighter<S> highlightComputer) {
            this.area = area;
            this.highlightComputer = highlightComputer;
        }

        public void setHighlightComputer(Highlighter<S> highlightComputer) {
            this.highlightComputer = highlightComputer;
        }

        @Override
        public void accept(ListModification<? extends Paragraph<PS, SEG, S>> lm)
        {
            if ( lm.getAddedSize() > 0 ) Platform.runLater( () ->
            {
                int paragraph = Math.min( area.firstVisibleParToAllParIndex() + lm.getFrom(), area.getParagraphs().size()-1 );
                String text = area.getText( paragraph, 0, paragraph, area.getParagraphLength( paragraph ) );

                if ( paragraph != prevParagraph || text.length() != prevTextLength )
                {
                    if ( paragraph < area.getParagraphs().size()-1 )
                    {
                        int startPos = area.getAbsolutePosition( paragraph, 0 );
                        area.setStyleSpans(startPos, highlightComputer.computeHighlight(text));
                    }
                    prevTextLength = text.length();
                    prevParagraph = paragraph;
                }
            });
        }
    }
}
