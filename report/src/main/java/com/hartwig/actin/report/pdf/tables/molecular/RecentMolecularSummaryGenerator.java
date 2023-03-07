package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter;
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

public class RecentMolecularSummaryGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final MolecularRecord molecular;
    @NotNull
    private final List<EvaluatedCohort> cohorts;
    private final float keyWidth;
    private final float valueWidth;

    public RecentMolecularSummaryGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
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

            Stream.of(Maps.immutableEntry("Tumor mutational load", characteristicsGenerator.createTMLStatusStringOption()),
                            Maps.immutableEntry("Microsatellite (in)stability", characteristicsGenerator.createMSStabilityStringOption()),
                            Maps.immutableEntry("HR status", characteristicsGenerator.createHRStatusStringOption()),
                            Maps.immutableEntry("", Optional.of("")),
                            Maps.immutableEntry("Genes with high driver mutation", genesWithHighDriverMutationStringOption()),
                            Maps.immutableEntry("Amplified genes", genesWithHighDriverAmplificationStringOption()),
                            Maps.immutableEntry("Deleted genes", genesWithHighDriverDeletionStringOption()),
                            Maps.immutableEntry("Homozygously disrupted genes", genesWithHighDriverHomozygousDisruptionStringOption()),
                            Maps.immutableEntry("Gene fusions", highDriverGeneFusionsStringOption()),
                            Maps.immutableEntry("Virus detection", highDriverVirusDetectionsStringOption()),
                            Maps.immutableEntry("", Optional.of("")),
                            Maps.immutableEntry("Potentially actionable events with medium/low driver:",
                                    actionableEventsWithoutHighDriverMutation()))
                    .flatMap(entry -> Stream.of(Cells.createKey(entry.getKey()),
                            Cells.createValue(entry.getValue().orElse(Formats.VALUE_UNKNOWN))))
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

    @NotNull
    private <T extends GeneAlteration & Driver> Optional<String> summaryStringOptionForGeneAlterations(Stream<T> geneAlterationStream) {
        String genes = geneAlterationStream.filter(variant -> variant.driverLikelihood() == DriverLikelihood.HIGH)
                .map(GeneAlteration::gene)
                .collect(Collectors.joining(", "));
        return Optional.of(genes.isEmpty() ? Formats.VALUE_NONE : genes);
    }

    @NotNull
    private Optional<String> genesWithHighDriverMutationStringOption() {
        return summaryStringOptionForGeneAlterations(molecular.drivers().variants().stream());
    }

    @NotNull
    private Optional<String> genesWithHighDriverAmplificationStringOption() {
        return summaryStringOptionForGeneAlterations(molecular.drivers()
                .copyNumbers()
                .stream()
                .filter(copyNumber -> copyNumber.type().isGain()));
    }

    @NotNull
    private Optional<String> genesWithHighDriverDeletionStringOption() {
        return summaryStringOptionForGeneAlterations(molecular.drivers()
                .copyNumbers()
                .stream()
                .filter(copyNumber -> copyNumber.type().isLoss()));
    }

    @NotNull
    private Optional<String> genesWithHighDriverHomozygousDisruptionStringOption() {
        return summaryStringOptionForGeneAlterations(molecular.drivers().homozygousDisruptions().stream());
    }

    @NotNull
    private Optional<String> highDriverGeneFusionsStringOption() {
        String fusions = molecular.drivers()
                .fusions()
                .stream()
                .filter(fusion -> fusion.driverLikelihood() == DriverLikelihood.HIGH)
                .map(Driver::event)
                .collect(Collectors.joining(", "));
        return Optional.of(fusions.isEmpty() ? Formats.VALUE_NONE : fusions);
    }

    @NotNull
    private Optional<String> highDriverVirusDetectionsStringOption() {
        String fusions = molecular.drivers()
                .viruses()
                .stream()
                .filter(virus -> virus.driverLikelihood() == DriverLikelihood.HIGH)
                .map(virus -> String.format("%s (%s integrations detected)", virus.type(), virus.integrations()))
                .collect(Collectors.joining(", "));
        return Optional.of(fusions.isEmpty() ? Formats.VALUE_NONE : fusions);
    }

    @NotNull
    private Optional<String> actionableEventsWithoutHighDriverMutation() {
        Set<String> eventsWithActinTrials = cohorts.stream()
                .filter(EvaluatedCohort::isPotentiallyEligible)
                .filter(EvaluatedCohort::isOpen)
                .flatMap(trial -> trial.molecularEvents().stream())
                .collect(Collectors.toSet());

        Stream<? extends Driver> nonDisruptionDrivers = Stream.of(molecular.drivers().variants(),
                        molecular.drivers().copyNumbers(),
                        molecular.drivers().fusions(),
                        molecular.drivers().homozygousDisruptions(),
                        molecular.drivers().viruses())
                .flatMap(Collection::stream)
                .filter(driver -> driver.driverLikelihood() != DriverLikelihood.HIGH);

        String events = Stream.concat(nonDisruptionDrivers, molecular.drivers().disruptions().stream())
                .filter(driver -> !driver.evidence().externalEligibleTrials().isEmpty() || eventsWithActinTrials.contains(driver.event())
                        || !driver.evidence().approvedTreatments().isEmpty())
                .map(Driver::event)
                .collect(Collectors.joining(", "));

        return Optional.of(events.isEmpty() ? Formats.VALUE_NONE : events);
    }
}
