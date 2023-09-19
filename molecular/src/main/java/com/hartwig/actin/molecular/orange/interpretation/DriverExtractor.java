package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Collections;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.hmftools.datamodel.orange.OrangeRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

class DriverExtractor {

    private static final Logger LOGGER = LogManager.getLogger(DriverExtractor.class);

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
            @NotNull final HomozygousDisruptionExtractor homozygousDisruptionExtractor,
            @NotNull final DisruptionExtractor disruptionExtractor, @NotNull final FusionExtractor fusionExtractor,
            @NotNull final VirusExtractor virusExtractor) {
        this.variantExtractor = variantExtractor;
        this.copyNumberExtractor = copyNumberExtractor;
        this.homozygousDisruptionExtractor = homozygousDisruptionExtractor;
        this.disruptionExtractor = disruptionExtractor;
        this.fusionExtractor = fusionExtractor;
        this.virusExtractor = virusExtractor;
    }

    @NotNull
    public MolecularDrivers extract(@NotNull OrangeRecord record) {
        Set<Variant> variants = variantExtractor.extract(record.purple());
        LOGGER.info(" Extracted {} variants of which {} reportable", variants.size(), reportableCount(variants));

        Set<CopyNumber> copyNumbers = copyNumberExtractor.extract(record.purple());
        LOGGER.info(" Extracted {} copy numbers of which {} reportable", copyNumbers.size(), reportableCount(copyNumbers));

        Set<HomozygousDisruption> homozygousDisruptions = homozygousDisruptionExtractor.extractHomozygousDisruptions(record.linx());
        LOGGER.info(" Extracted {} homozygous disruptions of which {} reportable",
                homozygousDisruptions.size(),
                reportableCount(homozygousDisruptions));

        Set<Disruption> disruptions = disruptionExtractor.extractDisruptions(record.linx(), reportableLostGenes(copyNumbers));
        LOGGER.info(" Extracted {} disruptions of which {} reportable", disruptions.size(), reportableCount(disruptions));

        Set<Fusion> fusions = fusionExtractor.extract(record.linx());
        LOGGER.info(" Extracted {} fusions of which {} reportable", fusions.size(), reportableCount(fusions));

        Set<Virus> viruses = record.virusInterpreter().map(virusExtractor::extract).orElse(Collections.emptySet());
        LOGGER.info(" Extracted {} viruses of which {} reportable", viruses.size(), reportableCount(viruses));

        return ImmutableMolecularDrivers.builder()
                .variants(variants)
                .copyNumbers(copyNumbers)
                .homozygousDisruptions(homozygousDisruptions)
                .disruptions(disruptions)
                .fusions(fusions)
                .viruses(viruses)
                .build();
    }

    @NotNull
    @VisibleForTesting
    static Set<String> reportableLostGenes(@NotNull Iterable<CopyNumber> copyNumbers) {
        Set<String> lostGenes = Sets.newHashSet();
        for (CopyNumber copyNumber : copyNumbers) {
            if (copyNumber.isReportable() && copyNumber.type().isLoss()) {
                lostGenes.add(copyNumber.gene());
            }
        }
        return lostGenes;
    }

    @VisibleForTesting
    static <T extends Driver> int reportableCount(@NotNull Iterable<T> drivers) {
        int count = 0;
        for (T driver : drivers) {
            if (driver.isReportable()) {
                count++;
            }
        }
        return count;
    }
}
