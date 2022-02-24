package com.hartwig.actin.report.pdf.tables;

import java.util.List;
import java.util.StringJoiner;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
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
        return "Tumor localization details (" + Formats.date(record.patient().questionnaireDate()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Tumor stage"));
        table.addCell(Cells.createValue(stage(record.tumor())));

        table.addCell(Cells.createKey("Lesion locations"));
        table.addCell(Cells.createValue(lesionInformation(record.tumor())));

        table.addCell(Cells.createKey("Measurable disease (RECIST)"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.tumor().hasMeasurableLesionRecist())));

        return table;
    }

    @NotNull
    private static String stage(@NotNull TumorDetails tumor) {
        TumorStage stage = tumor.stage();
        return stage != null ? stage.display() : Formats.VALUE_UNKNOWN;
    }

    @NotNull
    private static String lesionInformation(@NotNull TumorDetails tumor) {
        StringJoiner joiner = new StringJoiner(", ");

        Boolean hasCnsLesions = tumor.hasCnsLesions();
        if (hasCnsLesions != null && hasCnsLesions) {
            joiner.add(activeSymptomaticLesionString("CNS", tumor.hasActiveCnsLesions(), tumor.hasSymptomaticCnsLesions()));
        }

        Boolean hasBrainLesions = tumor.hasBrainLesions();
        if (hasBrainLesions != null && hasBrainLesions) {
            joiner.add(activeSymptomaticLesionString("Brain", tumor.hasActiveBrainLesions(), tumor.hasSymptomaticBrainLesions()));
        }

        Boolean hasLiverLesions = tumor.hasLiverLesions();
        if (hasLiverLesions != null && hasLiverLesions) {
            joiner.add("Liver");
        }

        Boolean hasBoneLesions = tumor.hasBoneLesions();
        if (hasBoneLesions != null && hasBoneLesions) {
            joiner.add("Bone");
        }

        Boolean hasOtherLesions = tumor.hasOtherLesions();
        List<String> otherLesions = tumor.otherLesions();
        if (hasOtherLesions != null && hasOtherLesions && otherLesions != null) {
            for (String lesion : otherLesions) {
                joiner.add(lesion);
            }
        }

        boolean hasDataAboutLesions = hasCnsLesions != null || hasBrainLesions != null || hasLiverLesions != null || hasBoneLesions != null
                || hasOtherLesions != null;
        if (hasDataAboutLesions) {
            return Formats.valueOrDefault(joiner.toString(), "None");
        } else {
            return Formats.VALUE_UNKNOWN;
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
