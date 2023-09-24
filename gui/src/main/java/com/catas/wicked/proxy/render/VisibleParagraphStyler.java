package com.catas.wicked.proxy.render;

import javafx.application.Platform;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.collection.ListModification;

import java.util.function.Consumer;
import java.util.function.Function;

class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>>
{
    private final GenericStyledArea<PS, SEG, S> area;
    private final Function<String, StyleSpans<S>> computeStyles;
    private int prevParagraph, prevTextLength;

    public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area, Function<String,StyleSpans<S>> computeStyles )
    {
        this.computeStyles = computeStyles;
        this.area = area;
    }

    @Override
    public void accept( ListModification<? extends Paragraph<PS, SEG, S>> lm )
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
                    area.setStyleSpans( startPos, computeStyles.apply( text ) );
                }
                prevTextLength = text.length();
                prevParagraph = paragraph;
            }
        });
    }
}