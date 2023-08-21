package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class MolecularSummaryGenerator implements TableGenerator {

    private static final Logger LOGGER = LogManager.getLogger(MolecularSummaryGenerator.class);

    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final MolecularRecord molecular;
    @NotNull
    private final List<EvaluatedCohort> cohorts;
    private final float keyWidth;
    private final float valueWidth;

    public MolecularSummaryGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            @NotNull final List<EvaluatedCohort> cohorts, final float keyWidth, final float valueWidth) {
        this.clinical = clinical;
        this.molecular = molecular;
        this.cohorts = cohorts;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Recent molecular results";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createSingleColWithWidth(keyWidth + valueWidth);

        if (molecular.containsTumorCells()) {
            if (molecular.type() != ExperimentType.WGS) {
                LOGGER.warn("Generating WGS results for non-WGS sample");
            }

            TableGenerator wgsGenerator = new WGSSummaryGenerator(clinical, molecular, cohorts, keyWidth, valueWidth);

            table.addCell(Cells.createSubTitle(wgsGenerator.title()));
            table.addCell(Cells.create(wgsGenerator.contents()));
        } else {
            Table noRecent = Tables.createFixedWidthCols(keyWidth, valueWidth);

            noRecent.addCell(Cells.createKey(molecular.type() + " results"));
            noRecent.addCell(Cells.createValue("No successful WGS could be performed on the submitted biopsy"));

            table.addCell(Cells.create(noRecent));
        }

        PriorMolecularResultGenerator priorMolecularResultGenerator = new PriorMolecularResultGenerator(clinical, keyWidth, valueWidth);
        table.addCell(Cells.createEmpty());
        table.addCell(Cells.create(priorMolecularResultGenerator.contents()));

        return table;
    }
}
