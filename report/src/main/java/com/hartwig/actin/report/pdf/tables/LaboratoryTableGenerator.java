package com.hartwig.actin.report.pdf.tables;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class LaboratoryTableGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord record;
    private final float keyWidth;
    private final float valueWidth;

    public LaboratoryTableGenerator(@NotNull final ClinicalRecord record, final float keyWidth, final float valueWidth) {
        this.record = record;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Laboratory";
    }

    @NotNull
    @Override
    public Table contents() {
        float key1Width = keyWidth / 2;
        float key2Width = keyWidth - key1Width;

        Table table = Tables.createFixedWidthCols(new float[] { key1Width, key2Width, valueWidth });

        table.addCell(Cells.createKey("Liver function"));
        table.addCell(Cells.createKey("Total bilirubin (ref range + unit)"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("ASAT (ref range + unit)"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("ALAT (ref range + unit)"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("ALP (ref range + unit)"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Albumin"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createKey("Kidney function"));
        table.addCell(Cells.createKey("Creatinine (ref range + unit)"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("CKD-EPI eGFR (ref range + unit)"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createKey("Hemoglobin"));
        table.addCell(Cells.createKey("ref range + unit"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createKey("Thrombocytes"));
        table.addCell(Cells.createKey("ref range + unit"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createKey("PT"));
        table.addCell(Cells.createKey("ref range + unit"));
        table.addCell(Cells.createValue("some value"));

        table.addCell(Cells.createKey("INR"));
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createValue("some value"));

        return table;
    }
}
