package com.hartwig.actin.report.pdf.tables.clinical;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.clinical.sort.PriorTumorTreatmentDescendingDateComparator;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

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
        return priorTumorTreatmentString(record.priorTumorTreatments(), true);
    }

    @NotNull
    private static String relevantNonSystemicPreTreatmentHistory(@NotNull ClinicalRecord record) {
        return priorTumorTreatmentString(record.priorTumorTreatments(), false);
    }

    @NotNull
    private static String priorTumorTreatmentString(@NotNull List<PriorTumorTreatment> priorTumorTreatments, boolean requireSystemic) {
        List<PriorTumorTreatment> filtered = Lists.newArrayList();
        for (PriorTumorTreatment priorTumorTreatment : priorTumorTreatments) {
            if (priorTumorTreatment.isSystemic() == requireSystemic) {
                filtered.add(priorTumorTreatment);
            }
        }
        filtered.sort(new PriorTumorTreatmentDescendingDateComparator());

        StringJoiner joiner = Formats.commaJoiner();
        Set<String> evaluatedNames = Sets.newHashSet();
        for (PriorTumorTreatment priorTumorTreatment : filtered) {
            String treatmentName = treatmentName(priorTumorTreatment);
            if (!evaluatedNames.contains(treatmentName)) {
                StringJoiner annotationJoiner = Formats.semicolonJoiner();
                for (String annotation : extractAnnotationsForTreatmentName(filtered, treatmentName)) {
                    annotationJoiner.add(annotation);
                }
                String annotationAddition = annotationJoiner.toString();
                String treatment = treatmentName + (!annotationAddition.isEmpty() ? " (" + annotationAddition + ")" : "");

                joiner.add(treatment);
            }
            evaluatedNames.add(treatmentName);
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static Set<String> extractAnnotationsForTreatmentName(@NotNull List<PriorTumorTreatment> priorTumorTreatments,
            @NotNull String treatmentNameToInclude) {
        Set<String> annotations = Sets.newTreeSet();
        for (PriorTumorTreatment priorTumorTreatment : priorTumorTreatments) {
            String treatmentName = treatmentName(priorTumorTreatment);
            if (treatmentName.equals(treatmentNameToInclude)) {
                StringJoiner joiner = Formats.commaJoiner();

                String dateString = toDateString(priorTumorTreatment.startYear(), priorTumorTreatment.startMonth());
                if (dateString != null ) {
                    joiner.add(dateString);
                }

                String stopReasonString = toStopReasonString(priorTumorTreatment.stopReason());
                if (stopReasonString != null) {
                    joiner.add(stopReasonString);
                }

                String annotation = joiner.toString();
                if (!annotation.isEmpty()) {
                    annotations.add(annotation);
                }
            }
        }
        return annotations;
    }

    @NotNull
    private static String treatmentName(@NotNull PriorTumorTreatment priorTumorTreatment) {
        return !priorTumorTreatment.name().isEmpty()
                ? priorTumorTreatment.name()
                : TreatmentCategoryResolver.toStringList(priorTumorTreatment.categories());
    }

    @NotNull
    private static String secondPrimaryHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorSecondPrimary priorSecondPrimary : record.priorSecondPrimaries()) {
            String tumorDetails = priorSecondPrimary.tumorLocation();
            if (!priorSecondPrimary.tumorSubType().isEmpty()) {
                tumorDetails = tumorDetails + " " + priorSecondPrimary.tumorSubType();
            } else if (priorSecondPrimary.tumorSubType().isEmpty() && !priorSecondPrimary.tumorType().isEmpty()) {
                tumorDetails = tumorDetails + " " + priorSecondPrimary.tumorType();
            }

            String dateDiagnosis = toDateString(priorSecondPrimary.diagnosedYear(), priorSecondPrimary.diagnosedMonth());
            String dateAdditionDiagnosis = Strings.EMPTY;
            if (dateDiagnosis != null) {
                dateAdditionDiagnosis = "diagnosed " + dateDiagnosis + ", ";
            }

            String dateLastTreatment = toDateString(priorSecondPrimary.lastTreatmentYear(), priorSecondPrimary.lastTreatmentMonth());
            String dateAdditionLastTreatment = Strings.EMPTY;
            if (dateLastTreatment != null) {
                dateAdditionLastTreatment = "last treatment " + dateLastTreatment + ", ";
            }

            String active = priorSecondPrimary.isActive() ? "considered active" : "considered non-active";

            joiner.add(tumorDetails + " (" + dateAdditionDiagnosis + dateAdditionLastTreatment + active + ")");
        }

        if (record.priorSecondPrimaries().size() > 1) {
            return "Previous primary tumors: " + joiner;
        } else if (!record.priorSecondPrimaries().isEmpty()) {
            return "Previous primary tumor: " + joiner;
        } else {
            return Strings.EMPTY;
        }
    }

    @Nullable
    private static String toDateString(@Nullable Integer year, @Nullable Integer month) {
        if (year != null) {
            return month != null ? month + "/" + year : String.valueOf(year);
        } else {
            return null;
        }
    }

    @Nullable
    private static String toStopReasonString(@Nullable String stopReason) {
        return stopReason != null ? "stop reason: " + stopReason : null;
    }

    @NotNull
    private static String relevantNonOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorOtherCondition priorOtherCondition : record.priorOtherConditions()) {
            String addon = Strings.EMPTY;
            if (!priorOtherCondition.isContraindicationForTherapy()) {
                addon = " (no contraindication for therapy)";
            }
            joiner.add(priorOtherCondition.name() + addon);
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
