package com.hartwig.actin.report.pdf.tables.molecular;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularCharacteristicsGenerator implements TableGenerator {

    @NotNull
    private final MolecularRecord molecular;
    private final float width;

    public MolecularCharacteristicsGenerator(@NotNull final MolecularRecord molecular, final float width) {
        this.molecular = molecular;
        this.width = width;
    }

    @NotNull
    @Override
    public String title() {
        return "General";
    }

    @NotNull
    @Override
    public Table contents() {
        float colWidth = width / 7;
        Table table = Tables.createFixedWidthCols(colWidth, colWidth, colWidth, colWidth, colWidth, colWidth, colWidth);

        table.addHeaderCell(Cells.createHeader("Purity"));
        table.addHeaderCell(Cells.createHeader("Reliable Quality"));
        table.addHeaderCell(Cells.createHeader("CUPPA Cancer type prediction"));
        table.addHeaderCell(Cells.createHeader("TML Status"));
        table.addHeaderCell(Cells.createHeader("MS Stability"));
        table.addHeaderCell(Cells.createHeader("HR Status"));
        table.addHeaderCell(Cells.createHeader("DPYD"));

        table.addCell(createPurityCell(molecular.characteristics().purity()));
        table.addCell(Cells.createContent("TODO"));
        table.addCell(Cells.createContent("TODO"));
        table.addCell(Cells.createContent("TODO"));
        table.addCell(Cells.createContent("TODO"));
        table.addCell(Cells.createContent("TODO"));
        table.addCell(Cells.createContent("TODO"));

        return table;
    }

    @NotNull
    private static Cell createPurityCell(@Nullable Double purity) {
        if (purity == null){
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        } else {
            String purityString = Formats.percentage(purity);
            if (purity < 0.2) {
                return Cells.createContentWarn(purityString);
            } else {
                return Cells.createContent(purityString);
            }
        }
    }
}
