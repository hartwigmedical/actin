package com.hartwig.actin.report.pdf.tables;

import java.util.StringJoiner;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition;
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary;
import com.hartwig.actin.datamodel.clinical.PriorTumorTreatment;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class PatientClinicalHistoryGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord record;
    private final float keyWidth;
    private final float valueWidth;

    public PatientClinicalHistoryGenerator(@NotNull final ClinicalRecord record, final float keyWidth, final float valueWidth) {
        this.record = record;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Patient clinical history (" + Formats.date(record.patient().questionnaireDate()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { keyWidth, valueWidth });

        table.addCell(Cells.createKey("Relevant systemic treatment history"));
        table.addCell(Cells.createValue(relevantSystemicPreTreatmentHistory(record)));

        table.addCell(Cells.createKey("Other oncological history"));
        table.addCell(Cells.createValue(otherOncologicalHistory(record)));

        table.addCell(Cells.createKey("Relevant non-oncological history"));
        table.addCell(Cells.createValue(relevantNonOncologicalHistory(record)));

        return table;
    }

    @NotNull
    private static String relevantSystemicPreTreatmentHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (priorTumorTreatment.isSystemic()) {
                joiner.add(priorTumorTreatment.name());
            }
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String otherOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner otherOncologyHistories = Formats.commaJoiner();
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (!priorTumorTreatment.isSystemic()) {
                otherOncologyHistories.add(priorTumorTreatment.name());
            }
        }

        StringJoiner secondPrimaries = Formats.commaJoiner();
        for (PriorSecondPrimary priorSecondPrimary : record.priorSecondPrimaries()) {
            String secondPrimaryString = priorSecondPrimary.tumorLocation();
            if (priorSecondPrimary.diagnosedYear() != null) {
                secondPrimaryString = secondPrimaryString + " (" + priorSecondPrimary.diagnosedYear() + ")";
            }
            secondPrimaries.add(secondPrimaryString);
        }

        if (record.priorSecondPrimaries().size() > 1) {
            otherOncologyHistories.add("Previous primary tumors: " + secondPrimaries);
        } else if (!record.priorSecondPrimaries().isEmpty()) {
            otherOncologyHistories.add("Previous primary tumor: " + secondPrimaries);
        }

        return Formats.valueOrDefault(otherOncologyHistories.toString(), "None");
    }

    @NotNull
    private static String relevantNonOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorOtherCondition priorOtherCondition : record.priorOtherConditions()) {
            joiner.add(priorOtherCondition.name());
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
