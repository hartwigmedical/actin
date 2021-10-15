package com.hartwig.actin.report.pdf.util;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Paragraph;

import org.jetbrains.annotations.NotNull;

public final class Cells {

    private Cells() {
    }

    @NotNull
    public static Cell createCell(@NotNull IBlockElement element) {
        Cell cell = new Cell();
        cell.setKeepTogether(true);
        cell.setBorder(Border.NO_BORDER);
        cell.add(element);
        return cell;
    }

    @NotNull
    public static Cell createTitleCell(@NotNull String text) {
        Cell cell = createCell(new Paragraph(text));
        cell.addStyle(Styles.tableTitleStyle());
        return cell;
    }

    @NotNull
    public static Cell createKeyCell(@NotNull String text) {
        Cell cell = createCell(new Paragraph(text));
        cell.addStyle(Styles.tableKeyStyle());
        return cell;
    }

    @NotNull
    public static Cell createValueCell(@NotNull String text) {
        Cell cell = createCell(new Paragraph(text));
        cell.addStyle(Styles.tableValueStyle());
        return cell;
    }
}
