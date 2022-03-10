package com.hartwig.actin.report.pdf.tables;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin;
import com.hartwig.actin.report.interpretation.CUPInterpreter;
import com.hartwig.actin.report.interpretation.EvidenceInterpreter;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        table.addCell(Cells.createKey("Events with approved treatment evidence in " + molecular.evidenceSource()));
        table.addCell(Cells.createValue(concat(EvidenceInterpreter.eventsWithApprovedEvidence(molecular))));

        table.addCell(Cells.createKey("Events with trial eligibility in " + molecular.actinSource() + " database"));
        table.addCell(Cells.createValue(concat(EvidenceInterpreter.eventsWithActinEvidence(molecular))));

        Set<String> additionalTrialEvents = EvidenceInterpreter.additionalEventsWithExternalTrialEvidence(molecular);
        if (!additionalTrialEvents.isEmpty()) {
            table.addCell(addIndent(Cells.createKey(
                    "Additional events with trial eligibility in NL (" + molecular.externalTrialSource() + ")")));
            table.addCell(Cells.createValue(concat(additionalTrialEvents)));
        }

        table.addCell(addIndent(Cells.createKey("Additional events with experimental evidence (" + molecular.evidenceSource() + ")")));
        table.addCell(Cells.createValue(concat(EvidenceInterpreter.additionalEventsWithExperimentalEvidence(molecular))));

        table.addCell(Cells.createKey("Additional events with other responsive evidence in " + molecular.evidenceSource()));
        table.addCell(Cells.createValue(concat(EvidenceInterpreter.additionalEventsWithOtherEvidence(molecular))));

        Set<MolecularEvidence> resistanceEvidence = molecular.resistanceEvidence();
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
    private static String formatResistanceEvidence(@NotNull Iterable<MolecularEvidence> resistanceEvidences) {
        Set<String> resistanceEvidenceStrings = Sets.newTreeSet();
        for (MolecularEvidence evidence : resistanceEvidences) {
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
