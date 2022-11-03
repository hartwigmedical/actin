package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;
import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.interpretation.EvaluatedTrialFactory;
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter;
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
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
    private final TreatmentMatch treatmentMatch;
    private final float keyWidth;
    private final float valueWidth;

    public RecentMolecularSummaryGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            @NotNull final TreatmentMatch treatmentMatch, final float keyWidth, final float valueWidth) {
        this.clinical = clinical;
        this.molecular = molecular;
        this.treatmentMatch = treatmentMatch;
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

        // TODO Implement
//        table.addCell(Cells.createKey("Events with approved treatment evidence in " + evidence.evidenceSource()));
//        table.addCell(Cells.createValue(concat(EvidenceInterpreter.eventsWithApprovedEvidence(evidence))));
//
//        table.addCell(Cells.createKey("Events with trial eligibility in " + evidence.actinSource() + " database"));
//        table.addCell(Cells.createValue(concat(eventsForEligibleTrials(treatmentMatch, evidence.actinTrials()))));
//
//        table.addCell(addIndent(Cells.createKey(
//                "Additional events with trial eligibility in NL (" + evidence.externalTrialSource() + ")")));
//        table.addCell(Cells.createValue(concat(EvidenceInterpreter.additionalEventsWithExternalTrialEvidence(evidence))));
//
//        table.addCell(addIndent(Cells.createKey("Additional events with experimental evidence (" + evidence.evidenceSource() + ")")));
//        table.addCell(Cells.createValue(concat(EvidenceInterpreter.additionalEventsWithOnLabelExperimentalEvidence(evidence))));
//
//        table.addCell(Cells.createKey("Additional events with off-label experimental evidence in " + evidence.evidenceSource()));
//        table.addCell(Cells.createValue(concat(EvidenceInterpreter.additionalEventsWithOffLabelExperimentalEvidence(evidence))));
//
//        Set<TreatmentEvidence> knownResistanceEvidence = evidence.knownResistanceEvidence();
//        if (!knownResistanceEvidence.isEmpty()) {
//            table.addCell(Cells.createKey("Events with resistance evidence in " + evidence.evidenceSource()));
//            table.addCell(Cells.createValue(formatResistanceEvidence(knownResistanceEvidence)));
//        }

        return table;
    }

    @NotNull
    private static Set<String> eventsForEligibleTrials(@NotNull TreatmentMatch treatmentMatch,
            @NotNull Set<ActinTrialEvidence> actinTrialEvidences) {
        Set<String> molecularEvents = Sets.newTreeSet(Ordering.natural());
        for (EvaluatedTrial trial : EvaluatedTrialFactory.create(treatmentMatch, actinTrialEvidences)) {
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
    private static String formatResistanceEvidence(@NotNull Iterable<TreatmentEvidence> resistanceEvidences) {
        Set<String> resistanceEvidenceStrings = Sets.newTreeSet();
        for (TreatmentEvidence evidence : resistanceEvidences) {
            resistanceEvidenceStrings.add(evidence.event() + ": " + evidence.treatment());
        }
        return concat(resistanceEvidenceStrings);
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
