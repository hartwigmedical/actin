package com.hartwig.actin.report.pdf.tables;

import java.util.StringJoiner;

import com.hartwig.actin.datamodel.molecular.MolecularRecord;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class MolecularResultsTableGenerator implements TableGenerator {

    @NotNull
    private final MolecularRecord record;
    @NotNull
    private final float[] widths;

    public MolecularResultsTableGenerator(@NotNull final MolecularRecord record, @NotNull final float[] widths) {
        this.record = record;
        this.widths = widths;
    }

    @NotNull
    @Override
    public String title() {
        return "Molecular results";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(widths);

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
