package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter;
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

public class RecentMolecularSummaryGenerator implements TableGenerator {

    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final MolecularRecord molecular;
    @NotNull
    private final List<EvaluatedCohort> cohorts;
    private final float totalWidth;

    public RecentMolecularSummaryGenerator(@NotNull final ClinicalRecord clinical, @NotNull final MolecularRecord molecular,
            @NotNull final List<EvaluatedCohort> cohorts, final float totalWidth) {
        this.clinical = clinical;
        this.molecular = molecular;
        this.cohorts = cohorts;
        this.totalWidth = totalWidth;
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
        MolecularCharacteristicsGenerator characteristicsGenerator = new MolecularCharacteristicsGenerator(molecular, totalWidth);

        float keyWidth = 0.3F * totalWidth;
        float valueWidth = 0.2F * totalWidth;
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth, keyWidth, valueWidth);

        table.addCell(Cells.createKey("Biopsy location", 1));
        table.addCell(biopsySummary(3));

        if (molecular.containsTumorCells()) {
            if (TumorDetailsInterpreter.isCUP(clinical.tumor())) {
                table.addCell(Cells.createKey("Molecular tissue of origin prediction", 1));
                table.addCell(predictedTumorOrigin(3));
            }

            table.addCell(Cells.createKey("Tumor mutational load", 1));
            table.addCell(Cells.createValue(characteristicsGenerator.createTMLStatusStringOption().orElse(Formats.VALUE_UNKNOWN), 1));
            table.addCell(Cells.createKey("Tumor mutational burden", 1));
            table.addCell(Cells.createValue(characteristicsGenerator.createTMBStatusStringOption().orElse(Formats.VALUE_UNKNOWN), 1));

            table.addCell(Cells.createKey("Microsatellite (in)stability", 1));
            table.addCell(Cells.createValue(characteristicsGenerator.createMSStabilityStringOption().orElse(Formats.VALUE_UNKNOWN), 1));
            table.addCell(Cells.createKey("HR status", 1));
            table.addCell(Cells.createValue(characteristicsGenerator.createHRStatusStringOption().orElse(Formats.VALUE_UNKNOWN), 1));

            table.addCell(Cells.createKey("Genes with high driver mutation", 1));
            table.addCell(Cells.createValue(genesWithHighDriverMutationString(), 3));

            table.addCell(Cells.createKey("Amplified genes", 1));
            table.addCell(Cells.createValue(genesWithHighDriverAmplificationString(), 3));

            table.addCell(Cells.createKey("Gene fusions", 1));
            table.addCell(Cells.createValue(highDriverGeneFusionsString(), 3));

            table.addCell(Cells.createKey("Deleted genes", 1));
            table.addCell(Cells.createValue(genesWithHighDriverDeletionString(), 3));

            table.addCell(Cells.createKey("Homozygously disrupted genes", 1));
            table.addCell(Cells.createValue(genesWithHighDriverHomozygousDisruptionString(), 3));

            table.addCell(Cells.createKey("Virus detection", 1));
            table.addCell(Cells.createValue(highDriverVirusDetectionsString(), 3));

            table.addCell(Cells.createKey("Potential biomarkers with medium/low driver", 1));
            table.addCell(Cells.createValue(actionableEventsWithoutHighDriverMutation(), 3));

            table.addCell(Cells.createKey("", 2));
            table.addCell(Cells.createValue("", 2));
        } else {
            table.addCell(Cells.createSpanningEntry("The received biomaterial(s) did not meet the requirements that are needed for "
                    + "high quality whole genome sequencing", table));
        }
        return table;
    }

    @NotNull
    private Cell biopsySummary(int cols) {
        String biopsyLocation = clinical.tumor().biopsyLocation();
        Double purity = molecular.characteristics().purity();
        if (biopsyLocation != null) {
            if (purity != null) {
                Text biopsyText = new Text(biopsyLocation).addStyle(Styles.tableHighlightStyle());
                Text purityText = new Text(String.format(ApplicationConfig.LOCALE, " (purity %d%%)", Math.round(purity * 100)));
                purityText.addStyle(molecular.hasSufficientQuality() ? Styles.tableHighlightStyle() : Styles.tableNoticeStyle());
                return Cells.create(new Paragraph().addAll(Arrays.asList(biopsyText, purityText)), cols);
            } else {
                return Cells.createValue(biopsyLocation, cols);
            }
        } else {
            return Cells.createValue(Formats.VALUE_UNKNOWN, cols);
        }
    }

    @NotNull
    private Cell predictedTumorOrigin(int cols) {
        PredictedTumorOrigin predictedTumorOrigin = molecular.characteristics().predictedTumorOrigin();
        return Cells.createValue(TumorOriginInterpreter.interpret(predictedTumorOrigin), cols);
    }

    @NotNull
    private <T extends GeneAlteration & Driver> String summaryStringOptionForGeneAlterations(Stream<T> geneAlterationStream) {
        String genes = geneAlterationStream.filter(driver -> driver.driverLikelihood() == DriverLikelihood.HIGH)
                .map(GeneAlteration::gene)
                .distinct()
                .collect(Collectors.joining(Formats.COMMA_SEPARATOR));
        return genes.isEmpty() ? Formats.VALUE_NONE : genes;
    }

    @NotNull
    private String genesWithHighDriverMutationString() {
        return summaryStringOptionForGeneAlterations(molecular.drivers().variants().stream());
    }

    @NotNull
    private String genesWithHighDriverAmplificationString() {
        return summaryStringOptionForGeneAlterations(molecular.drivers()
                .copyNumbers()
                .stream()
                .filter(copyNumber -> copyNumber.type().isGain()));
    }

    @NotNull
    private String genesWithHighDriverDeletionString() {
        return summaryStringOptionForGeneAlterations(molecular.drivers()
                .copyNumbers()
                .stream()
                .filter(copyNumber -> copyNumber.type().isLoss()));
    }

    @NotNull
    private String genesWithHighDriverHomozygousDisruptionString() {
        return summaryStringOptionForGeneAlterations(molecular.drivers().homozygousDisruptions().stream());
    }

    @NotNull
    private String highDriverGeneFusionsString() {
        String fusions = molecular.drivers()
                .fusions()
                .stream()
                .filter(fusion -> fusion.driverLikelihood() == DriverLikelihood.HIGH)
                .map(Driver::event)
                .collect(Collectors.joining(Formats.COMMA_SEPARATOR));
        return fusions.isEmpty() ? Formats.VALUE_NONE : fusions;
    }

    @NotNull
    private String highDriverVirusDetectionsString() {
        String fusions = molecular.drivers()
                .viruses()
                .stream()
                .filter(virus -> virus.driverLikelihood() == DriverLikelihood.HIGH)
                .map(virus -> String.format("%s (%s integrations detected)", virus.type(), virus.integrations()))
                .collect(Collectors.joining(Formats.COMMA_SEPARATOR));
        return fusions.isEmpty() ? Formats.VALUE_NONE : fusions;
    }

    @NotNull
    private String actionableEventsWithoutHighDriverMutation() {
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
                .collect(Collectors.joining(Formats.COMMA_SEPARATOR));

        return events.isEmpty() ? Formats.VALUE_NONE : events;
    }
}
