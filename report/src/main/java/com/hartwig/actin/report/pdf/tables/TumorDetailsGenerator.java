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

        table.addCell(Cells.createKey("Measurable disease (RECIST)"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.tumor().hasMeasurableLesionRecist())));

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
            return activeSymptomaticLesionString("CNS", tumor.hasActiveCnsLesions(), tumor.hasSymptomaticCnsLesions());
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
            return activeSymptomaticLesionString("Brain", tumor.hasActiveBrainLesions(), tumor.hasSymptomaticBrainLesions());
        } else {
            return "No known brain lesions";
        }
    }

    @NotNull
    private static String activeSymptomaticLesionString(@NotNull String type, @Nullable Boolean active, @Nullable Boolean symptomatic) {
        String activeString = Strings.EMPTY;
        if (active != null) {
            activeString = active ? "active" : "not active";
        }

        String symptomaticString = Strings.EMPTY;
        if (symptomatic != null) {
            symptomaticString = symptomatic ? "symptomatic" : "not symptomatic";
        }

        String lesionAddon = Strings.EMPTY;
        if (!activeString.isEmpty() || !symptomaticString.isEmpty()) {
            if (activeString.isEmpty()) {
                lesionAddon = " (" + symptomaticString + ")";
            } else if (symptomaticString.isEmpty()) {
                lesionAddon = " (" + activeString + ")";
            } else {
                lesionAddon = " (" + activeString + ", " + symptomaticString + ")";
            }
        }

        return type + lesionAddon;
    }

}
