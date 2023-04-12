package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer;
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.util.ApplicationConfig;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

import org.jetbrains.annotations.NotNull;

public class WGSSummaryGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final MolecularRecord molecular;
    @NotNull
    private final MolecularDriversSummarizer summarizer;
    private final float keyWidth;
    private final float valueWidth;

    public WGSSummaryGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            @NotNull final List<EvaluatedCohort> cohorts, final float keyWidth, final float valueWidth) {
        this.clinical = clinical;
        this.molecular = molecular;
        this.summarizer = MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecular.drivers(), cohorts);
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return String.format(ApplicationConfig.LOCALE,
                "%s of %s (%s)",
                molecular.type(),
                clinical.patientId(),
                Formats.date(molecular.date()));
    }

    @NotNull
    @Override
    public Table contents() {
        MolecularCharacteristicsGenerator characteristicsGenerator =
                new MolecularCharacteristicsGenerator(molecular, keyWidth + valueWidth);

        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Biopsy location"));
        table.addCell(biopsySummary());

        if (molecular.containsTumorCells()) {
            Stream.of(Maps.immutableEntry("Molecular tissue of origin prediction",
                                    createHighConfidenceTumorOriginPredictionOption().orElse("Inconclusive")),
                            Maps.immutableEntry("Tumor mutational load",
                                    characteristicsGenerator.createTMLStatusStringOption().orElse(Formats.VALUE_UNKNOWN)),
                            Maps.immutableEntry("Microsatellite (in)stability",
                                    characteristicsGenerator.createMSStabilityStringOption().orElse(Formats.VALUE_UNKNOWN)),
                            Maps.immutableEntry("HR status", characteristicsGenerator.createHRStatusStringOption().orElse(Formats.VALUE_UNKNOWN)),
                            Maps.immutableEntry("", ""),
                            Maps.immutableEntry("Genes with high driver mutation", formatStream(summarizer.keyGenesWithVariants())),
                            Maps.immutableEntry("Amplified genes", formatStream(summarizer.keyAmplifiedGenes())),
                            Maps.immutableEntry("Deleted genes", formatStream(summarizer.keyDeletedGenes())),
                            Maps.immutableEntry("Homozygously disrupted genes", formatStream(summarizer.keyHomozygouslyDisruptedGenes())),
                            Maps.immutableEntry("Gene fusions", formatStream(summarizer.keyFusionEvents())),
                            Maps.immutableEntry("Virus detection", formatStream(summarizer.keyVirusEvents())),
                            Maps.immutableEntry("", ""),
                            Maps.immutableEntry("Potentially actionable events with medium/low driver:",
                                    formatStream(summarizer.actionableEventsThatAreNotKeyDrivers())))
                    .flatMap(entry -> Stream.of(Cells.createKey(entry.getKey()), Cells.createValue(entry.getValue())))
                    .forEach(table::addCell);
        } else {
            table.addCell(Cells.createSpanningEntry("The received biomaterial(s) did not meet the requirements that are needed for "
                    + "high quality whole genome sequencing", table));
        }
        return table;
    }

    @NotNull
    private Cell biopsySummary() {
        String biopsyLocation = clinical.tumor().biopsyLocation();
        Double purity = molecular.characteristics().purity();
        if (biopsyLocation != null) {
            if (purity != null) {
                Text biopsyText = new Text(biopsyLocation).addStyle(Styles.tableHighlightStyle());
                Text purityText = new Text(String.format(ApplicationConfig.LOCALE, " (purity %d%%)", Math.round(purity * 100)));
                purityText.addStyle(molecular.hasSufficientQuality() ? Styles.tableHighlightStyle() : Styles.tableNoticeStyle());
                return Cells.create(new Paragraph().addAll(Arrays.asList(biopsyText, purityText)));
            } else {
                return Cells.createValue(biopsyLocation);
            }
        } else {
            return Cells.createValue(Formats.VALUE_UNKNOWN);
        }
    }

    private Optional<String> createHighConfidenceTumorOriginPredictionOption() {
        PredictedTumorOrigin predictedTumorOrigin = molecular.characteristics().predictedTumorOrigin();
        if (TumorOriginInterpreter.hasConfidentPrediction(predictedTumorOrigin) && molecular.hasSufficientQuality()) {
            return Optional.of(TumorOriginInterpreter.interpret(predictedTumorOrigin));
        } else {
            return Optional.empty();
        }
    }

    private String formatStream(Stream<String> stream) {
        String collected = stream.collect(Collectors.joining(Formats.COMMA_SEPARATOR));
        return collected.isEmpty() ? Formats.VALUE_NONE : collected;
    }
}
