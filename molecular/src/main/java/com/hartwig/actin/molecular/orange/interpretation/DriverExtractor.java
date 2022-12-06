package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;

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

        Set<Amplification> amplifications = copyNumberExtractor.extractAmplifications(record.purple());
        LOGGER.info(" Extracted {} amplifications of which {} reportable", amplifications.size(), reportableCount(amplifications));

        Set<Loss> losses = copyNumberExtractor.extractLosses(record.purple());
        LOGGER.info(" Extracted {} losses of which {} reportable", losses.size(), reportableCount(losses));

        Set<HomozygousDisruption> homozygousDisruptions = homozygousDisruptionExtractor.extractHomozygousDisruptions(record.linx());
        LOGGER.info(" Extracted {} homozygous disruptions of which {} reportable",
                homozygousDisruptions.size(),
                reportableCount(homozygousDisruptions));

        Set<Disruption> disruptions = disruptionExtractor.extractDisruptions(record.linx(), losses);
        LOGGER.info(" Extracted {} disruptions of which {} reportable", disruptions.size(), reportableCount(disruptions));

        Set<Fusion> fusions = fusionExtractor.extract(record.linx());
        LOGGER.info(" Extracted {} fusions of which {} reportable", fusions.size(), reportableCount(fusions));

        Set<Virus> viruses = virusExtractor.extract(record.virusInterpreter());
        LOGGER.info(" Extracted {} viruses of which {} reportable", viruses.size(), reportableCount(viruses));

        return ImmutableMolecularDrivers.builder()
                .variants(variants)
                .amplifications(amplifications)
                .losses(losses)
                .homozygousDisruptions(homozygousDisruptions)
                .disruptions(disruptions)
                .fusions(fusions)
                .viruses(viruses)
                .build();
    }

    private static <T extends Driver> int reportableCount(@NotNull Set<T> drivers) {
        int count = 0;
        for (T driver : drivers) {
            if (driver.isReportable()) {
                count++;
            }
        }
        return count;
    }
}
