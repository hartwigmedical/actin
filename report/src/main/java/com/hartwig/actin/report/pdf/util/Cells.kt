package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfArray
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.kernel.pdf.annot.PdfAnnotation
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation
import com.itextpdf.layout.Style
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.IBlockElement
import com.itextpdf.layout.element.Link
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.apache.logging.log4j.util.Strings

object Cells {
    fun create(element: IBlockElement?): Cell {
        return create(element, 1, 1)
    }

    fun createEmpty(): Cell {
        return create(Paragraph(Strings.EMPTY))
    }

    fun createSpanningNoneEntry(table: Table): Cell {
        return createSpanningEntry("None", table)
    }

    fun createSpanningEntry(text: String, table: Table): Cell {
        val cell = create(Paragraph(text), 1, table.numberOfColumns)
        cell.addStyle(Styles.tableContentStyle())
        return cell
    }

    fun createSpanningTitle(text: String, table: Table): Cell {
        val cell = create(Paragraph(text), 1, table.numberOfColumns)
        cell.addStyle(Styles.tableTitleStyle())
        return cell
    }

    fun createTitle(text: String): Cell {
        val cell = create(Paragraph(text))
        cell.addStyle(Styles.tableTitleStyle())
        return cell
    }

    fun createSubTitle(text: String): Cell {
        val cell = create(Paragraph(text))
        cell.addStyle(Styles.tableSubTitleStyle())
        return cell
    }

    @Suppress("unused")
    fun createHeaderTest(text: String): Cell {
        // TODO (ACTIN-33) Clean up or actually use.
        val la1 = PdfLinkAnnotation(Rectangle(0f, 0f, 0f, 0f)).setHighlightMode(PdfAnnotation.HIGHLIGHT_NONE)
            .setAction(PdfAction.createJavaScript("app.alert('These are all trials!!')"))
            .setBorder(PdfArray(intArrayOf(0, 0, 0))) as PdfLinkAnnotation
        val link = Link(text, la1)
        val cell = create(Paragraph(Strings.EMPTY).add(link))
        cell.addStyle(Styles.tableHeaderStyle())
        return cell
    }

    fun createHeader(text: String): Cell {
        val cell = create(Paragraph(text))
        cell.addStyle(Styles.tableHeaderStyle())
        return cell
    }

    fun createSpanningSubNote(text: String, table: Table): Cell {
        val cell = create(Paragraph(text), 1, table.numberOfColumns)
        cell.addStyle(Styles.tableSubStyle())
        return cell
    }

    fun createSubNote(text: String): Cell {
        val cell = create(Paragraph(text))
        cell.addStyle(Styles.tableSubStyle())
        return cell
    }

    @JvmOverloads
    fun createContent(element: IBlockElement, style: Style? = Styles.tableContentStyle()): Cell {
        val cell = create(element)
        cell.addStyle(style)
        cell.setBorderTop(SolidBorder(Styles.PALETTE_MID_GREY, 0.25f))
        return cell
    }

    fun createContent(text: String): Cell {
        return createContent(Paragraph(text))
    }

    fun createContentBold(text: String): Cell {
        return createContent(Paragraph(text), Styles.tableHighlightStyle())
    }

    fun createContentNoBorder(text: String): Cell {
        return createContentNoBorder(Paragraph(text))
    }

    fun createContentNoBorder(element: IBlockElement): Cell {
        val cell = create(element)
        cell.addStyle(Styles.tableContentStyle())
        return cell
    }

    fun createContentNoBorderDeemphasize(text: String): Cell {
        return createContentNoBorderDeemphasize(Paragraph(text))
    }

    fun createContentNoBorderDeemphasize(element: IBlockElement): Cell {
        val cell = createContentNoBorder(element)
        cell.setFontColor(Styles.PALETTE_MID_GREY)
        return cell
    }

    fun createContentWarn(text: String): Cell {
        val cell = createContent(text)
        cell.setFontColor(Styles.PALETTE_WARN)
        return cell
    }

    fun createContentYesNo(yesNo: String): Cell {
        val cell = createContent(yesNo)
        cell.setFontColor(Formats.fontColorForYesNo(yesNo))
        return cell
    }

    fun createKey(text: String): Cell {
        val cell = create(Paragraph(text))
        cell.addStyle(Styles.tableKeyStyle())
        return cell
    }

    fun createValue(text: String): Cell {
        val cell = create(Paragraph(text))
        cell.addStyle(Formats.styleForTableValue(text))
        return cell
    }

    fun createValue(paragraphs: List<Paragraph?>): Cell {
        val cell = create(null)
        for (paragraph in paragraphs) {
            cell.add(paragraph)
        }
        cell.addStyle(Styles.tableHighlightStyle())
        return cell
    }

    fun createValueYesNo(yesNo: String): Cell {
        val cell = createValue(yesNo)
        cell.setFontColor(Formats.fontColorForYesNo(yesNo))
        return cell
    }

    fun createEvaluation(evaluation: Evaluation): Cell {
        return createEvaluationResult(evaluation.result(), evaluation.recoverable())
    }

    @JvmOverloads
    fun createEvaluationResult(result: EvaluationResult, recoverable: Boolean = false): Cell {
        var addon = Strings.EMPTY
        if (result == EvaluationResult.FAIL && recoverable) {
            addon = " (potentially recoverable)"
        }
        val cell = create(Paragraph(result.toString() + addon))
        cell.setFontColor(Formats.fontColorForEvaluation(result))
        return cell
    }

    private fun create(element: IBlockElement?, rows: Int, cols: Int): Cell {
        val cell = Cell(rows, cols)
        cell.setBorder(Border.NO_BORDER)
        if (element != null) {
            cell.add(element)
        }
        return cell
    }
}