package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.mapping.FusionGene;
import com.hartwig.actin.molecular.datamodel.mapping.GeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableGeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableInactivatedGene;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableMappedActinEvents;
import com.hartwig.actin.molecular.datamodel.mapping.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.mapping.MappedActinEvents;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.util.FusionParser;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.jetbrains.annotations.NotNull;

class OrangeEventMapper {

    private static final Set<EvidenceType> MUTATION_TYPES =
            Sets.newHashSet(EvidenceType.HOTSPOT_MUTATION, EvidenceType.CODON_MUTATION, EvidenceType.EXON_MUTATION);

    private static final Set<EvidenceType> ACTIVATION_TYPES = Sets.newHashSet(EvidenceType.ACTIVATION);
    private static final Set<EvidenceType> INACTIVATION_TYPES = Sets.newHashSet(EvidenceType.INACTIVATION);
    private static final Set<EvidenceType> AMPLIFICATION_TYPES = Sets.newHashSet(EvidenceType.AMPLIFICATION);
    private static final Set<EvidenceType> FUSION_TYPES = Sets.newHashSet(EvidenceType.FUSION_PAIR, EvidenceType.PROMISCUOUS_FUSION);

    static final String ACTIN_SOURCE = "ACTIN";

    @NotNull
    private final MutationMapper mutationMapper;

    @NotNull
    public static OrangeEventMapper fromServeRecords(@NotNull List<ServeRecord> records) {
        return new OrangeEventMapper(OrangeMutationMapper.fromServeRecords(records));
    }

    @VisibleForTesting
    OrangeEventMapper(@NotNull final MutationMapper mutationMapper) {
        this.mutationMapper = mutationMapper;
    }

    @NotNull
    public MappedActinEvents map(@NotNull ProtectRecord protect) {
        List<ProtectEvidence> evidences = reportedFromActinSource(protect.evidences());

        return ImmutableMappedActinEvents.builder()
                .mutations(mapMutations(evidences, mutationMapper))
                .activatedGenes(mapActivatedGenes(evidences))
                .inactivatedGenes(mapInactivatedGenes(evidences))
                .amplifiedGenes(mapAmplifiedGenes(evidences))
                .wildtypeGenes(mapWildtypeGenes(evidences))
                .fusions(mapFusions(evidences))
                .build();
    }

    @NotNull
    private static Set<GeneMutation> mapMutations(@NotNull List<ProtectEvidence> evidences, @NotNull MutationMapper mutationMapper) {
        Set<GeneMutation> geneMutations = Sets.newHashSet();

        for (ProtectEvidence evidence : evidences) {
            if (MUTATION_TYPES.contains(evidence.type())) {
                for (String mutation : mutationMapper.map(evidence)) {
                    geneMutations.add(ImmutableGeneMutation.builder().gene(evidence.gene()).mutation(mutation).build());
                }
            }
        }

        return geneMutations;
    }

    @NotNull
    private static Set<String> mapActivatedGenes(@NotNull List<ProtectEvidence> evidences) {
        Set<String> activatedGenes = Sets.newHashSet();
        for (ProtectEvidence evidence : evidences) {
            if (ACTIVATION_TYPES.contains(evidence.type())) {
                activatedGenes.add(evidence.gene());
            }
        }

        return activatedGenes;
    }

    @NotNull
    private static Set<InactivatedGene> mapInactivatedGenes(@NotNull List<ProtectEvidence> evidences) {
        Set<InactivatedGene> inactivatedGenes = Sets.newHashSet();
        for (ProtectEvidence evidence : evidences) {
            if (INACTIVATION_TYPES.contains(evidence.type())) {
                boolean hasBeenDeleted = evidence.event().equals("full loss") || evidence.event().equals("partial loss");

                inactivatedGenes.add(ImmutableInactivatedGene.builder().gene(evidence.gene()).hasBeenDeleted(hasBeenDeleted).build());
            }
        }
        return inactivatedGenes;
    }

    @NotNull
    private static Set<String> mapAmplifiedGenes(@NotNull List<ProtectEvidence> evidences) {
        Set<String> amplifiedGenes = Sets.newHashSet();

        for (ProtectEvidence evidence : evidences) {
            if (AMPLIFICATION_TYPES.contains(evidence.type())) {
                amplifiedGenes.add(evidence.gene());
            }
        }

        return amplifiedGenes;
    }

    @NotNull
    private static Set<String> mapWildtypeGenes(@NotNull List<ProtectEvidence> evidences) {
        // TODO Implement wildtype genes.
        return Sets.newHashSet();

    }

    @NotNull
    private static Set<FusionGene> mapFusions(@NotNull List<ProtectEvidence> evidences) {
        Set<FusionGene> fusionGenes = Sets.newHashSet();

        for (ProtectEvidence evidence : evidences) {
            if (FUSION_TYPES.contains(evidence.type())) {
                fusionGenes.add(FusionParser.fromEvidenceEvent(evidence.event()));
            }
        }

        return fusionGenes;
    }

    @NotNull
    private static List<ProtectEvidence> reportedFromActinSource(@NotNull Iterable<ProtectEvidence> evidences) {
        List<ProtectEvidence> filtered = Lists.newArrayList();
        for (ProtectEvidence evidence : evidences) {
            if (evidence.sources().contains(ACTIN_SOURCE) && evidence.reported()) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }
}
