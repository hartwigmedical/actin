package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.report.interpretation.AggregatedEvidence;
import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.interpretation.EvidenceInterpreter;
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter;
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.TreatmentConstants;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecentMolecularSummaryGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final MolecularRecord molecular;
    @NotNull
    private final List<EvaluatedTrial> trials;
    @NotNull
    private final AggregatedEvidence aggregatedEvidence;
    @NotNull
    private final EvidenceInterpreter interpreter;
    private final float keyWidth;
    private final float valueWidth;

    public RecentMolecularSummaryGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            @NotNull final List<EvaluatedTrial> trials, @NotNull final AggregatedEvidence aggregatedEvidence,
            @NotNull final EvidenceInterpreter interpreter, final float keyWidth, final float valueWidth) {
        this.clinical = clinical;
        this.molecular = molecular;
        this.trials = trials;
        this.aggregatedEvidence = aggregatedEvidence;
        this.interpreter = interpreter;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return molecular.type() + " molecular results (" + Formats.date(molecular.date()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Biopsy location"));
        table.addCell(Cells.createValue(biopsyLocation(clinical.tumor())));

        if (TumorDetailsInterpreter.isCUP(clinical.tumor())) {
            table.addCell(Cells.createKey("Predicted tumor origin"));
            table.addCell(createPredictedTumorOriginValue(molecular.characteristics().predictedTumorOrigin()));
        }

        table.addCell(Cells.createKey("Events with approved treatment evidence in " + molecular.evidenceSource()));
        table.addCell(Cells.createValue(concat(interpreter.eventsWithApprovedEvidence(aggregatedEvidence))));

        table.addCell(Cells.createKey("Events with trial eligibility in " + TreatmentConstants.ACTIN_SOURCE + " database"));
        table.addCell(Cells.createValue(concat(eventsForEligibleTrials())));

        table.addCell(addIndent(Cells.createKey(
                "Additional events with trial eligibility in NL (" + molecular.externalTrialSource() + ")")));
        table.addCell(Cells.createValue(concat(interpreter.additionalEventsWithExternalTrialEvidence(aggregatedEvidence))));

        table.addCell(addIndent(Cells.createKey("Additional events with experimental evidence (" + molecular.evidenceSource() + ")")));
        table.addCell(Cells.createValue(concat(interpreter.additionalEventsWithOnLabelExperimentalEvidence(aggregatedEvidence))));

        table.addCell(Cells.createKey("Additional events with off-label experimental evidence in " + molecular.evidenceSource()));
        table.addCell(Cells.createValue(concat(interpreter.additionalEventsWithOffLabelExperimentalEvidence(aggregatedEvidence))));

        if (!aggregatedEvidence.knownResistantTreatmentsPerEvent().isEmpty()) {
            table.addCell(Cells.createKey("Events with resistance evidence in " + molecular.evidenceSource()));
            table.addCell(Cells.createValue(formatTreatmentsPerEvent(aggregatedEvidence.knownResistantTreatmentsPerEvent())));
        }

        return table;
    }

    @NotNull
    private  Set<String> eventsForEligibleTrials() {
        Set<String> molecularEvents = Sets.newTreeSet(Ordering.natural());
        for (EvaluatedTrial trial : trials) {
            if (trial.isPotentiallyEligible() && trial.isOpen()) {
                molecularEvents.addAll(trial.molecularEvents());
            }
        }
        return molecularEvents;
    }

    @NotNull
    private static String biopsyLocation(@NotNull TumorDetails tumor) {
        String biopsyLocation = tumor.biopsyLocation();
        return biopsyLocation != null ? biopsyLocation : Formats.VALUE_UNKNOWN;
    }

    @NotNull
    private static Cell createPredictedTumorOriginValue(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        String interpretation = TumorOriginInterpreter.interpret(predictedTumorOrigin);
        if (TumorOriginInterpreter.hasConfidentPrediction(predictedTumorOrigin)) {
            return Cells.createValue(interpretation);
        } else {
            return Cells.createValueWarn("Inconclusive, highest prediction: " + interpretation);
        }
    }

    @NotNull
    private static Cell addIndent(@NotNull Cell cell) {
        return cell.setPaddingLeft(10);
    }

    @NotNull
    private static String formatTreatmentsPerEvent(@NotNull Multimap<String, String> treatmentsPerEvent) {
        Set<String> evidenceStrings = Sets.newTreeSet();
        for (Map.Entry<String, String> entry : treatmentsPerEvent.entries()) {
            evidenceStrings.add(entry.getKey() + ": " + entry.getValue());
        }
        return concat(evidenceStrings);
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = Formats.commaJoiner();
        for (String string : strings) {
            joiner.add(string);
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
