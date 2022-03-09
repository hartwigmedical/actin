package com.hartwig.actin.report.pdf.tables;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return "Tumor details (" + Formats.date(record.patient().questionnaireDate()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Measurable disease"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.tumor().hasMeasurableDisease())));

        table.addCell(Cells.createKey("CNS lesion status"));
        table.addCell(Cells.createValue(cnsLesions(record.tumor())));

        table.addCell(Cells.createKey("Brain lesion status"));
        table.addCell(Cells.createValue(brainLesions(record.tumor())));

        return table;
    }

    @NotNull
    private static String cnsLesions(@NotNull TumorDetails tumor) {
        if (tumor.hasCnsLesions() == null) {
            return Formats.VALUE_UNKNOWN;
        }

        if (tumor.hasCnsLesions()) {
            return activeLesionString("CNS", tumor.hasActiveCnsLesions());
        } else {
            return "No known CNS lesions";
        }
    }

    @NotNull
    private static String brainLesions(@NotNull TumorDetails tumor) {
        if (tumor.hasBrainLesions() == null) {
            return Formats.VALUE_UNKNOWN;
        }

        if (tumor.hasBrainLesions()) {
            return activeLesionString("Brain", tumor.hasActiveBrainLesions());
        } else {
            return "No known brain lesions";
        }
    }

    @NotNull
    private static String activeLesionString(@NotNull String type, @Nullable Boolean active) {
        String activeString = Strings.EMPTY;
        if (active != null) {
            activeString = active ? " (active)" : " (not active)";
        }

        return type + activeString;
    }
}
