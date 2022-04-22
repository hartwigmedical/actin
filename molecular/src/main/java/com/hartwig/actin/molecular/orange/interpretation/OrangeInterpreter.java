package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.molecular.orange.curation.ExternalTreatmentMapping;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OrangeInterpreter {

    private static final Logger LOGGER = LogManager.getLogger(OrangeInterpreter.class);

    static final String MICROSATELLITE_STABLE = "MSS";
    static final String MICROSATELLITE_UNSTABLE = "MSI";

    static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HR_DEFICIENT";
    static final String HOMOLOGOUS_REPAIR_PROFICIENT = "HR_PROFICIENT";
    static final String HOMOLOGOUS_REPAIR_UNKNOWN = "CANNOT_BE_DETERMINED";

    @NotNull
    private final OrangeEventMapper eventMapper;
    @NotNull
    private final OrangeEvidenceFactory evidenceFactory;

    @NotNull
    public static OrangeInterpreter create(@NotNull List<ServeRecord> records, @NotNull List<ExternalTreatmentMapping> mappings) {
        return new OrangeInterpreter(OrangeEventMapper.fromServeRecords(records),
                OrangeEvidenceFactory.create(records, mappings));
    }

    @VisibleForTesting
    OrangeInterpreter(@NotNull final OrangeEventMapper eventMapper, @NotNull final OrangeEvidenceFactory evidenceFactory) {
        this.eventMapper = eventMapper;
        this.evidenceFactory = evidenceFactory;
    }

    @NotNull
    public MolecularRecord interpret(@NotNull OrangeRecord record) {
        return ImmutableMolecularRecord.builder()
                .sampleId(record.sampleId())
                .type(ExperimentType.WGS)
                .date(record.reportDate())
                .hasReliableQuality(record.purple().hasReliableQuality())
                .characteristics(extractCharacteristics(record))
                .drivers(DriverExtraction.extract(record))
                .pharmaco(extractPharmaco(record.peach()))
                .evidence(evidenceFactory.create(record.protect()))
                .mappedEvents(eventMapper.map(record.protect()))
                .build();
    }

    @NotNull
    private static MolecularCharacteristics extractCharacteristics(@NotNull OrangeRecord record) {
        PredictedTumorOrigin predictedTumorOrigin = ImmutablePredictedTumorOrigin.builder()
                .tumorType(record.cuppa().predictedCancerType())
                .likelihood(record.cuppa().bestPredictionLikelihood())
                .build();

        PurpleRecord purple = record.purple();
        return ImmutableMolecularCharacteristics.builder()
                .purity(purple.purity())
                .hasReliablePurity(purple.hasReliablePurity())
                .predictedTumorOrigin(predictedTumorOrigin)
                .isMicrosatelliteUnstable(isMSI(purple.microsatelliteStabilityStatus()))
                .isHomologousRepairDeficient(isHRD(record.chord().hrStatus()))
                .tumorMutationalBurden(purple.tumorMutationalBurden())
                .tumorMutationalLoad(purple.tumorMutationalLoad())
                .build();
    }

    @NotNull
    private static Set<PharmacoEntry> extractPharmaco(@NotNull PeachRecord peach) {
        Map<String, List<PeachEntry>> peachEntryPerGene = Maps.newHashMap();

        for (PeachEntry entry : peach.entries()) {
            List<PeachEntry> entries = peachEntryPerGene.get(entry.gene());
            if (entries == null) {
                entries = Lists.newArrayList();
            }
            entries.add(entry);
            peachEntryPerGene.put(entry.gene(), entries);
        }

        Set<PharmacoEntry> entries = Sets.newHashSet();
        for (Map.Entry<String, List<PeachEntry>> mapEntry : peachEntryPerGene.entrySet()) {
            Set<Haplotype> haplotypes = Sets.newHashSet();
            for (PeachEntry entry : mapEntry.getValue()) {
                haplotypes.add(ImmutableHaplotype.builder().name(entry.haplotype()).function(entry.function()).build());
            }
            entries.add(ImmutablePharmacoEntry.builder().gene(mapEntry.getKey()).haplotypes(haplotypes).build());
        }
        return entries;
    }

    @Nullable
    private static Boolean isMSI(@NotNull String microsatelliteStatus) {
        if (microsatelliteStatus.equals(MICROSATELLITE_UNSTABLE)) {
            return true;
        } else if (microsatelliteStatus.equals(MICROSATELLITE_STABLE)) {
            return false;
        }

        LOGGER.warn("Cannot interpret microsatellite status '{}'", microsatelliteStatus);
        return null;
    }

    @Nullable
    private static Boolean isHRD(@NotNull String hrStatus) {
        switch (hrStatus) {
            case HOMOLOGOUS_REPAIR_DEFICIENT:
                return true;
            case HOMOLOGOUS_REPAIR_PROFICIENT:
                return false;
            case HOMOLOGOUS_REPAIR_UNKNOWN:
                return null;
        }

        LOGGER.warn("Cannot interpret homologous repair status '{}'", hrStatus);
        return null;
    }
}
