package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.List;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.sort.PriorMolecularTestComparator;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence;
import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
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
    private final List<EvaluatedTrial> trials;
    @NotNull
    private final AggregatedEvidence aggregatedEvidence;
    private final float keyWidth;
    private final float valueWidth;

    public MolecularSummaryGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            @NotNull final List<EvaluatedTrial> trials, @NotNull final AggregatedEvidence aggregatedEvidence, final float keyWidth,
            final float valueWidth) {
        this.clinical = clinical;
        this.molecular = molecular;
        this.trials = trials;
        this.aggregatedEvidence = aggregatedEvidence;
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
        Table table = Tables.createSingleColWithWidth(keyWidth + valueWidth);

        table.addCell(Cells.create(prior()));
        table.addCell(Cells.createEmpty());

        if (molecular.containsTumorCells()) {
            TableGenerator recentGenerator =
                    new RecentMolecularSummaryGenerator(clinical, molecular, trials, aggregatedEvidence, keyWidth, valueWidth);

            table.addCell(Cells.createTitle(recentGenerator.title()).setFontSize(7));
            table.addCell(Cells.create(recentGenerator.contents()));
        } else {
            Table noRecent = Tables.createFixedWidthCols(keyWidth, valueWidth);

            noRecent.addCell(Cells.createKey(molecular.type() + " molecular results"));
            noRecent.addCell(Cells.createValue("No successful WGS could be performed on the submitted biopsy"));

            table.addCell(Cells.create(noRecent));
        }

        return table;
    }

    @NotNull
    private Table prior() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Previous relevant molecular results"));
        table.addCell(Cells.createValue(concat(clinical.priorMolecularTests())));

        return table;
    }

    @NotNull
    private static String concat(@NotNull List<PriorMolecularTest> priorMolecularTests) {
        List<PriorMolecularTest> sorted = Lists.newArrayList();
        sorted.addAll(priorMolecularTests);
        sorted.sort(new PriorMolecularTestComparator());

        StringJoiner joiner = Formats.commaJoiner();
        for (PriorMolecularTest priorMolecularTest : sorted) {
            String entry = priorMolecularTest.item();
            if (priorMolecularTest.measure() != null) {
                entry += (" " + priorMolecularTest.measure());
            }
            if (priorMolecularTest.scoreText() != null) {
                entry += (" " + priorMolecularTest.scoreText());
            } else if (priorMolecularTest.scoreValue() != null) {
                if (priorMolecularTest.scoreValuePrefix() != null) {
                    entry += (" " + priorMolecularTest.scoreValuePrefix());
                }
                entry += (" " + Formats.twoDigitNumber(priorMolecularTest.scoreValue()));
                if (priorMolecularTest.scoreValueUnit() != null) {
                    entry += (" " + priorMolecularTest.scoreValueUnit());
                }
            } else {
                LOGGER.warn("Neither a score value nor a score text is available for: " + priorMolecularTest);
            }

            joiner.add(entry + " (" + priorMolecularTest.test() + ")");
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
