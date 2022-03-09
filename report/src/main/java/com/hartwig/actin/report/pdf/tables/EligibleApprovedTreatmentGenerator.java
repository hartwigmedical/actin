package com.hartwig.actin.report.pdf.tables;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin;
import com.hartwig.actin.report.interpretation.CUPInterpreter;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class EligibleApprovedTreatmentGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final MolecularRecord molecular;
    private final float width;

    public EligibleApprovedTreatmentGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            final float width) {
        this.clinical = clinical;
        this.molecular = molecular;
        this.width = width;
    }

    @NotNull
    @Override
    public String title() {
        return "Approved treatments considered eligible";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createSingleColWithWidth(width);

        table.addHeaderCell(Cells.createHeader("Treatment"));

        if (CUPInterpreter.isCUP(clinical.tumor()) && hasReliableTumorPrediction(molecular.predictedTumorOrigin())) {
            table.addCell(Cells.createContent("Potential SOC for " + molecular.predictedTumorOrigin().tumorType()));
        } else {
            table.addCell(Cells.createContent("Not yet determined"));
        }

        return Tables.makeWrapping(table);
    }

    private static boolean hasReliableTumorPrediction(@NotNull PredictedTumorOrigin predictedTumorOrigin) {
        return (predictedTumorOrigin != null && predictedTumorOrigin.likelihood() > 0.8);
    }
}
