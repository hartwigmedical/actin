package com.hartwig.actin.report.pdf.tables.clinical;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
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

public class LabResultsGenerator implements TableGenerator {

    private static final int MAX_LAB_DATES = 5;

    @NotNull
    private final LabInterpretation labInterpretation;

    private final float key1Width;
    private final float key2Width;
    private final float key3Width;
    private final float valueWidth;

    @NotNull
    public static LabResultsGenerator fromRecord(@NotNull ClinicalRecord record, float keyWidth, float valueWidth) {
        float key1Width = keyWidth / 3;
        float key2Width = keyWidth / 3;
        float key3Width = keyWidth - key1Width - key2Width;

        return new LabResultsGenerator(LabInterpreter.interpret(record.labValues()), key1Width, key2Width, key3Width, valueWidth);
    }

    private LabResultsGenerator(@NotNull final LabInterpretation labInterpretation, final float key1Width, final float key2Width,
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
        return "Laboratory results";
    }

    @NotNull
    @Override
    public Table contents() {
        List<LocalDate> dates = labInterpretation.allDates()
                .stream()
                .sorted(Comparator.reverseOrder())
                .distinct()
                .limit(MAX_LAB_DATES)
                .sorted()
                .collect(Collectors.toList());

        Table table = Tables.createFixedWidthCols(defineWidths());

        table.addHeaderCell(Cells.createHeader(Strings.EMPTY));
        table.addHeaderCell(Cells.createHeader(Strings.EMPTY));
        table.addHeaderCell(Cells.createHeader(Strings.EMPTY));
        for (LocalDate date : dates) {
            table.addHeaderCell(Cells.createHeader(Formats.date(date)));
        }

        for (int i = dates.size(); i < MAX_LAB_DATES; i++) {
            table.addHeaderCell(Cells.createHeader(Strings.EMPTY));
        }

        table.addCell(Cells.createKey("Liver function"));
        table.addCell(Cells.createKey("Total bilirubin"));
        addLabMeasurements(table, dates, LabMeasurement.TOTAL_BILIRUBIN);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("ASAT"));
        addLabMeasurements(table, dates, LabMeasurement.ASPARTATE_AMINOTRANSFERASE);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("ALAT"));
        addLabMeasurements(table, dates, LabMeasurement.ALANINE_AMINOTRANSFERASE);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("ALP"));
        addLabMeasurements(table, dates, LabMeasurement.ALKALINE_PHOSPHATASE);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Albumin"));
        addLabMeasurements(table, dates, LabMeasurement.ALBUMIN);

        table.addCell(Cells.createKey("Kidney function"));
        table.addCell(Cells.createKey("Creatinine"));
        addLabMeasurements(table, dates, LabMeasurement.CREATININE);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("CKD-EPI eGFR"));
        addLabMeasurements(table, dates, LabMeasurement.EGFR_CKD_EPI);

        table.addCell(Cells.createKey("Other"));
        table.addCell(Cells.createKey("Hemoglobin"));
        addLabMeasurements(table, dates, LabMeasurement.HEMOGLOBIN);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Thrombocytes"));
        addLabMeasurements(table, dates, LabMeasurement.THROMBOCYTES_ABS);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("LDH"));
        addLabMeasurements(table, dates, LabMeasurement.LACTATE_DEHYDROGENASE);

        table.addCell(Cells.createKey("Tumor markers"));
        table.addCell(Cells.createKey("CA 15.3"));
        addLabMeasurements(table, dates, LabMeasurement.CA_153);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("CA 125"));
        addLabMeasurements(table, dates, LabMeasurement.CA_125);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("CA 19.9"));
        addLabMeasurements(table, dates, LabMeasurement.CA_199);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("CEA"));
        addLabMeasurements(table, dates, LabMeasurement.CEA);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("PSA"));
        addLabMeasurements(table, dates, LabMeasurement.PSA);

        if (labInterpretation.allDates().size() > MAX_LAB_DATES) {
            String note = "Note: Only the most recent " + MAX_LAB_DATES + " lab results have been displayed";
            table.addCell(Cells.createSpanningSubNote(note, table));
        }

        return table;
    }

    @NotNull
    private float[] defineWidths() {
        float[] widths = new float[3 + MAX_LAB_DATES];
        widths[0] = key1Width;
        widths[1] = key2Width;
        widths[2] = key3Width;
        for (int i = 0; i < MAX_LAB_DATES; i++) {
            widths[3 + i] = valueWidth / MAX_LAB_DATES;
        }
        return widths;
    }

    private void addLabMeasurements(@NotNull Table table, @NotNull List<LocalDate> dates, @NotNull LabMeasurement measurement) {
        table.addCell(Cells.createKey(buildLimitString(labInterpretation.mostRecentValue(measurement))));

        for (LocalDate date : dates) {
            StringJoiner joiner = Formats.commaJoiner();
            Style style = Styles.tableHighlightStyle();
            for (LabValue lab : labInterpretation.valuesOnDate(measurement, date)) {
                String value = Formats.twoDigitNumber(lab.value()) + " " + lab.unit().display();
                if (!lab.comparator().isEmpty()) {
                    value = lab.comparator() + " " + value;
                }
                joiner.add(value);

                if (lab.isOutsideRef() != null && Boolean.TRUE.equals(lab.isOutsideRef())) {
                    style = Styles.tableWarnStyle();
                }
            }
            table.addCell(Cells.create(new Paragraph(joiner.toString()).addStyle(style)));
        }

        for (int i = dates.size(); i < MAX_LAB_DATES; i++) {
            table.addCell(Cells.createEmpty());
        }
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
            limit = "< " + Formats.twoDigitNumber(refLimitUp);
        } else if (refLimitUp == null) {
            limit = "> " + Formats.twoDigitNumber(refLimitLow);
        } else {
            limit = Formats.twoDigitNumber(refLimitLow) + " - " + Formats.twoDigitNumber(refLimitUp);
        }

        return "(" + limit + " " + lab.unit().display() + ")";
    }
}
