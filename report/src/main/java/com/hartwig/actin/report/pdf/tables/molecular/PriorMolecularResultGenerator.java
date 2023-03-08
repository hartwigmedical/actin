package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.sort.PriorMolecularTestComparator;
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpretation;
import com.hartwig.actin.report.interpretation.PriorMolecularTestInterpreter;
import com.hartwig.actin.report.interpretation.PriorMolecularTestKey;
import com.hartwig.actin.report.interpretation.PriorMolecularTestKeyComparator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.util.ApplicationConfig;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class PriorMolecularResultGenerator {

    @NotNull
    private final ClinicalRecord clinical;
    private final float keyWidth;
    private final float valueWidth;

    public PriorMolecularResultGenerator(@NotNull final ClinicalRecord clinical, final float keyWidth, final float valueWidth) {
        this.clinical = clinical;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createSubTitle(String.format(ApplicationConfig.LOCALE,
                "IHC results (%s)",
                Formats.date(clinical.patient().questionnaireDate()))));
        if (clinical.priorMolecularTests().isEmpty()) {
            table.addCell(Cells.createValue("None"));
        } else {
            PriorMolecularTestInterpretation interpretation = PriorMolecularTestInterpreter.interpret(clinical.priorMolecularTests());
            List<Paragraph> paragraphs = generatePriorTestParagraphs(interpretation);
            table.addCell(Cells.createValue(paragraphs));
        }

        return table;
    }

    @NotNull
    private static List<Paragraph> generatePriorTestParagraphs(@NotNull PriorMolecularTestInterpretation interpretation) {
        List<Paragraph> paragraphs = Lists.newArrayList();

        Set<PriorMolecularTestKey> sortedKeys = Sets.newTreeSet(new PriorMolecularTestKeyComparator());
        sortedKeys.addAll(interpretation.textBasedPriorTests().keySet());

        for (PriorMolecularTestKey key : sortedKeys) {
            paragraphs.add(new Paragraph(formatTextBasedPriorTests(key, interpretation.textBasedPriorTests().get(key))));
        }

        List<PriorMolecularTest> sortedValueTests = Lists.newArrayList(interpretation.valueBasedPriorTests());
        sortedValueTests.sort(new PriorMolecularTestComparator());
        for (PriorMolecularTest valueTest : sortedValueTests) {
            paragraphs.add(new Paragraph(formatValueBasedPriorTest(valueTest)));
        }
        return paragraphs;
    }

    @NotNull
    private static String formatTextBasedPriorTests(@NotNull PriorMolecularTestKey key, @NotNull Collection<PriorMolecularTest> values) {
        List<PriorMolecularTest> sorted = Lists.newArrayList(values);
        sorted.sort(new PriorMolecularTestComparator());

        StringBuilder builder = new StringBuilder();
        String scoreText = key.scoreText();
        builder.append(scoreText.substring(0, 1).toUpperCase());
        if (scoreText.length() > 1) {
            builder.append(scoreText.substring(1).toLowerCase());
        }
        builder.append(" (");
        builder.append(key.test());
        builder.append("): ");
        builder.append(sorted.get(0).item());

        for (int i = 1; i < sorted.size(); i++) {
            if (i < sorted.size() - 1) {
                builder.append(", ");
            } else {
                builder.append(" and ");
            }
            builder.append(sorted.get(i).item());
        }

        return builder.toString();
    }

    @NotNull
    private static String formatValueBasedPriorTest(@NotNull PriorMolecularTest valueTest) {
        StringBuilder builder = new StringBuilder();
        builder.append("Score ");
        builder.append(valueTest.item());
        builder.append(" ");
        String measure = valueTest.measure();
        if (measure != null) {
            builder.append(measure);
            builder.append(" ");
        }
        String scoreValuePrefix = valueTest.scoreValuePrefix();
        if (scoreValuePrefix != null) {
            builder.append(scoreValuePrefix);
            builder.append(" ");
        }
        builder.append(Formats.twoDigitNumber(valueTest.scoreValue()));
        String scoreValueUnit = valueTest.scoreValueUnit();
        if (scoreValueUnit != null) {
            builder.append(" ");
            builder.append(scoreValueUnit);
        }
        return builder.toString();
    }
}
