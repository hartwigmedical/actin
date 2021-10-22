package com.hartwig.actin.report.pdf.tables;

import java.util.StringJoiner;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
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
        String nonSystemicHistory = relevantNonSystemicPreTreatmentHistory(record);
        String secondPrimaryHistory = secondPrimaryHistory(record);
        if (!nonSystemicHistory.isEmpty() && !secondPrimaryHistory.isEmpty()) {
            table.addCell(Cells.createValue(nonSystemicHistory));
            table.addCell(Cells.createEmpty());
            table.addCell(Cells.createValue(secondPrimaryHistory));
        } else if (!nonSystemicHistory.isEmpty()) {
            table.addCell(Cells.createValue(nonSystemicHistory));
        } else if (!secondPrimaryHistory.isEmpty()) {
            table.addCell(Cells.createValue(secondPrimaryHistory));
        } else {
            table.addCell(Cells.createValue("None"));
        }

        table.addCell(Cells.createKey("Relevant non-oncological history"));
        table.addCell(Cells.createValue(relevantNonOncologicalHistory(record)));

        return table;
    }

    @NotNull
    private static String relevantSystemicPreTreatmentHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (priorTumorTreatment.isSystemic()) {
                joiner.add(toTreatmentString(priorTumorTreatment));
            }
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String relevantNonSystemicPreTreatmentHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (!priorTumorTreatment.isSystemic()) {
                joiner.add(toTreatmentString(priorTumorTreatment));
            }
        }

        return joiner.toString();
    }

    @NotNull
    private static String toTreatmentString(@NotNull PriorTumorTreatment priorTumorTreatment) {
        String year = Strings.EMPTY;
        if (priorTumorTreatment.year() != null) {
            year = " (" + priorTumorTreatment.year() + ")";
        }

        String treatmentName = priorTumorTreatment.name() == "" ? priorTumorTreatment.category() : priorTumorTreatment.name() ;

        return treatmentName + year;
    }

    @NotNull
    private static String secondPrimaryHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorSecondPrimary priorSecondPrimary : record.priorSecondPrimaries()) {
            String tumorDetails = priorSecondPrimary.tumorLocation();
            if (!priorSecondPrimary.tumorType().isEmpty()) {
                tumorDetails = tumorDetails + " " + priorSecondPrimary.tumorType();
            }

            String year = Strings.EMPTY;
            if (priorSecondPrimary.diagnosedYear() != null) {
                year = priorSecondPrimary.diagnosedYear() + ", ";
            }

            String active = priorSecondPrimary.isActive() ? "considered active" : "considered non-active";

            joiner.add(tumorDetails + " (" + year + active + ")");
        }

        if (record.priorSecondPrimaries().size() > 1) {
            return "Previous primary tumors: " + joiner;
        } else if (!record.priorSecondPrimaries().isEmpty()) {
            return "Previous primary tumor: " + joiner;
        } else {
            return Strings.EMPTY;
        }
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
