package com.hartwig.actin.report.interpretation;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;

public class MolecularDriversInterpreter {

    private final MolecularDrivers molecularDrivers;
    private final EvaluatedCohortsInterpreter evaluatedCohortsInterpreter;

    public MolecularDriversInterpreter(MolecularDrivers molecularDrivers, EvaluatedCohortsInterpreter evaluatedCohortsInterpreter) {
        this.molecularDrivers = molecularDrivers;
        this.evaluatedCohortsInterpreter = evaluatedCohortsInterpreter;
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

    public boolean hasPotentiallySubClonalVariants() {
        return filteredVariants().anyMatch(ClonalityInterpreter::isPotentiallySubclonal);
    }

    public List<String> trialsForDriver(Driver driver) {
        return evaluatedCohortsInterpreter.trialsForDriver(driver);
    }

    private <T extends Driver> Stream<T> streamAndFilterDrivers(Set<T> drivers) {
        return drivers.stream().filter(driver -> driver.isReportable() || evaluatedCohortsInterpreter.driverIsActionable(driver));
    }
}
