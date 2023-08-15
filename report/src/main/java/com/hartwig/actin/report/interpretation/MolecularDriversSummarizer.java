package com.hartwig.actin.report.interpretation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;

public class MolecularDriversSummarizer {

    private final MolecularDrivers molecularDrivers;
    private final EvaluatedCohortsInterpreter evaluatedCohortsInterpreter;

    private MolecularDriversSummarizer(MolecularDrivers molecularDrivers, EvaluatedCohortsInterpreter evaluatedCohortsInterpreter) {
        this.molecularDrivers = molecularDrivers;
        this.evaluatedCohortsInterpreter = evaluatedCohortsInterpreter;
    }

    public static MolecularDriversSummarizer fromMolecularDriversAndEvaluatedCohorts(MolecularDrivers molecularDrivers,
            List<EvaluatedCohort> cohorts) {
        return new MolecularDriversSummarizer(molecularDrivers, new EvaluatedCohortsInterpreter(cohorts));
    }

    public Stream<String> keyGenesWithVariants() {
        return keyGenesForAlterations(molecularDrivers.variants().stream());
    }

    public Stream<String> keyAmplifiedGenes() {
        return molecularDrivers.copyNumbers()
                .stream()
                .filter(copyNumber -> copyNumber.type().isGain())
                .filter(MolecularDriversSummarizer::isKeyDriver)
                .map(amp -> amp.gene() + (amp.type().equals(CopyNumberType.PARTIAL_GAIN) ? " (partial)" : ""))
                .distinct();
    }

    public Stream<String> keyDeletedGenes() {
        return keyGenesForAlterations(molecularDrivers.copyNumbers().stream().filter(copyNumber -> copyNumber.type().isLoss()));
    }

    public Stream<String> keyHomozygouslyDisruptedGenes() {
        return keyGenesForAlterations(molecularDrivers.homozygousDisruptions().stream());
    }

    public Stream<String> keyFusionEvents() {
        return molecularDrivers.fusions().stream().filter(MolecularDriversSummarizer::isKeyDriver).map(Driver::event).distinct();
    }

    public Stream<String> keyVirusEvents() {
        return molecularDrivers.viruses()
                .stream()
                .filter(MolecularDriversSummarizer::isKeyDriver)
                .map(virus -> String.format("%s (%s integrations detected)", virus.type(), virus.integrations()))
                .distinct();
    }

    public Stream<String> actionableEventsThatAreNotKeyDrivers() {
        Stream<? extends Driver> nonDisruptionDrivers = Stream.of(molecularDrivers.variants(),
                molecularDrivers.copyNumbers(),
                molecularDrivers.fusions(),
                molecularDrivers.homozygousDisruptions(),
                molecularDrivers.viruses()).flatMap(Collection::stream).filter(driver -> !isKeyDriver(driver));

        return Stream.concat(nonDisruptionDrivers, molecularDrivers.disruptions().stream())
                .filter(evaluatedCohortsInterpreter::driverIsActionable)
                .map(Driver::event)
                .distinct();
    }

    private static boolean isKeyDriver(Driver driver) {
        return driver.driverLikelihood() == DriverLikelihood.HIGH && driver.isReportable();
    }

    private static <T extends GeneAlteration & Driver> Stream<String> keyGenesForAlterations(Stream<T> geneAlterationStream) {
        return geneAlterationStream.filter(MolecularDriversSummarizer::isKeyDriver).map(GeneAlteration::gene).distinct();
    }
}
