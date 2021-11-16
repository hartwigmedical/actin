package com.hartwig.actin.report.pdf.util;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Tables {

    private Tables() {
    }

    @NotNull
    public static Table createFixedWidthCols(@NotNull float[] widths) {
        return new Table(UnitValue.createPointArray(widths));
    }

    @NotNull
    public static Table createSingleColWithWidth(float width) {
        return new Table(UnitValue.createPercentArray(new float[] { 1 })).setWidth(width);
    }

    public static void addNoneEntry(@NotNull Table table) {
        table.addCell(new Cell(1, table.getNumberOfColumns()).setBorder(Border.NO_BORDER)
                .add(new Paragraph("None"))
                .addStyle(Styles.tableContentStyle()));
    }

    @NotNull
    public static Table makeWrapping(@NotNull Table table) {
        return makeWrapping(table, null);
    }

    @NotNull
    public static Table makeWrapping(@NotNull Table table, @Nullable String title) {
        table.addFooterCell(new Cell(1, table.getNumberOfColumns()).setBorder(Border.NO_BORDER)
                .setPaddingTop(5)
                .setPaddingBottom(5)
                .add(new Paragraph("The table continues on the next page").addStyle(Styles.tableSubStyle()))).setSkipLastFooter(true);

        Table wrappingTable = new Table(1).setMinWidth(table.getWidth())
                .addHeaderCell(Cells.createSubNote("Continued from the previous page"))
                .setSkipFirstHeader(true)
                .addCell(Cells.create(table).setPadding(0));

        Table finalTable = new Table(1).setMinWidth(table.getWidth());
        if (title != null) {
            finalTable.addHeaderCell(Cells.createTitle(title));
        }
        finalTable.addCell(Cells.create(wrappingTable).setPadding(0));
        return finalTable;
    }
}
