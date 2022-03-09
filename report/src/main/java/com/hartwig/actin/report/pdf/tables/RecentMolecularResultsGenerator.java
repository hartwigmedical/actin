package com.hartwig.actin.report.pdf.tables;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin;
import com.hartwig.actin.report.interpretation.CUPInterpreter;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO Remove duplication with EligibleExternalTrials
public class RecentMolecularResultsGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final MolecularRecord molecular;
    private final float keyWidth;
    private final float valueWidth;

    public RecentMolecularResultsGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            final float keyWidth, final float valueWidth) {
        this.clinical = clinical;
        this.molecular = molecular;
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

        if (!molecular.hasReliableQuality()) {
            table.addCell(Cells.createKey("Results have reliable quality"));
            table.addCell(Cells.createValue(Formats.yesNoUnknown(molecular.hasReliableQuality())));
        }

        if (CUPInterpreter.isCUP(clinical.tumor())) {
            table.addCell(Cells.createKey("Predicted tumor origin"));
            table.addCell(Cells.createValue(predictedTumorOrigin((molecular.predictedTumorOrigin()))));
        }

        Set<String> eventsWithApprovedEvidence = extractEvents(molecular.approvedResponsiveEvidence());
        table.addCell(Cells.createKey("Events with approved treatment evidence in " + molecular.evidenceSource()));
        table.addCell(Cells.createValue(concat(eventsWithApprovedEvidence)));

        Set<String> eventsWithActinEvidence = extractEvents(molecular.actinTrials());
        table.addCell(Cells.createKey("Events with trial eligibility in " + molecular.actinSource() + " database"));
        table.addCell(Cells.createValue(concat(eventsWithActinEvidence)));

        Set<String> eventsWithExternalTrialEvidence = extractEvents(molecular.externalTrials());
        Set<String> additionalTrialEvents =
                subtract(eventsWithExternalTrialEvidence, Lists.newArrayList(eventsWithApprovedEvidence, eventsWithActinEvidence));
        if (!additionalTrialEvents.isEmpty()) {
            table.addCell(addIndent(Cells.createKey(
                    "Additional events with trial eligibility in NL (" + molecular.externalTrialSource() + ")")));
            table.addCell(Cells.createValue(concat(additionalTrialEvents)));
        }

        Set<String> eventsWithExperimentalEvidence = extractEvents(molecular.experimentalResponsiveEvidence());
        Set<String> additionalExperimentalEvents =
                subtract(eventsWithExperimentalEvidence, Lists.newArrayList(eventsWithApprovedEvidence, eventsWithActinEvidence));
        table.addCell(addIndent(Cells.createKey("Additional events with experimental evidence (" + molecular.evidenceSource() + ")")));
        table.addCell(Cells.createValue(concat(additionalExperimentalEvents)));

        Set<String> eventsWithOtherEvidence = extractEvents(molecular.otherResponsiveEvidence());
        Set<String> additionalOtherEvents = subtract(eventsWithOtherEvidence,
                Lists.newArrayList(eventsWithApprovedEvidence, eventsWithActinEvidence, eventsWithExperimentalEvidence));
        table.addCell(Cells.createKey("Additional events with other responsive evidence in " + molecular.evidenceSource()));
        table.addCell(Cells.createValue(concat(additionalOtherEvents)));

        List<MolecularEvidence> resistanceEvidence = molecular.resistanceEvidence();
        if (!resistanceEvidence.isEmpty()) {
            table.addCell(Cells.createKey("Events with resistance evidence in " + molecular.evidenceSource()));
            table.addCell(Cells.createValue(formatResistanceEvidence(resistanceEvidence)));
        }

        return table;
    }

    @NotNull
    private static String biopsyLocation(@NotNull TumorDetails tumor) {
        String biopsyLocation = tumor.biopsyLocation();
        return biopsyLocation != null ? biopsyLocation : Formats.VALUE_UNKNOWN;
    }

    @NotNull
    private static String predictedTumorOrigin(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        if (predictedTumorOrigin == null) {
            return Formats.VALUE_UNKNOWN;
        }

        double likelihoodValue = predictedTumorOrigin.likelihood();
        return likelihoodValue >= 0.8
                ? predictedTumorOrigin.tumorType() + " (" + Formats.percentage(likelihoodValue) + ")"
                : "Inconclusive";
    }

    @NotNull
    private static Cell addIndent(@NotNull Cell cell) {
        return cell.setPaddingLeft(10);
    }

    @NotNull
    private static Set<String> extractEvents(@NotNull List<MolecularEvidence> evidences) {
        Set<String> events = Sets.newTreeSet();
        for (MolecularEvidence evidence : evidences) {
            events.add(evidence.event());
        }
        return events;
    }

    @NotNull
    private static String formatResistanceEvidence(@NotNull List<MolecularEvidence> resistanceEvidences) {
        Set<String> resistanceEvidenceStrings = Sets.newTreeSet();
        for (MolecularEvidence evidence : resistanceEvidences) {
            resistanceEvidenceStrings.add(evidence.event() + ": " + evidence.treatment());
        }
        return concat(resistanceEvidenceStrings);
    }

    @NotNull
    private static Set<String> subtract(@NotNull Set<String> mainSet, @NotNull List<Set<String>> setsToRemove) {
        Set<String> filtered = Sets.newTreeSet();
        for (String entry : mainSet) {
            boolean retain = true;
            for (Set<String> set : setsToRemove) {
                if (set.contains(entry)) {
                    retain = false;
                    break;
                }
            }

            if (retain) {
                filtered.add(entry);
            }
        }
        return filtered;
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
