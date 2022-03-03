package com.hartwig.actin.report.pdf.tables;

import java.util.List;
import java.util.StringJoiner;

import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class PatientCurrentDetailsGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord record;
    private final float keyWidth;
    private final float valueWidth;

    public PatientCurrentDetailsGenerator(@NotNull final ClinicalRecord record, final float keyWidth, final float valueWidth) {
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
            table.addCell(Cells.createValue(infectionStatus.description()));
        }

        ECG ecg = record.clinicalStatus().ecg();
        if (ecg != null && ecg.hasSigAberrationLatestECG()) {
            if (ecg.hasSigAberrationLatestECG()) {
                table.addCell(Cells.createKey("Significant aberration on latest ECG"));
                table.addCell(Cells.createValue(ecg.aberrationDescription()));
            }

            if (ecg.qtcfValue() != null && ecg.qtcfUnit() != null) {
                table.addCell(Cells.createKey("QTcF"));
                table.addCell(Cells.createValue(Formats.number(ecg.qtcfValue()) + " " + ecg.qtcfUnit()));
            }
        }

        if (record.clinicalStatus().lvef() != null) {
            table.addCell(Cells.createKey("LVEF"));
            table.addCell(Cells.createValue(Formats.percentage(record.clinicalStatus().lvef())));
        }

        table.addCell(Cells.createKey("Cancer-related complications"));
        table.addCell(Cells.createValue(complications(record.complications())));

        table.addCell(Cells.createKey("Known allergies"));
        table.addCell(Cells.createValue(allergies(record.allergies())));

        if (!record.surgeries().isEmpty()) {
            table.addCell(Cells.createKey("Recent surgeries"));
            table.addCell(Cells.createValue(surgeries(record.surgeries())));
        }

        return table;
    }

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
    private static String complications(@NotNull List<Complication> complications) {
        StringJoiner joiner = Formats.commaJoiner();
        for (Complication complication : complications) {
            joiner.add(complication.name());
        }

        return Formats.valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String allergies(@NotNull List<Allergy> allergies) {
        StringJoiner joiner = Formats.commaJoiner();
        for (Allergy allergy : allergies) {
            String addition = !allergy.category().isEmpty() ? " (" + allergy.category() + ")" : Strings.EMPTY;
            joiner.add(allergy.name() + addition);
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
