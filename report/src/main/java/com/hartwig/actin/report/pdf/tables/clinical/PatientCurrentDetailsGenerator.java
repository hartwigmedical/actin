package com.hartwig.actin.report.pdf.tables.clinical;

import java.util.List;
import java.util.StringJoiner;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ECGMeasure;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatientCurrentDetailsGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord record;
    private final float keyWidth;
    private final float valueWidth;

    public PatientCurrentDetailsGenerator(@NotNull final ClinicalRecord record, final float keyWidth,
            final float valueWidth) {
        this.record = record;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Patient current details (" + Formats.date(record.patient().questionnaireDate()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Unresolved toxicities grade => 2"));
        table.addCell(Cells.createValue(unresolvedToxicities(record)));

        InfectionStatus infectionStatus = record.clinicalStatus().infectionStatus();
        if (infectionStatus != null && infectionStatus.hasActiveInfection()) {
            table.addCell(Cells.createKey("Significant infection"));
            String description = infectionStatus.description() != null ? infectionStatus.description() : "Unknown";
            table.addCell(Cells.createValue(description));
        }

        ECG ecg = record.clinicalStatus().ecg();
        if (ecg != null && ecg.hasSigAberrationLatestECG()) {
            if (ecg.hasSigAberrationLatestECG()) {
                table.addCell(Cells.createKey("Significant aberration on latest ECG"));
                String aberration = ecg.aberrationDescription();
                String description = aberration != null ? aberration : "Yes (ECG aberration details unknown)";
                table.addCell(Cells.createValue(description));
            }

            ECGMeasure qtcfMeasure = ecg.qtcfMeasure();
            if (qtcfMeasure != null) {
                createMeasureCells(table, "QTcF", qtcfMeasure);
            }

            ECGMeasure jtcMeasure = ecg.jtcMeasure();
            if (qtcfMeasure != null) {
                createMeasureCells(table, "JTc", jtcMeasure);
            }
        }

        if (record.clinicalStatus().lvef() != null) {
            table.addCell(Cells.createKey("LVEF"));
            table.addCell(Cells.createValue(Formats.percentage(record.clinicalStatus().lvef())));
        }

        table.addCell(Cells.createKey("Cancer-related complications"));
        table.addCell(Cells.createValue(complications(record.complications())));

        table.addCell(Cells.createKey("Known allergies"));
        table.addCell(Cells.createValue(allergies(record.intolerances())));

        if (!record.surgeries().isEmpty()) {
            table.addCell(Cells.createKey("Recent surgeries"));
            table.addCell(Cells.createValue(surgeries(record.surgeries())));
        }

        return table;
    }

    private static void createMeasureCells(final Table table, final String key, final ECGMeasure measure) {
        table.addCell(Cells.createKey(key));
        table.addCell(Cells.createValue(Formats.twoDigitNumber(measure.value())) + " " + measure.unit());
    }

    //TODO: For source EHR, only consider the most recent value of each toxicity and write these with "From EHR: " for clarity
    @NotNull
    private static String unresolvedToxicities(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (Toxicity toxicity : record.toxicities()) {
            Integer grade = toxicity.grade();
            if ((grade != null && grade >= 2) || toxicity.source() == ToxicitySource.QUESTIONNAIRE) {
                String gradeString = grade != null ? " (" + grade + ")" : Strings.EMPTY;
                joiner.add(toxicity.name() + gradeString);
            }
        }

        return Formats.valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String complications(@Nullable List<Complication> complications) {
        StringJoiner joiner = Formats.commaJoiner();
        if (complications == null) {
            return "Unknown";
        }

        for (Complication complication : complications) {

            String date = toDateString(complication.year(), complication.month());
            String dateAddition = Strings.EMPTY;
            if (date != null) {
                dateAddition = " (" + date + ")";
            }

            joiner.add(complication.name() + dateAddition);
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }

    @Nullable
    private static String toDateString(@Nullable Integer year, @Nullable Integer month) {
        if (year != null) {
            return month != null ? month + "/" + year : String.valueOf(year);
        } else {
            return null;
        }
    }

    @NotNull
    private static String allergies(@NotNull List<Intolerance> intolerances) {
        StringJoiner joiner = Formats.commaJoiner();
        for (Intolerance intolerance : intolerances) {
            if (!intolerance.name().equalsIgnoreCase("none")) {
                String addition =
                        !intolerance.category().isEmpty() ? " (" + intolerance.category() + ")" : Strings.EMPTY;
                joiner.add(intolerance.name() + addition);
            }
        }

        return Formats.valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String surgeries(@NotNull List<Surgery> surgeries) {
        StringJoiner joiner = Formats.commaJoiner();
        for (Surgery surgery : surgeries) {
            joiner.add(Formats.date(surgery.endDate()));
        }

        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
