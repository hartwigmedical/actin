package com.hartwig.actin.report.pdf.tables;

import java.util.StringJoiner;

import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ECGAberration;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
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
        Table table = Tables.createFixedWidthCols(new float[] { keyWidth, valueWidth });

        table.addCell(Cells.createKey("WHO status"));
        table.addCell(Cells.createValue(whoStatus(record.clinicalStatus().who())));

        table.addCell(Cells.createKey("Unresolved toxicities grade => 2"));
        table.addCell(Cells.createValue(unresolvedToxicities(record)));

        table.addCell(Cells.createKey("Significant infection"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.clinicalStatus().hasActiveInfection())));

        ECGAberration ecgAberration = record.clinicalStatus().ecgAberration();
        if (ecgAberration != null && ecgAberration.hasSigAberrationLatestECG()) {
            table.addCell(Cells.createKey("Significant aberration on latest ECG"));
            table.addCell(Cells.createValue(ecgAberration.description()));
        }

        table.addCell(Cells.createKey("Cancer-related complications"));
        table.addCell(Cells.createValue(cancerRelatedComplications(record)));

        return table;
    }

    @NotNull
    private static String whoStatus(@Nullable Integer who) {
        return who != null ? String.valueOf(who) : Formats.VALUE_UNKNOWN;
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
    private static String cancerRelatedComplications(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (CancerRelatedComplication complication : record.cancerRelatedComplications()) {
            joiner.add(complication.name());
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
