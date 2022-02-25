package com.hartwig.actin.report.pdf.tables;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

import org.jetbrains.annotations.NotNull;

public class RecentMolecularResultsGenerator implements TableGenerator {

    @NotNull
    private final MolecularRecord record;
    private final float keyWidth;
    private final float valueWidth;

    public RecentMolecularResultsGenerator(@NotNull final MolecularRecord record, final float keyWidth, final float valueWidth) {
        this.record = record;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return record.type() + " molecular results (" + Formats.date(record.date()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Biopsy location"));
        table.addCell(Cells.createKey("location"));
        // table.addCell(Cells.createValue(biopsyLocation)); //TODO

        table.addCell(Cells.createKey("Results have reliable quality"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.hasReliableQuality())));

        Set<String> eventsWithApprovedEvidence = extractEvents(record.approvedResponsiveEvidence());
        table.addCell(Cells.createKey("Events with approved treatment evidence in " + record.evidenceSource()));
        table.addCell(Cells.createValue(concat(eventsWithApprovedEvidence)));

        Set<String> eventsWithActinEvidence = extractEvents(record.actinTrials());
        table.addCell(Cells.createKey("Events with trial eligibility in " + record.actinSource() + " database"));
        table.addCell(Cells.createValue(concat(eventsWithActinEvidence)));

        Set<String> eventsWithExternalTrialEvidence = extractEvents(record.externalTrials());
        Set<String> additionalTrialEvents =
                subtract(eventsWithExternalTrialEvidence, Lists.newArrayList(eventsWithApprovedEvidence, eventsWithActinEvidence));
        if (!additionalTrialEvents.isEmpty()) {
            table.addCell(addIndent(Cells.createKey("Additional events with trial eligibility in NL (" + record.externalTrialSource() + ")")));
            table.addCell(Cells.createValue(concat(additionalTrialEvents)));
        }

        Set<String> eventsWithExperimentalEvidence = extractEvents(record.experimentalResponsiveEvidence());
        Set<String> additionalExperimentalEvents =
                subtract(eventsWithExperimentalEvidence, Lists.newArrayList(eventsWithApprovedEvidence, eventsWithActinEvidence));
        table.addCell(addIndent(Cells.createKey("Additional events with experimental evidence (" + record.evidenceSource() +")")));
        table.addCell(Cells.createValue(concat(additionalExperimentalEvents)));

        Set<String> eventsWithOtherEvidence = extractEvents(record.otherResponsiveEvidence());
        Set<String> additionalOtherEvents = subtract(eventsWithOtherEvidence,
                Lists.newArrayList(eventsWithApprovedEvidence, eventsWithActinEvidence, eventsWithExperimentalEvidence));
        table.addCell(Cells.createKey("Additional events with other responsive evidence in " + record.evidenceSource()));
        table.addCell(Cells.createValue(concat(additionalOtherEvents)));

        List<MolecularEvidence> resistanceEvidence = record.resistanceEvidence();
        if (!resistanceEvidence.isEmpty()) {
            table.addCell(Cells.createKey("Events with resistance evidence in " + record.evidenceSource()));
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
