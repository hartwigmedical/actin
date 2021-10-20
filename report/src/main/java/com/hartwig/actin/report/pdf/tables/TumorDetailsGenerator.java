package com.hartwig.actin.report.pdf.tables;

import java.util.StringJoiner;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

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
        Table table = Tables.createFixedWidthCols(new float[] { keyWidth, valueWidth });

        table.addCell(Cells.createKey("Stage"));
        table.addCell(Cells.createValue(stage(record.tumor())));

        table.addCell(Cells.createKey("Primary tumor location"));
        table.addCell(Cells.createValue(tumorLocation(record.tumor())));

        table.addCell(Cells.createKey("Primary tumor type"));
        table.addCell(Cells.createValue(tumorType(record.tumor())));

        table.addCell(Cells.createKey("Biopsy location"));
        table.addCell(Cells.createValue(biopsyLocation(record.tumor())));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createEmpty());

        table.addCell(Cells.createKey("Lesions in CNS / Brain / Bone  / Liver"));
        table.addCell(Cells.create(lesionsParagraph(record.tumor())));

        table.addCell(Cells.createKey("Other lesions"));
        table.addCell(Cells.createValue(lesionsOther(record.tumor())));

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
    private static String tumorLocation(@NotNull TumorDetails tumor) {
        String tumorLocation = tumor.primaryTumorLocation();

        if (tumorLocation != null) {
            String tumorSubLocation = tumor.primaryTumorSubLocation();
            return (tumorSubLocation != null && !tumorSubLocation.isEmpty())
                    ? tumorLocation + " (" + tumorSubLocation + ")"
                    : tumorLocation;
        }

        return Formats.VALUE_UNKNOWN;
    }

    @NotNull
    private static String tumorType(@NotNull TumorDetails tumor) {
        String tumorType = tumor.primaryTumorType();

        if (tumorType != null) {
            String tumorSubType = tumor.primaryTumorSubType();
            return (tumorSubType != null && !tumorSubType.isEmpty()) ? tumorType + " (" + tumorSubType + ")" : tumorType;
        }

        return Formats.VALUE_UNKNOWN;
    }

    @NotNull
    private static String biopsyLocation(@NotNull TumorDetails tumor) {
        String biopsyLocation = tumor.biopsyLocation();
        return biopsyLocation != null ? biopsyLocation : Formats.VALUE_UNKNOWN;
    }

    @NotNull
    private static Paragraph lesionsParagraph(@NotNull TumorDetails tumor) {
        Paragraph lesions = new Paragraph();

        lesions.add(renderStyledText(activeSymptomaticLesionString(tumor.hasCnsLesions(),
                tumor.hasActiveCnsLesions(),
                tumor.hasSymptomaticCnsLesions())));
        lesions.add(new Text(" / ").addStyle(Styles.labelStyle()));
        lesions.add(renderStyledText(activeSymptomaticLesionString(tumor.hasBrainLesions(),
                tumor.hasActiveBrainLesions(),
                tumor.hasSymptomaticBrainLesions())));

        lesions.add(new Text(" / ").addStyle(Styles.labelStyle()));
        lesions.add(renderStyledText(Formats.yesNoUnknown(tumor.hasBoneLesions())));
        lesions.add(new Text(" / ").addStyle(Styles.labelStyle()));
        lesions.add(renderStyledText(Formats.yesNoUnknown(tumor.hasLiverLesions())));

        return lesions;
    }

    @NotNull
    private static String activeSymptomaticLesionString(@Nullable Boolean hasLesions, @Nullable Boolean active,
            @Nullable Boolean symptomatic) {
        String lesionAddon = Strings.EMPTY;
        if (hasLesions) {
            String activeString = Strings.EMPTY;
            if (active != null) {
                activeString = active ? "active" : "not active";
            }

            String symptomaticString = Strings.EMPTY;
            if (symptomatic != null) {
                symptomaticString = symptomatic ? "symptomatic" : "not symptomatic";
            }

            if (!activeString.isEmpty() || !symptomaticString.isEmpty()) {
                if (activeString.isEmpty()) {
                    lesionAddon = " (" + symptomaticString + ")";
                } else if (symptomaticString.isEmpty()) {
                    lesionAddon = " (" + activeString + ")";
                } else {
                    lesionAddon = " (" + activeString + ", " + symptomaticString + ")";
                }
            }
        }
        return Formats.yesNoUnknown(hasLesions) + lesionAddon;
    }

    @NotNull
    private static Text renderStyledText(@NotNull String value) {
        Style style = !value.equals(Formats.VALUE_UNKNOWN) ? Styles.tableValueStyle() : Styles.labelStyle();
        return new Text(value).addStyle(style);
    }

    @NotNull
    private static String lesionsOther(@NotNull TumorDetails tumor) {
        Boolean hasOtherLesions = tumor.hasOtherLesions();
        if (hasOtherLesions == null) {
            return Formats.VALUE_UNKNOWN;
        }

        if (hasOtherLesions) {
            assert tumor.otherLesions() != null && !tumor.otherLesions().isEmpty();
            StringJoiner joiner = Formats.commaJoiner();
            for (String lesion : tumor.otherLesions()) {
                joiner.add(lesion);
            }
            return joiner.toString();
        } else {
            return "No";
        }
    }
}
