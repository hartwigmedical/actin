package com.hartwig.actin.report.pdf.tables;

import java.util.StringJoiner;

import com.hartwig.actin.datamodel.clinical.CancerRelatedComplication;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.Toxicity;
import com.hartwig.actin.datamodel.clinical.ToxicitySource;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Clinical;
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
        return "Patient current details (" + Clinical.questionnaireDate(record) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { keyWidth, valueWidth });

        table.addCell(Cells.createKey("WHO status"));
        table.addCell(Cells.createValue(String.valueOf(record.clinicalStatus().who())));

        table.addCell(Cells.createKey("Unresolved toxicities grade => 2"));
        table.addCell(Cells.createValue(unresolvedToxicities(record)));

        table.addCell(Cells.createKey("Significant infection"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.clinicalStatus().hasActiveInfection())));

        Boolean hasAberration = record.clinicalStatus().hasSigAberrationLatestEcg();
        if (hasAberration != null && hasAberration) {
            table.addCell(Cells.createKey("Significant aberration on latest ECG"));
            String ecg = record.clinicalStatus().ecgAberrationDescription();
            table.addCell(Cells.createValue(ecg != null ? ecg : Strings.EMPTY));
        }

        table.addCell(Cells.createKey("Cancer-related complications"));
        table.addCell(Cells.createValue(cancerRelatedComplications(record)));

        return table;
    }

    @NotNull
    private static String unresolvedToxicities(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.stringJoiner();
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
        StringJoiner joiner = Formats.stringJoiner();
        for (CancerRelatedComplication complication : record.cancerRelatedComplications()) {
            joiner.add(complication.name());
        }
        return Formats.valueOrDefault(joiner.toString(), "No");
    }
}
