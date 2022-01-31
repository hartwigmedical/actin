package com.hartwig.actin.report.pdf.tables;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class MolecularResultsGenerator implements TableGenerator {

    @NotNull
    private final MolecularRecord record;
    private final float keyWidth;
    private final float valueWidth;

    public MolecularResultsGenerator(@NotNull final MolecularRecord record, final float keyWidth, final float valueWidth) {
        this.record = record;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Molecular results (" + record.type() + ", " + Formats.date(record.date()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { keyWidth, valueWidth });

        table.addCell(Cells.createKey("Molecular results have reliable quality"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.hasReliableQuality())));

        Set<String> eventsWithActinEvidence = extractEvents(record.actinTreatmentEvidence());
        table.addCell(Cells.createKey("Events with trial eligibility in ACTIN database"));
        table.addCell(Cells.createValue(formatEvents(eventsWithActinEvidence)));

        Set<String> eventsWithGeneralTrialEvidence = extractEvents(record.generalTrialEvidence());
        Set<String> additionalTrialEvents = subtract(eventsWithGeneralTrialEvidence, eventsWithActinEvidence);
        if (!additionalTrialEvents.isEmpty()) {
            table.addCell(Cells.createKey("Additional events with trial evidence in " + record.generalTrialSource()));
            table.addCell(Cells.createValue(formatEvents(additionalTrialEvents)));
        }

        Set<String> eventsWithGeneralResponsiveEvidence = extractEvents(record.generalResponsiveEvidence());
        table.addCell(Cells.createKey("Events with responsive evidence in " + record.generalEvidenceSource()));
        table.addCell(Cells.createValue(formatEvents(eventsWithGeneralResponsiveEvidence)));

        List<MolecularEvidence> resistanceEvidence = record.generalResistanceEvidence();
        if (!resistanceEvidence.isEmpty()) {
            table.addCell(Cells.createKey("Events with resistance evidence in " + record.generalEvidenceSource()));
            table.addCell(Cells.createValue(formatResistanceEvidence(resistanceEvidence)));
        }

        return table;
    }

    @NotNull
    private static Set<String> extractEvents(@NotNull List<MolecularEvidence> evidences) {
        Set<String> events = Sets.newHashSet();
        for (MolecularEvidence evidence : evidences) {
            events.add(evidence.event());
        }
        return events;
    }

    @NotNull
    private static String formatEvents(@NotNull Set<String> events) {
        return concat(Sets.newTreeSet(events));
    }

    @NotNull
    private static String formatResistanceEvidence(@NotNull List<MolecularEvidence> resistanceEvidences) {
        Set<String> resistanceEvidenceStrings = Sets.newHashSet();
        for (MolecularEvidence evidence : resistanceEvidences) {
            resistanceEvidenceStrings.add(evidence.event() + ": " + evidence.treatment());
        }
        return concat(resistanceEvidenceStrings);
    }

    @NotNull
    private static Set<String> subtract(@NotNull Set<String> mainSet, @NotNull Set<String> setToRemove) {
        Set<String> filtered = Sets.newHashSet();
        for (String entry : mainSet) {
            if (!setToRemove.contains(entry)) {
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
