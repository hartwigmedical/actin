package com.hartwig.actin.report.pdf.tables;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class EligibleExternalTrialsGenerator implements TableGenerator {

    @NotNull
    private final String source;
    @NotNull
    private final Set<MolecularEvidence> additionalEvidenceForExternalTrials;
    private final float keyWidth;
    private final float valueWidth;

    public EligibleExternalTrialsGenerator(@NotNull final String source,
            @NotNull final Set<MolecularEvidence> additionalEvidenceForExternalTrials, final float keyWidth, final float valueWidth) {
        this.source = source;
        this.additionalEvidenceForExternalTrials = additionalEvidenceForExternalTrials;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Additional " + source + " trials eligible based on molecular results";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addHeaderCell(Cells.createHeader("Event"));
        table.addHeaderCell(Cells.createHeader("Trial"));

        for (MolecularEvidence evidence : additionalEvidenceForExternalTrials) {
            table.addCell(Cells.createContent(evidence.event()));
            table.addCell(Cells.createContent(evidence.treatment()));
        }

        return Tables.makeWrapping(table);
    }
}
