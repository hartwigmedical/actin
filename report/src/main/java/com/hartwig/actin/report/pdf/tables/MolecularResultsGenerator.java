package com.hartwig.actin.report.pdf.tables;

import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Multimap;
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

        MolecularEvidence evidence = record.evidence();
        Set<String> eventsWithActinEvidence = evidence.actinTrialEvidence().keySet();
        table.addCell(Cells.createKey("Events with trial eligibility in ACTIN database"));
        table.addCell(Cells.createValue(formatEvents(eventsWithActinEvidence)));

        Set<String> eventsWithGeneralTrialEvidence = evidence.generalTrialEvidence().keySet();
        Set<String> additionalTrialEvents = subtract(eventsWithGeneralTrialEvidence, eventsWithActinEvidence);
        if (!additionalTrialEvents.isEmpty()) {
            table.addCell(Cells.createKey("Additional events with trial evidence in " + evidence.generalTrialSource()));
            table.addCell(Cells.createValue(formatEvents(additionalTrialEvents)));
        }

        table.addCell(Cells.createKey("Events with responsive evidence in " + evidence.generalEvidenceSource()));
        table.addCell(Cells.createValue(formatEvents(evidence.generalResponsiveEvidence().keySet())));

        Multimap<String, String> resistanceEvidence = evidence.generalResistanceEvidence();
        if (!resistanceEvidence.isEmpty()) {
            table.addCell(Cells.createKey("Events with resistance evidence in " + evidence.generalEvidenceSource()));
            table.addCell(Cells.createValue(formatResistanceEvidence(resistanceEvidence)));
        }

        return table;
    }

    @NotNull
    private static String formatEvents(@NotNull Set<String> events) {
        return concat(Sets.newTreeSet(events));
    }

    @NotNull
    private static String formatResistanceEvidence(@NotNull Multimap<String, String> resistanceEvidence) {
        Set<String> resistanceEvents = Sets.newHashSet();
        for (Map.Entry<String, String> entry : resistanceEvidence.entries()) {
            resistanceEvents.add(entry.getKey() + " - " + entry.getValue());
        }
        return concat(resistanceEvents);
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
