package com.hartwig.actin.report.pdf.tables.clinical;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return "Clinical Summary";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Relevant systemic treatment history"));
        table.addCell(Cells.createValue(relevantSystemicPreTreatmentHistory(record)));

        table.addCell(Cells.createKey("Relevant other oncological history"));
        String nonSystemicHistory = relevantNonSystemicPreTreatmentHistory(record);
        if (!nonSystemicHistory.isEmpty()) {
            table.addCell(Cells.createValue(nonSystemicHistory));
        } else {
            table.addCell(Cells.createValue("None"));
        }

        table.addCell(Cells.createKey("Previous primary tumor"));
        String secondPrimaryHistory = secondPrimaryHistory(record);
        if (!secondPrimaryHistory.isEmpty()) {
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
        List<PriorTumorTreatment> sortedFilteredTreatments = priorTumorTreatments.stream()
                .filter(treatment -> treatment.isSystemic() == requireSystemic)
                .sorted(new PriorTumorTreatmentDescendingDateComparator())
                .collect(Collectors.toList());

        Map<String, List<PriorTumorTreatment>> treatmentsByName =
                sortedFilteredTreatments.stream().collect(groupingBy(PatientClinicalHistoryGenerator::treatmentName));

        Set<String> evaluatedNames = Sets.newHashSet();
        Stream<Optional<String>> annotationStream = sortedFilteredTreatments.stream().map(treatment -> {
            String treatmentName = treatmentName(treatment);
            if (!evaluatedNames.contains(treatmentName)) {
                evaluatedNames.add(treatmentName);
                Optional<String> annotationOption = treatmentsByName.get(treatmentName).stream()
                        .map(PatientClinicalHistoryGenerator::extractAnnotationForTreatment)
                        .flatMap(Optional::stream)
                        .reduce((x, y) -> x + "; " + y);
                return Optional.of(treatmentName
                        + annotationOption.map(annotation -> " (" + annotation + ")").orElse(""));

            } else {
                return Optional.empty();
            }

        });
        String annotationString = annotationStream.flatMap(Optional::stream)
                .collect(joining(", "));

        return Formats.valueOrDefault(annotationString, "None");
    }

    @NotNull
    private static Optional<String> extractAnnotationForTreatment(@NotNull PriorTumorTreatment priorTumorTreatment) {
        return Stream.of(toDateRangeString(priorTumorTreatment),
                        toStopReasonString(priorTumorTreatment.stopReason()),
                        toNumberOfCyclesString(priorTumorTreatment.cycles())
                ).flatMap(Optional::stream)
                .reduce((x, y) -> x + ", " + y);
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

            String dateAdditionDiagnosis = toDateString(priorSecondPrimary.diagnosedYear(),
                    priorSecondPrimary.diagnosedMonth())
                .map(dateDiagnosis -> "diagnosed " + dateDiagnosis + ", ").orElse(Strings.EMPTY);

            String dateAdditionLastTreatment = toDateString(priorSecondPrimary.lastTreatmentYear(),
                    priorSecondPrimary.lastTreatmentMonth())
                .map(dateLastTreatment -> "last treatment " + dateLastTreatment + ", ").orElse(Strings.EMPTY);

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

    @NotNull
    private static Optional<String> toDateRangeString(@NotNull PriorTumorTreatment priorTumorTreatment) {
        Optional<String> startOption = toDateString(priorTumorTreatment.startYear(), priorTumorTreatment.startMonth());
        Optional<String> stopOption = toDateString(priorTumorTreatment.stopYear(), priorTumorTreatment.stopMonth());
        return startOption.map(startString -> startString + stopOption.map(stopString -> "-" + stopString).orElse(""))
                .or(() -> stopOption.map(stopString -> "end: " + stopString));
    }

    @NotNull
    private static Optional<String> toDateString(@Nullable Integer maybeYear, @Nullable Integer maybeMonth) {
        return Optional.ofNullable(maybeYear).map(year ->
                Optional.ofNullable(maybeMonth).map(month -> month + "/" + year).orElse(String.valueOf(year)));
    }

    @NotNull
    private static Optional<String> toNumberOfCyclesString(@Nullable Integer cycles) {
        return Optional.ofNullable(cycles).map(num -> num + " cycles");
    }

    @NotNull
    private static Optional<String> toStopReasonString(@Nullable String stopReason) {
        return Optional.ofNullable(stopReason).map(reason -> "stop reason: " + reason);
    }

    @NotNull
    private static String relevantNonOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorOtherCondition priorOtherCondition : record.priorOtherConditions()) {
            String addon = Strings.EMPTY;
            if (!priorOtherCondition.isContraindicationForTherapy()) {
                addon = " (no contraindication for therapy)";
            }

            Optional<String> dateOption = toDateString(priorOtherCondition.year(), priorOtherCondition.month());
            String dateAddition = dateOption.map(date -> " (" + date + ")").orElse(Strings.EMPTY);

            joiner.add(priorOtherCondition.name() + dateAddition + addon);
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
