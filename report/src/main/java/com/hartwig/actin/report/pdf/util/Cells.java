package com.hartwig.actin.report.pdf.util;

import java.util.List;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Cells {

    private Cells() {
    }

    @NotNull
    public static Cell create(@Nullable IBlockElement element) {
        return create(element, 1, 1);
    }

    @NotNull
    public static Cell createEmpty() {
        return create(new Paragraph(Strings.EMPTY));
    }

    @NotNull
    public static Cell createSpanningNoneEntry(@NotNull Table table) {
        return createSpanningEntry("None", table);
    }

    @NotNull
    public static Cell createSpanningEntry(@NotNull String text, @NotNull Table table) {
        Cell cell = create(new Paragraph(text), 1, table.getNumberOfColumns());
        cell.addStyle(Styles.tableContentStyle());
        return cell;
    }

    @NotNull
    public static Cell createSpanningTitle(@NotNull String text, @NotNull Table table) {
        Cell cell = create(new Paragraph(text), 1, table.getNumberOfColumns());
        cell.addStyle(Styles.tableTitleStyle());
        return cell;
    }

    @NotNull
    public static Cell createTitle(@NotNull String text) {
        Cell cell = create(new Paragraph(text));
        cell.addStyle(Styles.tableTitleStyle());
        return cell;
    }

    @NotNull
    public static Cell createSubTitle(@NotNull String text) {
        Cell cell = create(new Paragraph(text));
        cell.addStyle(Styles.tableSubTitleStyle());
        return cell;
    }

    @NotNull
    public static Cell createHeaderTest(@NotNull String text) {
        // TODO Clean up or actually use.
        PdfLinkAnnotation la1 = (PdfLinkAnnotation) new PdfLinkAnnotation(new Rectangle(0, 0, 0, 0))
                .setHighlightMode(PdfAnnotation.HIGHLIGHT_NONE)
                .setAction(PdfAction.createJavaScript("app.alert('These are all trials!!')"))
                .setBorder(new PdfArray(new int[]{0,0,0}));

        Link link = new Link(text, la1);

        Cell cell = create(new Paragraph(Strings.EMPTY).add(link));
        cell.addStyle(Styles.tableHeaderStyle());
        return cell;
    }

    @NotNull
    public static Cell createHeader(@NotNull String text) {
        Cell cell = create(new Paragraph(text));
        cell.addStyle(Styles.tableHeaderStyle());
        return cell;
    }

    @NotNull
    public static Cell createSpanningSubNote(@NotNull String text, @NotNull Table table) {
        Cell cell = create(new Paragraph(text), 1, table.getNumberOfColumns());
        cell.addStyle(Styles.tableSubStyle());
        return cell;
    }

    @NotNull
    public static Cell createSubNote(@NotNull String text) {
        Cell cell = create(new Paragraph(text));
        cell.addStyle(Styles.tableSubStyle());
        return cell;
    }

    @NotNull
    public static Cell createContent(@NotNull IBlockElement element) {
        Cell cell = create(element);
        cell.addStyle(Styles.tableContentStyle());
        cell.setBorderBottom(new SolidBorder(Styles.PALETTE_MID_GREY, 0.25F));
        return cell;
    }

    @NotNull
    public static Cell createContent(@NotNull String text) {
        return createContent(new Paragraph(text));
    }

    @NotNull
    public static Cell createContentWarn(@NotNull String text) {
        Cell cell = createContent(text);
        cell.setFontColor(Styles.PALETTE_WARN);
        return cell;
    }

    @NotNull
    public static Cell createContentYesNo(@NotNull String yesNo) {
        Cell cell = createContent(yesNo);
        cell.setFontColor(Formats.fontColorForYesNo(yesNo));
        return cell;
    }

    @NotNull
    public static Cell createKey(@NotNull String text) {
        Cell cell = create(new Paragraph(text));
        cell.addStyle(Styles.tableKeyStyle());
        return cell;
    }

    @NotNull
    public static Cell createValue(@NotNull String text) {
        Cell cell = create(new Paragraph(text));
        cell.addStyle(Formats.styleForTableValue(text));
        return cell;
    }

    @NotNull
    public static Cell createValue(@NotNull List<Paragraph> paragraphs) {
        Cell cell = create(null);
        for (Paragraph paragraph : paragraphs) {
            cell.add(paragraph);
        }
        cell.addStyle(Styles.tableHighlightStyle());
        return cell;
    }

    @NotNull
    public static Cell createValueWarn(@NotNull String text) {
        Cell cell = createValue(text);
        cell.setFontColor(Styles.PALETTE_WARN);
        return cell;
    }

    @NotNull
    public static Cell createValueYesNo(@NotNull String yesNo) {
        Cell cell = createValue(yesNo);
        cell.setFontColor(Formats.fontColorForYesNo(yesNo));
        return cell;
    }

    @NotNull
    public static Cell createEvaluation(@NotNull Evaluation evaluation) {
        return createEvaluationResult(evaluation.result(), evaluation.recoverable());
    }

    @NotNull
    public static Cell createEvaluationResult(@NotNull EvaluationResult result) {
        return createEvaluationResult(result, false);
    }

    @NotNull
    public static Cell createEvaluationResult(@NotNull EvaluationResult result, boolean recoverable) {
        String addon = Strings.EMPTY;
        if (result == EvaluationResult.FAIL && recoverable) {
            addon = " (potentially recoverable)";
        }
        Cell cell = create(new Paragraph(result + addon));
        cell.setFontColor(Formats.fontColorForEvaluation(result));
        return cell;
    }

    @NotNull
    private static Cell create(@Nullable IBlockElement element, int rows, int cols) {
        Cell cell = new Cell(rows, cols);
        cell.setBorder(Border.NO_BORDER);
        if (element != null) {
            cell.add(element);
        }
        return cell;
    }
}
