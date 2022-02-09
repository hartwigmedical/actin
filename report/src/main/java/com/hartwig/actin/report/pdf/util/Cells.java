package com.hartwig.actin.report.pdf.util;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class Cells {

    private Cells() {
    }

    @NotNull
    public static Cell create(@NotNull IBlockElement element) {
        return create(element, 1, 1);
    }

    @NotNull
    public static Cell createEmpty() {
        return create(new Paragraph(Strings.EMPTY));
    }

    @NotNull
    public static Cell createSpanningNoneEntry(@NotNull Table table) {
        Cell cell = create(new Paragraph("None"), 1, table.getNumberOfColumns());
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
    public static Cell createContent(@NotNull String text) {
        Cell cell = create(new Paragraph(text));
        cell.addStyle(Styles.tableContentStyle());
        cell.setBorderBottom(new SolidBorder(Styles.PALETTE_MID_GREY, 0.25F));
        return cell;
    }

    @NotNull
    public static Cell createContent(@NotNull Evaluation evaluation) {
        Cell cell = createContent(evaluation.toString());
        cell.setFontColor(Formats.fontColorForEvaluation(evaluation));
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
    public static Cell createValue(@NotNull Evaluation evaluation) {
        Cell cell = createValue(evaluation.toString());
        cell.setFontColor(Formats.fontColorForEvaluation(evaluation));
        return cell;
    }

    @NotNull
    private static Cell create(@NotNull IBlockElement element, int rows, int cols) {
        Cell cell = new Cell(rows, cols);
        cell.setBorder(Border.NO_BORDER);
        cell.add(element);
        return cell;
    }
}
