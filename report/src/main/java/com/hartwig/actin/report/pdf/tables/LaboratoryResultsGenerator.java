package com.hartwig.actin.report.pdf.tables;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaboratoryResultsGenerator implements TableGenerator {

    @NotNull
    private final LabInterpretation labInterpretation;

    private final float key1Width;
    private final float key2Width;
    private final float key3Width;
    private final float valueWidth;

    @NotNull
    public static LaboratoryResultsGenerator fromRecord(@NotNull ClinicalRecord record, float keyWidth, float valueWidth) {
        float key1Width = keyWidth / 3;
        float key2Width = keyWidth / 3;
        float key3Width = keyWidth - key1Width - key2Width;

        return new LaboratoryResultsGenerator(LabInterpreter.interpret(record.labValues()), key1Width, key2Width, key3Width, valueWidth);
    }

    private LaboratoryResultsGenerator(@NotNull final LabInterpretation labInterpretation, final float key1Width, final float key2Width,
            final float key3Width, final float valueWidth) {
        this.labInterpretation = labInterpretation;
        this.key1Width = key1Width;
        this.key2Width = key2Width;
        this.key3Width = key3Width;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Laboratory results (" + Formats.date(labInterpretation.mostRecentRelevantDate()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { key1Width, key2Width, key3Width, valueWidth });

        table.addCell(Cells.createKey("Liver function"));
        addLabMeasurement(table, LabMeasurement.TOTAL_BILIRUBIN, "Total bilirubin");
        table.addCell(Cells.createEmpty());
        addLabMeasurement(table, LabMeasurement.ASAT, "ASAT");
        table.addCell(Cells.createEmpty());
        addLabMeasurement(table, LabMeasurement.ALAT, "ALAT");
        table.addCell(Cells.createEmpty());
        addLabMeasurement(table, LabMeasurement.ALP, "ALP");
        table.addCell(Cells.createEmpty());
        addLabMeasurement(table, LabMeasurement.ALBUMIN, "Albumin");

        table.addCell(Cells.createKey("Kidney function"));
        addLabMeasurement(table, LabMeasurement.CREATININE, "Creatinine");
        table.addCell(Cells.createEmpty());
        addLabMeasurement(table, LabMeasurement.CDK_EPI_EGFR, "CDK-EPI eGFR");

        table.addCell(Cells.createKey("Other"));
        addLabMeasurement(table, LabMeasurement.HEMOGLOBIN, "Hemoglobin");
        table.addCell(Cells.createEmpty());
        addLabMeasurement(table, LabMeasurement.THROMBOCYTES, "Thrombocytes");
        table.addCell(Cells.createEmpty());
        addLabMeasurement(table, LabMeasurement.LDH, "LDH");

        return table;
    }

    private void addLabMeasurement(@NotNull Table table, @NotNull LabMeasurement measurement, @NotNull String name) {
        LabValue lab = labInterpretation.mostRecentValue(measurement);
        String value = Strings.EMPTY;

        Style style = Styles.tableValueHighlightStyle();
        if (lab != null) {
            value = Formats.number(lab.value()) + " " + lab.unit();
            if (!lab.comparator().isEmpty()) {
                value = lab.comparator() + " " + value;
            }

            if (!labInterpretation.mostRecentRelevantDate().equals(lab.date())) {
                value = value + " (" + Formats.date(lab.date()) + ")";
            }

            if (lab.isOutsideRef() != null && lab.isOutsideRef()) {
                style = Styles.tableValueWarnStyle();
                value = value + " (" + buildOutOfRangeAddition(labInterpretation.allValuesForType(lab)) + ")";
            }
        }

        table.addCell(Cells.createKey(name));
        table.addCell(Cells.createKey(buildLimitString(lab)));
        table.addCell(Cells.create(new Paragraph(value).addStyle(style)));
    }

    @NotNull
    private static String buildLimitString(@Nullable LabValue lab) {
        if (lab == null) {
            return Strings.EMPTY;
        }

        Double refLimitLow = lab.refLimitLow();
        Double refLimitUp = lab.refLimitUp();

        if (refLimitLow == null && refLimitUp == null) {
            return Strings.EMPTY;
        }

        String limit;
        if (refLimitLow == null) {
            limit = "< " + Formats.number(refLimitUp);
        } else if (refLimitUp == null) {
            limit = "> " + Formats.number(refLimitLow);
        } else {
            limit = Formats.number(refLimitLow) + " - " + Formats.number(refLimitUp);
        }

        return "(" + limit + " " + lab.unit() + ")";
    }

    @NotNull
    private static String buildOutOfRangeAddition(@NotNull List<LabValue> values) {
        values.sort(new LabValueDescendingDateComparator());

        assert values.get(0).isOutsideRef();

        int outOfRangeCount = 1;
        double moreRecentValue = values.get(0).value();
        boolean trendIsUp = true;
        boolean trendIsDown = true;

        boolean inOutOfRefChain = true;
        int index = 1;
        while (inOutOfRefChain && index < values.size()) {
            LabValue lab = values.get(index);

            Boolean isOutsideRef = lab.isOutsideRef();
            if (isOutsideRef != null && isOutsideRef) {
                outOfRangeCount++;
                if (moreRecentValue > lab.value()) {
                    trendIsDown = false;
                } else {
                    trendIsUp = false;
                }
                moreRecentValue = lab.value();
            } else {
                inOutOfRefChain = false;
            }

            index++;
        }

        if (outOfRangeCount > 1) {
            assert !(trendIsUp && trendIsDown);

            String trend;
            if (trendIsUp) {
                trend = "trend is up";
            } else if (trendIsDown) {
                trend = "trend down";
            } else {
                trend = "no trend detected";
            }

            return "out of range for " + outOfRangeCount + " cons. measurements, " + trend;
        } else {
            return "no trend information available";
        }
    }
}
