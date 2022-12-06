package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;

import org.jetbrains.annotations.NotNull;

class DriverExtractor {

    @NotNull
    private final VariantExtractor variantExtractor;
    @NotNull
    private final CopyNumberExtractor copyNumberExtractor;
    @NotNull
    private final HomozygousDisruptionExtractor homozygousDisruptionExtractor;
    @NotNull
    private final DisruptionExtractor disruptionExtractor;
    @NotNull
    private final FusionExtractor fusionExtractor;
    @NotNull
    private final VirusExtractor virusExtractor;

    @NotNull
    public static DriverExtractor create(@NotNull GeneFilter geneFilter, @NotNull EvidenceDatabase evidenceDatabase) {
        return new DriverExtractor(new VariantExtractor(geneFilter, evidenceDatabase),
                new CopyNumberExtractor(geneFilter, evidenceDatabase),
                new HomozygousDisruptionExtractor(geneFilter, evidenceDatabase),
                new DisruptionExtractor(geneFilter, evidenceDatabase),
                new FusionExtractor(geneFilter, evidenceDatabase),
                new VirusExtractor(evidenceDatabase));
    }

    private DriverExtractor(@NotNull final VariantExtractor variantExtractor, @NotNull final CopyNumberExtractor copyNumberExtractor,
            @NotNull final HomozygousDisruptionExtractor homozygousDisruptionExtractor, @NotNull final DisruptionExtractor disruptionExtractor,
            @NotNull final FusionExtractor fusionExtractor, @NotNull final VirusExtractor virusExtractor) {
        this.variantExtractor = variantExtractor;
        this.copyNumberExtractor = copyNumberExtractor;
        this.homozygousDisruptionExtractor = homozygousDisruptionExtractor;
        this.disruptionExtractor = disruptionExtractor;
        this.fusionExtractor = fusionExtractor;
        this.virusExtractor = virusExtractor;
    }

    @NotNull
    public MolecularDrivers extract(@NotNull OrangeRecord record) {
        // In case purple contains no tumor cells, we wipe all drivers.
        if (!record.purple().fit().hasReliablePurity()) {
            return ImmutableMolecularDrivers.builder().build();
        }

        Set<Loss> losses = copyNumberExtractor.extractLosses(record.purple());

        return ImmutableMolecularDrivers.builder()
                .variants(variantExtractor.extract(record.purple()))
                .amplifications(copyNumberExtractor.extractAmplifications(record.purple()))
                .losses(losses)
                .homozygousDisruptions(homozygousDisruptionExtractor.extractHomozygousDisruptions(record.linx()))
                .disruptions(disruptionExtractor.extractDisruptions(record.linx(), losses))
                .fusions(fusionExtractor.extract(record.linx()))
                .viruses(virusExtractor.extract(record.virusInterpreter()))
                .build();
    }
}
