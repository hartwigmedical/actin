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

    @NotNull
    public static Table addTitle(@NotNull Table contentTable, @Nullable String title) {
        Table table = new Table(1).setMinWidth(contentTable.getWidth());

        table.addCell(Cells.createTitle(title));
        table.addCell(Cells.create(contentTable));

        return table;
    }

    @NotNull
    public static Table makeWrapping(@NotNull Table table) {
        table.addFooterCell(new Cell(1, table.getNumberOfColumns()).setBorder(Border.NO_BORDER)
                .setPaddingTop(5)
                .setPaddingBottom(5)
                .add(new Paragraph("The table continues on the next page").addStyle(Styles.labelStyle())))
                .setSkipLastFooter(true)
                .setFixedLayout();

        Table wrappingTable = new Table(1).setMinWidth(table.getWidth())
                .addHeaderCell(new Cell().setBorder(Border.NO_BORDER)
                        .add(new Paragraph("Continued from the previous page").addStyle(Styles.labelStyle())))
                .setSkipFirstHeader(true)
                .addCell(new Cell().add(table).setPadding(0).setBorder(Border.NO_BORDER));

        Table finalTable = new Table(1).setMinWidth(table.getWidth()).setMarginBottom(20);
        finalTable.addCell(new Cell().add(wrappingTable).setPadding(0).setBorder(Border.NO_BORDER));
        return finalTable;
    }
}
