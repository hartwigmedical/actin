package com.hartwig.actin.report.pdf.tables;

import java.util.StringJoiner;

import com.hartwig.actin.datamodel.molecular.MolecularRecord;
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
        return "Molecular results";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { keyWidth, valueWidth });

        table.addCell(Cells.createKey("Molecular results have reliable quality"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.hasReliableQuality())));

        table.addCell(Cells.createKey("Tumor sample has reliable and sufficient purity"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.hasReliablePurity())));

        table.addCell(Cells.createKey("Actionable molecular events"));
        table.addCell(Cells.createValue(actionableMolecularEvents(record)));

        return table;
    }

    @NotNull
    private static String actionableMolecularEvents(@NotNull MolecularRecord record) {
        StringJoiner joiner = Formats.stringJoiner();
        for (String string : record.actionableGenomicEvents()) {
            joiner.add(string);
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
