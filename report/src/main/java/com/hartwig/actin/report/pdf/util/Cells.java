package com.hartwig.actin.report.pdf.util;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Paragraph;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class Cells {

    private Cells() {
    }

    @NotNull
    public static Cell create(@NotNull IBlockElement element) {
        Cell cell = new Cell();
        cell.setKeepTogether(true);
        cell.setBorder(Border.NO_BORDER);
        cell.add(element);
        return cell;
    }

    @NotNull
    public static Cell createEmpty() {
        return create(new Paragraph(Strings.EMPTY));
    }

    @NotNull
    public static Cell createTitle(@NotNull String text) {
        Cell cell = create(new Paragraph(text));
        cell.addStyle(Styles.tableTitleStyle());
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
}
