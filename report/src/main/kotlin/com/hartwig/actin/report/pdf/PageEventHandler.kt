package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.pdf.components.Footer
import com.hartwig.actin.report.pdf.components.Header
import com.hartwig.actin.report.pdf.components.SidePanel
import com.itextpdf.kernel.events.Event
import com.itextpdf.kernel.events.IEventHandler
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfOutline
import com.itextpdf.kernel.pdf.navigation.PdfExplicitRemoteGoToDestination
import java.time.LocalDate

class PageEventHandler private constructor(private val header: Header, private val footer: Footer, private val sidePanel: SidePanel) :
    IEventHandler {

    private var chapterTitle = "Undefined"
    private var firstPageOfChapter = true
    private var outline: PdfOutline? = null

    override fun handleEvent(event: Event) {
        val documentEvent = event as PdfDocumentEvent
        if (documentEvent.type == PdfDocumentEvent.START_PAGE) {
            val page = documentEvent.page
            header.render(page)
            if (firstPageOfChapter) {
                firstPageOfChapter = false
                createChapterBookmark(documentEvent.document, chapterTitle)
            }
            sidePanel.render(page)
            footer.render(page)
        }
    }

    fun chapterTitle(chapterTitle: String) {
        this.chapterTitle = chapterTitle
    }

    fun resetChapterPageCounter() {
        firstPageOfChapter = true
    }

    fun writePageCounts(document: PdfDocument) {
        footer.writePageCounts(document)
    }

    private fun createChapterBookmark(pdf: PdfDocument, title: String) {
        if (outline == null) {
            outline = pdf.getOutlines(false)
        }
        val chapterItem = outline!!.addOutline(title)
        chapterItem.addDestination(PdfExplicitRemoteGoToDestination.createFitH(pdf.numberOfPages, 0f))
    }

    companion object {
        fun create(patientId: String, reportDate: LocalDate): PageEventHandler {
            return PageEventHandler(Header(), Footer(), SidePanel(patientId, reportDate))
        }
    }
}