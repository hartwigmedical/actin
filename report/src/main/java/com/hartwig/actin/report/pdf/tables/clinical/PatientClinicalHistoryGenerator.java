package com.hartwig.actin.report.pdf.tables.clinical;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;
import com.hartwig.actin.clinical.sort.PriorSecondPrimaryDiagnosedDateComparator;
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparatorFactory;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
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
        table.addCell(Cells.createContentNoBorder(tableOrNone(relevantSystemicPreTreatmentHistoryTable(record))));

        table.addCell(Cells.createKey("Relevant other oncological history"));
        table.addCell(Cells.createContentNoBorder(tableOrNone(relevantNonSystemicPreTreatmentHistoryTable(record))));

        table.addCell(Cells.createKey("Previous primary tumor"));
        table.addCell(Cells.createContentNoBorder(tableOrNone(secondPrimaryHistoryTable(record))));

        table.addCell(Cells.createKey("Relevant non-oncological history"));
        table.addCell(Cells.createContentNoBorder(tableOrNone(relevantNonOncologicalHistoryTable(record))));

        return table;
    }

    @NotNull
    private Table tableOrNone(@NotNull Table table) {
        return table.getNumberOfRows() > 0 ? table : createNoneTable();
    }

    @NotNull
    private Table createNoneTable() {
        Table table = Tables.createSingleColWithWidth(valueWidth);
        table.addCell(Cells.createSpanningNoneEntry(table));
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

        return table;
    }

    @NotNull
    private Table secondPrimaryHistoryTable(@NotNull ClinicalRecord record) {
        Stream<PriorSecondPrimary> priorSecondPrimaryStream =
                record.priorSecondPrimaries().stream().sorted(new PriorSecondPrimaryDiagnosedDateComparator());
        Table table = Tables.createSingleColWithWidth(valueWidth);

        priorSecondPrimaryStream.forEach(secondPrimary -> {
            table.addCell(Cells.createContentNoBorder(toPriorSecondPrimaryString(secondPrimary)));
        });

        return table;
    }

    @NotNull
    private Table relevantNonOncologicalHistoryTable(@NotNull ClinicalRecord record) {
        float dateWidth = valueWidth / 4;
        float treatmentWidth = valueWidth - dateWidth;
        Table table = Tables.createFixedWidthCols(dateWidth, treatmentWidth);

        record.priorOtherConditions().forEach(priorOtherCondition -> {
            Optional<String> dateOption = toDateString(priorOtherCondition.year(), priorOtherCondition.month());
            if (dateOption.isPresent()) {
                table.addCell(Cells.createContentNoBorder(dateOption.get()));
                table.addCell(Cells.createContentNoBorder(toPriorOtherConditionString(priorOtherCondition)));
            } else {
                Cells.createSpanningEntry(toPriorOtherConditionString(priorOtherCondition), table);
            }
        });

        return table;
    }

    @NotNull
    private static String toPriorOtherConditionString(@NotNull PriorOtherCondition priorOtherCondition) {
        String addon = Strings.EMPTY;
        if (!priorOtherCondition.isContraindicationForTherapy()) {
            addon = " (no contraindication for therapy)";
        }

        return priorOtherCondition.name() + addon;
    }

    @NotNull
    private static String toPriorSecondPrimaryString(@NotNull PriorSecondPrimary priorSecondPrimary) {
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

        return tumorDetails + " (" + dateAdditionDiagnosis + dateAdditionLastTreatment + active + ")";
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
}
