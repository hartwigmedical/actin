package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.util.ApplicationConfig;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

import org.jetbrains.annotations.NotNull;

public class RecentMolecularSummaryGenerator implements TableGenerator {

    private static final double PURITY_WARN_THRESHOLD = 0.20;
    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final MolecularRecord molecular;
    @NotNull
    private final List<EvaluatedTrial> trials;
    private final float keyWidth;
    private final float valueWidth;

    public RecentMolecularSummaryGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            @NotNull final List<EvaluatedTrial> trials, final float keyWidth, final float valueWidth) {
        this.clinical = clinical;
        this.molecular = molecular;
        this.trials = trials;
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

        if (molecular.hasSufficientQuality()) {
            if (TumorDetailsInterpreter.isCUP(clinical.tumor())) {
                table.addCell(Cells.createKey("Molecular tissue of origin prediction"));
                table.addCell(characteristicsGenerator.createPredictedTumorOriginCell());
            }

            Stream.of(Maps.immutableEntry("Tumor mutational load",
                                    characteristicsGenerator.createTMLStatusCell().setBorderBottom(Border.NO_BORDER)),
                            Maps.immutableEntry("Microsatellite (in)stability",
                                    characteristicsGenerator.createMSStabilityCell().setBorderBottom(Border.NO_BORDER)),
                            Maps.immutableEntry("HR status", characteristicsGenerator.createHRStatusCell().setBorderBottom(Border.NO_BORDER)),
                            Maps.immutableEntry("", Cells.createEmpty()),
                            Maps.immutableEntry("Genes with high driver mutation", genesWithHighDriverMutationCell()),
                            Maps.immutableEntry("Amplified genes", genesWithHighDriverCopyNumberCell(true)),
                            Maps.immutableEntry("Deleted genes", genesWithHighDriverCopyNumberCell(false)),
                            Maps.immutableEntry("Homozygously disrupted genes", genesWithHighDriverHomozygousDisruptionCell()),
                            Maps.immutableEntry("Gene fusions", highDriverGeneFusionsCell()),
                            Maps.immutableEntry("Virus detection", highDriverVirusDetectionsCell()),
                            Maps.immutableEntry("", Cells.createEmpty()),
                            Maps.immutableEntry("Actionable genes with medium/low driver:", actionableGenesWithMedOrLowDriverMutation()))
                    .flatMap(entry -> Stream.of(Cells.createKey(entry.getKey()), entry.getValue()))
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
                Text purityText = new Text(String.format(ApplicationConfig.LOCALE, " (purity %s%%)", purity * 100));
                purityText.addStyle((purity < PURITY_WARN_THRESHOLD) ? Styles.tableNoticeStyle() : Styles.tableHighlightStyle());
                return Cells.create(new Paragraph().addAll(Arrays.asList(biopsyText, purityText)));
            } else {
                return Cells.createValue(biopsyLocation);
            }
        } else {
            return Cells.createValue(Formats.VALUE_UNKNOWN);
        }
    }

    @NotNull
    private <T extends GeneAlteration & Driver> Cell summaryCellForGeneAlterations(Stream<T> geneAlterationStream) {
        return Cells.createValue(geneAlterationStream.filter(variant -> variant.driverLikelihood() == DriverLikelihood.HIGH)
                .map(GeneAlteration::gene)
                .collect(Collectors.joining(", ")));
    }

    @NotNull
    private Cell genesWithHighDriverMutationCell() {
        return summaryCellForGeneAlterations(molecular.drivers().variants().stream());
    }

    @NotNull
    private Cell genesWithHighDriverCopyNumberCell(boolean isGain) {
        return summaryCellForGeneAlterations(molecular.drivers()
                .copyNumbers()
                .stream()
                .filter(copyNumber -> copyNumber.type().isGain() == isGain));
    }

    @NotNull
    private Cell genesWithHighDriverHomozygousDisruptionCell() {
        return summaryCellForGeneAlterations(molecular.drivers().homozygousDisruptions().stream());
    }

    @NotNull
    private Cell highDriverGeneFusionsCell() {
        String fusions = molecular.drivers()
                .fusions()
                .stream()
                .filter(fusion -> fusion.driverLikelihood() == DriverLikelihood.HIGH)
                .map(Driver::event)
                .collect(Collectors.joining(", "));
        return Cells.createValue(fusions);
    }

    @NotNull
    private Cell highDriverVirusDetectionsCell() {
        String fusions = molecular.drivers()
                .viruses()
                .stream()
                .filter(virus -> virus.driverLikelihood() == DriverLikelihood.HIGH)
                .map(virus -> String.format("%s (%s integrations detected)", virus.event(), virus.integrations()))
                .collect(Collectors.joining(", "));
        return Cells.createValue(fusions);
    }

    @NotNull
    private Cell actionableGenesWithMedOrLowDriverMutation() {
        Set<DriverLikelihood> allowedLikelihoods = Set.of(DriverLikelihood.LOW, DriverLikelihood.MEDIUM);

        Set<String> eventsWithActinTrials = trials.stream()
                .filter(EvaluatedTrial::isPotentiallyEligible)
                .filter(EvaluatedTrial::isOpen)
                .flatMap(trial -> trial.molecularEvents().stream())
                .collect(Collectors.toSet());

        String genes = Stream.concat(molecular.drivers().variants().stream(), molecular.drivers().disruptions().stream())
                .filter(driver -> allowedLikelihoods.contains(driver.driverLikelihood()))
                .filter(driver -> !driver.evidence().externalEligibleTrials().isEmpty() || eventsWithActinTrials.contains(driver.event()))
                .map(GeneAlteration::gene)
                .collect(Collectors.joining(", "));

        return Cells.createValue(genes);
    }
}
