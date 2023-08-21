package com.hartwig.actin.report.pdf.tables.clinical;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class BloodTransfusionGenerator implements TableGenerator {

    @NotNull
    private final List<BloodTransfusion> bloodTransfusions;
    private final float totalWidth;

    public BloodTransfusionGenerator(@NotNull final List<BloodTransfusion> bloodTransfusions, final float totalWidth) {
        this.bloodTransfusions = bloodTransfusions;
        this.totalWidth = totalWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Blood transfusions";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(1, 1).setWidth(totalWidth);

        table.addHeaderCell(Cells.createHeader("Product"));
        table.addHeaderCell(Cells.createHeader("Date"));

        for (BloodTransfusion bloodTransfusion : bloodTransfusions) {
            table.addCell(Cells.createContent(bloodTransfusion.product()));
            table.addCell(Cells.createContent(Formats.date(bloodTransfusion.date())));
        }

        return Tables.makeWrapping(table);
    }
}
