package com.hartwig.actin.report.pdf.tables.clinical;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparatorFactory;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatientClinicalHistoryGenerator implements TableGenerator {

    private static final String STOP_REASON_PROGRESSIVE_DISEASE = "PD";

    @NotNull
    private final ClinicalRecord record;
    private final float keyWidth;
    private final float valueWidth;

    public PatientClinicalHistoryGenerator(@NotNull ClinicalRecord record, float keyWidth, float valueWidth) {
        this.record = record;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Clinical summary";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Relevant systemic treatment history"));
        table.addCell(Cells.createContentNoBorder(relevantSystemicPreTreatmentHistoryTable(record)));

        table.addCell(Cells.createKey("Relevant other oncological history"));
        table.addCell(Cells.createContentNoBorder(relevantNonSystemicPreTreatmentHistoryTable(record)));

        table.addCell(Cells.createKey("Previous primary tumor"));
        String secondPrimaryHistory = secondPrimaryHistoryTable(record);
        if (!secondPrimaryHistory.isEmpty()) {
            table.addCell(Cells.createValue(secondPrimaryHistory));
        } else {
            table.addCell(Cells.createValue("None"));
        }

        table.addCell(Cells.createKey("Relevant non-oncological history"));
        table.addCell(Cells.createContent(relevantNonOncologicalHistoryTable(record)));

        return table;
    }

    @NotNull
    private Table relevantSystemicPreTreatmentHistoryTable(@NotNull ClinicalRecord record) {
        return treatmentHistoryTable(record.treatmentHistory(), true);
    }

    @NotNull
    private Table relevantNonSystemicPreTreatmentHistoryTable(@NotNull ClinicalRecord record) {
        return treatmentHistoryTable(record.treatmentHistory(), false);
    }

    @NotNull
    private Table treatmentHistoryTable(@NotNull List<TreatmentHistoryEntry> treatmentHistory, boolean requireSystemic) {
        Stream<TreatmentHistoryEntry> sortedFilteredTreatments = treatmentHistory.stream()
                .filter(entry -> entry.treatments().stream().anyMatch(treatment -> treatment.isSystemic() == requireSystemic))
                .sorted(TreatmentHistoryAscendingDateComparatorFactory.treatmentHistoryEntryComparator());

        float dateWidth = valueWidth / 4;
        float treatmentWidth = valueWidth - dateWidth;
        Table table = Tables.createFixedWidthCols(dateWidth, treatmentWidth);

        sortedFilteredTreatments.forEach(entry -> {
            table.addCell(Cells.createContentNoBorder(extractDateRangeString(entry)));
            table.addCell(Cells.createContentNoBorder(extractTreatmentString(entry)));
        });

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static String secondPrimaryHistoryTable(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.commaJoiner();
        for (PriorSecondPrimary priorSecondPrimary : record.priorSecondPrimaries()) {
            String tumorDetails = priorSecondPrimary.tumorLocation();
            if (!priorSecondPrimary.tumorSubType().isEmpty()) {
                tumorDetails = tumorDetails + " " + priorSecondPrimary.tumorSubType();
            } else if (priorSecondPrimary.tumorSubType().isEmpty() && !priorSecondPrimary.tumorType().isEmpty()) {
                tumorDetails = tumorDetails + " " + priorSecondPrimary.tumorType();
            }

            String dateAdditionDiagnosis =
                    toDateString(priorSecondPrimary.diagnosedYear(), priorSecondPrimary.diagnosedMonth()).map(dateDiagnosis -> "diagnosed "
                            + dateDiagnosis + ", ").orElse(Strings.EMPTY);

            String dateAdditionLastTreatment =
                    toDateString(priorSecondPrimary.lastTreatmentYear(), priorSecondPrimary.lastTreatmentMonth()).map(dateLastTreatment ->
                            "last treatment " + dateLastTreatment + ", ").orElse(Strings.EMPTY);

            String active = priorSecondPrimary.isActive() ? "considered active" : "considered non-active";

            joiner.add(tumorDetails + " (" + dateAdditionDiagnosis + dateAdditionLastTreatment + active + ")");
        }

        return joiner.toString();
    }

    @NotNull
    private static String extractDateRangeString(@NotNull TreatmentHistoryEntry treatmentHistoryEntry) {
        Optional<String> startOption = toDateString(treatmentHistoryEntry.startYear(), treatmentHistoryEntry.startMonth());
        Optional<String> stopOption = Optional.ofNullable(treatmentHistoryEntry.therapyHistoryDetails())
                .flatMap(details -> toDateString(details.stopYear(), details.stopMonth()));

        return startOption.orElse("?") + stopOption.map(stopString -> "-" + stopString).orElse("");
    }

    @NotNull
    private static Optional<String> toDateString(@Nullable Integer maybeYear, @Nullable Integer maybeMonth) {
        return Optional.ofNullable(maybeYear)
                .map(year -> Optional.ofNullable(maybeMonth).map(month -> month + "/" + year).orElse(String.valueOf(year)));
    }

    @NotNull
    private static String extractTreatmentString(@NotNull TreatmentHistoryEntry treatmentHistoryEntry) {
        Optional<String> stopReasonOption = Optional.ofNullable(treatmentHistoryEntry.therapyHistoryDetails())
                .flatMap(details -> Optional.ofNullable(details.stopReasonDetail()))
                .map(reason -> !reason.equalsIgnoreCase(STOP_REASON_PROGRESSIVE_DISEASE) ? "stop reason: " + reason : null);

        Optional<String> cyclesOption = Optional.ofNullable(treatmentHistoryEntry.therapyHistoryDetails())
                .flatMap(details -> Optional.ofNullable(details.cycles()))
                .map(num -> num + " cycles");

        Optional<String> combinedAnnotation = cyclesOption.isPresent()
                ? stopReasonOption.map(stopReason -> cyclesOption.get() + ", " + stopReason).or(() -> cyclesOption)
                : stopReasonOption;

        return treatmentHistoryEntry.treatmentDisplay() + combinedAnnotation.map(annotation -> " (" + annotation + ")")
                .orElse(Strings.EMPTY);
    }

    @NotNull
    private static String relevantNonOncologicalHistoryTable(@NotNull ClinicalRecord record) {
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
