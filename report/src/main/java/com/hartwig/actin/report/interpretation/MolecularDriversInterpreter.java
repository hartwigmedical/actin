package com.hartwig.actin.report.interpretation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;

public class MolecularDriversInterpreter {

    private final MolecularDrivers molecularDrivers;
    private final EvaluatedCohortsInterpreter evaluatedCohortsInterpreter;

    public static MolecularDriversInterpreter fromMolecularDriversAndEvaluatedCohorts(MolecularDrivers molecularDrivers,
            List<EvaluatedCohort> cohorts) {
        return new MolecularDriversInterpreter(molecularDrivers, new EvaluatedCohortsInterpreter(cohorts));
    }

    private MolecularDriversInterpreter(MolecularDrivers molecularDrivers, EvaluatedCohortsInterpreter evaluatedCohortsInterpreter) {
        this.molecularDrivers = molecularDrivers;
        this.evaluatedCohortsInterpreter = evaluatedCohortsInterpreter;
    }

    public Stream<String> keyVariantGenes() {
        return keyGenesForAlterations(molecularDrivers.variants().stream());
    }

    public Stream<String> keyAmplificationGenes() {
        return molecularDrivers.copyNumbers()
                .stream()
                .filter(copyNumber -> copyNumber.type().isGain())
                .filter(MolecularDriversInterpreter::isKeyDriver)
                .map(amp -> amp.gene() + (amp.type().equals(CopyNumberType.PARTIAL_GAIN) ? " (partial)" : ""))
                .distinct();
    }

    public Stream<String> keyDeletionGenes() {
        return keyGenesForAlterations(molecularDrivers.copyNumbers().stream().filter(copyNumber -> copyNumber.type().isLoss()));
    }

    public Stream<String> keyHomozygousDisruptionGenes() {
        return keyGenesForAlterations(molecularDrivers.homozygousDisruptions().stream());
    }

    public Stream<String> keyFusionEvents() {
        return molecularDrivers.fusions().stream().filter(MolecularDriversInterpreter::isKeyDriver).map(Driver::event).distinct();
    }

    public Stream<String> keyVirusEvents() {
        return molecularDrivers.viruses()
                .stream()
                .filter(MolecularDriversInterpreter::isKeyDriver)
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
                .filter(this::driverIsActionable)
                .map(Driver::event)
                .distinct();
    }

    public Stream<Variant> filteredVariants() {
        return streamAndFilterDrivers(molecularDrivers.variants());
    }

    public Stream<CopyNumber> filteredCopyNumbers() {
        return streamAndFilterDrivers(molecularDrivers.copyNumbers());
    }

    public Stream<HomozygousDisruption> filteredHomozygousDisruptions() {
        return streamAndFilterDrivers(molecularDrivers.homozygousDisruptions());
    }

    public Stream<Disruption> filteredDisruptions() {
        return streamAndFilterDrivers(molecularDrivers.disruptions());
    }

    public Stream<Fusion> filteredFusions() {
        return streamAndFilterDrivers(molecularDrivers.fusions());
    }

    public Stream<Virus> filteredViruses() {
        return streamAndFilterDrivers(molecularDrivers.viruses());
    }

    public List<String> getTrialsForDriver(Driver driver) {
        return evaluatedCohortsInterpreter.getTrialsForDriver(driver);
    }

    public boolean hasPotentiallySubClonalVariants() {
        return filteredVariants().anyMatch(ClonalityInterpreter::isPotentiallySubclonal);
    }

    private boolean driverIsActionable(Driver driver) {
        return !driver.evidence().externalEligibleTrials().isEmpty() || evaluatedCohortsInterpreter.hasTrialMatchingEvent(driver.event())
                || !driver.evidence().approvedTreatments().isEmpty();
    }

    private static boolean isKeyDriver(Driver driver) {
        return driver.driverLikelihood() == DriverLikelihood.HIGH && driver.isReportable();
    }

    private <T extends Driver> Stream<T> streamAndFilterDrivers(Set<T> drivers) {
        return drivers.stream().filter(driver -> driver.isReportable() || driverIsActionable(driver));
    }

    private <T extends GeneAlteration & Driver> Stream<String> keyGenesForAlterations(Stream<T> geneAlterationStream) {
        return geneAlterationStream.filter(MolecularDriversInterpreter::isKeyDriver).map(GeneAlteration::gene).distinct();
    }
}
