package com.hartwig.actin.report.pdf.tables;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.TumorStage;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Clinical;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class TumorDetailsGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord record;
    private final float keyWidth;
    private final float valueWidth;

    public TumorDetailsGenerator(@NotNull final ClinicalRecord record, final float keyWidth, final float valueWidth) {
        this.record = record;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Tumor details (" + Clinical.questionnaireDate(record) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { keyWidth, valueWidth });

        table.addCell(Cells.createKey("Stage"));
        table.addCell(Cells.createValue(stage(record)));

        table.addCell(Cells.createKey("Primary tumor location"));
        table.addCell(Cells.createValue("TODO"));

        table.addCell(Cells.createKey("Primary tumor type"));
        table.addCell(Cells.createValue("TODO"));

        table.addCell(Cells.createKey("Biopsy location"));
        table.addCell(Cells.createValue("TODO"));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createEmpty());

        table.addCell(Cells.createKey("Lesions in CNS / Brain / Bone  / Liver"));
        table.addCell(Cells.createValue("TODO"));

        table.addCell(Cells.createKey("Other lesions"));
        table.addCell(Cells.createValue("TODO"));

        table.addCell(Cells.createKey("Measurable disease (RECIST)"));
        table.addCell(Cells.createValue("TODO"));

        return table;
    }

    @NotNull
    private static String stage(@NotNull ClinicalRecord record) {
        TumorStage stage = record.tumor().stage();
        return stage != null ? stage.display() : "Unknown";
    }
}
