package com.hartwig.actin.report.pdf.tables;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class LaboratoryTableGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord record;
    @NotNull
    private final float[] widths;

    public LaboratoryTableGenerator(@NotNull final ClinicalRecord record, @NotNull final float[] widths) {
        this.record = record;
        this.widths = widths;
    }

    @NotNull
    @Override
    public String title() {
        return "Laboratory";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(widths);

        return table;
    }
}
