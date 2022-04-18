package com.hartwig.actin.report.pdf.tables.molecular;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

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
        Table table = Tables.createFixedWidthCols(width);

        return Tables.makeWrapping(table);
    }
}
