package com.hartwig.actin.report.pdf.tables;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
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

        Set<String> eventsWithActinEvidence = record.actinTrialEligibility().keySet();
        table.addCell(Cells.createKey("Events with trial eligibility in ACTIN database"));
        table.addCell(Cells.createValue(concat(eventsWithActinEvidence)));

        Set<String> eventsWithGeneralTrialEvidence = record.generalTrialEligibility().keySet();
        Set<String> additionalTrialEvents = subtract(eventsWithActinEvidence, eventsWithGeneralTrialEvidence);
        if (!additionalTrialEvents.isEmpty()) {
            table.addCell(Cells.createKey("Additional events with applicable trial evidence"));
            table.addCell(Cells.createValue(concat(additionalTrialEvents)));
        }

        table.addCell(Cells.createKey("Events with responsive evidence in CKB"));
        table.addCell(Cells.createValue(concat(record.generalResponsiveEvidence().keySet())));

        Set<String> eventsWithResistanceEvidence = record.generalResistanceEvidence().keySet();
        if (!eventsWithResistanceEvidence.isEmpty()) {
            table.addCell(Cells.createKey("Events with resistance evidence in CKB"));
            table.addCell(Cells.createValue(concat(eventsWithResistanceEvidence)));
        }

        return table;
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
