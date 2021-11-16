package com.hartwig.actin.report.pdf.tables;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.interpretation.MolecularInterpretation;
import com.hartwig.actin.molecular.interpretation.MolecularInterpreter;
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
        return "Molecular results (" + Formats.date(record.date()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { keyWidth, valueWidth });

        table.addCell(Cells.createKey("Molecular results have reliable quality"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(record.hasReliableQuality())));

        table.addCell(Cells.createKey("Molecular experiment type"));
        table.addCell(Cells.createValue(record.type().toString()));

        MolecularInterpretation interpretation = MolecularInterpreter.interpret(record);
        table.addCell(Cells.createKey("Actionable mutations"));
        table.addCell(Cells.createValue(concat(interpretation.applicableResponsiveEvents())));

        if (!interpretation.applicableResistanceEvents().isEmpty()) {
            table.addCell(Cells.createKey("Resistance mutations"));
            table.addCell(Cells.createValue(concat(interpretation.applicableResistanceEvents())));
        }

        return table;
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
