package com.hartwig.actin.report.pdf;

import com.hartwig.actin.report.pdf.components.Footer;
import com.hartwig.actin.report.pdf.components.Header;
import com.hartwig.actin.report.pdf.components.SidePanel;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitRemoteGoToDestination;

import org.jetbrains.annotations.NotNull;

public class PageEventHandler implements IEventHandler {

    @NotNull
    private final Header header;
    @NotNull
    private final Footer footer;
    @NotNull
    private final SidePanel sidePanel;

    private String chapterTitle = "Undefined";
    private boolean firstPageOfChapter = true;
    private PdfOutline outline = null;

    @NotNull
    static PageEventHandler create(@NotNull String patientId) {
        return new PageEventHandler(new Header(), new Footer(), new SidePanel(patientId));
    }

    private PageEventHandler(@NotNull final Header header, @NotNull final Footer footer, @NotNull final SidePanel sidePanel) {
        this.header = header;
        this.footer = footer;
        this.sidePanel = sidePanel;
    }

    @Override
    public void handleEvent(@NotNull Event event) {
        PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
        if (documentEvent.getType().equals(PdfDocumentEvent.START_PAGE)) {
            PdfPage page = documentEvent.getPage();

            header.render(page);
            if (firstPageOfChapter) {
                firstPageOfChapter = false;

                createChapterBookmark(documentEvent.getDocument(), chapterTitle);
            }

            sidePanel.render(page);
            footer.render(page);
        }
    }

    void chapterTitle(@NotNull String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    void resetChapterPageCounter() {
        firstPageOfChapter = true;
    }

    void writePageCounts(@NotNull PdfDocument document) {
        footer.writePageCounts(document);
    }

    private void createChapterBookmark(@NotNull PdfDocument pdf, @NotNull String title) {
        if (outline == null) {
            outline = pdf.getOutlines(false);
        }

        PdfOutline chapterItem = outline.addOutline(title);
        chapterItem.addDestination(PdfExplicitRemoteGoToDestination.createFitH(pdf.getNumberOfPages(), 0));
    }
}
