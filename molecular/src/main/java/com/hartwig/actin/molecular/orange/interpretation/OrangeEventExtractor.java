package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.ImmutableGeneMutation;
import com.hartwig.actin.molecular.datamodel.ImmutableInactivatedGene;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
import com.hartwig.actin.molecular.orange.util.FusionParser;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.jetbrains.annotations.NotNull;

class OrangeEventExtractor {

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
    public static OrangeEventExtractor fromServeRecords(@NotNull List<ServeRecord> records) {
        return new OrangeEventExtractor(OrangeMutationMapper.fromServeRecords(records));
    }

    @VisibleForTesting
    OrangeEventExtractor(@NotNull final MutationMapper mutationMapper) {
        this.mutationMapper = mutationMapper;
    }

    @NotNull
    public OrangeEventExtraction extract(@NotNull OrangeRecord record) {
        List<TreatmentEvidence> evidences = reportedFromActinSource(record.evidences());

        return ImmutableOrangeEventExtraction.builder()
                .mutations(extractMutations(evidences, mutationMapper))
                .activatedGenes(extractActivatedGenes(evidences))
                .inactivatedGenes(extractInactivatedGenes(evidences))
                .amplifiedGenes(extractAmplifiedGenes(evidences))
                .wildtypeGenes(extractWildtypeGenes(evidences))
                .fusions(extractFusions(evidences))
                .build();
    }

    @NotNull
    private static Set<GeneMutation> extractMutations(@NotNull List<TreatmentEvidence> evidences, @NotNull MutationMapper mutationMapper) {
        Set<GeneMutation> geneMutations = Sets.newHashSet();

        for (TreatmentEvidence evidence : evidences) {
            if (MUTATION_TYPES.contains(evidence.type())) {
                for (String mutation : mutationMapper.map(evidence)) {
                    geneMutations.add(ImmutableGeneMutation.builder().gene(evidence.gene()).mutation(mutation).build());
                }
            }
        }

        return geneMutations;
    }

    @NotNull
    private static Set<String> extractActivatedGenes(@NotNull List<TreatmentEvidence> evidences) {
        Set<String> activatedGenes = Sets.newHashSet();
        for (TreatmentEvidence evidence : evidences) {
            if (ACTIVATION_TYPES.contains(evidence.type())) {
                activatedGenes.add(evidence.gene());
            }
        }

        return activatedGenes;
    }

    @NotNull
    private static Set<InactivatedGene> extractInactivatedGenes(@NotNull List<TreatmentEvidence> evidences) {
        Set<InactivatedGene> inactivatedGenes = Sets.newHashSet();
        for (TreatmentEvidence evidence : evidences) {
            if (INACTIVATION_TYPES.contains(evidence.type())) {
                boolean hasBeenDeleted = evidence.event().equals("full loss") || evidence.event().equals("partial loss");

                inactivatedGenes.add(ImmutableInactivatedGene.builder().gene(evidence.gene()).hasBeenDeleted(hasBeenDeleted).build());
            }
        }
        return inactivatedGenes;
    }

    @NotNull
    private static Set<String> extractAmplifiedGenes(@NotNull List<TreatmentEvidence> evidences) {
        Set<String> amplifiedGenes = Sets.newHashSet();

        for (TreatmentEvidence evidence : evidences) {
            if (AMPLIFICATION_TYPES.contains(evidence.type())) {
                amplifiedGenes.add(evidence.gene());
            }
        }

        return amplifiedGenes;
    }

    @NotNull
    private static Set<String> extractWildtypeGenes(@NotNull List<TreatmentEvidence> evidences) {
        // TODO Implement wildtype genes.
        return Sets.newHashSet();

    }

    @NotNull
    private static Set<FusionGene> extractFusions(@NotNull List<TreatmentEvidence> evidences) {
        Set<FusionGene> fusionGenes = Sets.newHashSet();

        for (TreatmentEvidence evidence : evidences) {
            if (FUSION_TYPES.contains(evidence.type())) {
                fusionGenes.add(FusionParser.fromEvidenceEvent(evidence.event()));
            }
        }

        return fusionGenes;
    }

    @NotNull
    private static List<TreatmentEvidence> reportedFromActinSource(@NotNull List<TreatmentEvidence> evidences) {
        List<TreatmentEvidence> filtered = Lists.newArrayList();
        for (TreatmentEvidence evidence : evidences) {
            if (evidence.sources().contains(ACTIN_SOURCE) && evidence.reported()) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }
}
