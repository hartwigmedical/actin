package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;

import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.jetbrains.annotations.NotNull;

public class OrangeEvidenceEvaluator implements EvidenceEvaluator {

    @NotNull
    private final List<ServeRecord> records;
    @NotNull
    private final MutationMapper mutationMapper;

    @NotNull
    public static OrangeEvidenceEvaluator fromServeRecords(@NotNull List<ServeRecord> records) {
        return new OrangeEvidenceEvaluator(records, OrangeMutationMapper.fromServeRecords(records));
    }

    private OrangeEvidenceEvaluator(@NotNull final List<ServeRecord> records, @NotNull final MutationMapper mutationMapper) {
        this.records = records;
        this.mutationMapper = mutationMapper;
    }

    @Override
    public boolean isPotentiallyForTrialInclusion(@NotNull TreatmentEvidence evidence) {
        switch (evidence.type()) {
            case VIRAL_PRESENCE: {
                return true;
            }
            case SIGNATURE: {
                return hasInclusiveSignatureRecord(records, evidence.event());
            }
            case ACTIVATION: {
                return true;
            }
            case INACTIVATION: {
                return true;
            }
            case AMPLIFICATION: {
                return true;
            }
            case DELETION: {
                return true;
            }
            case PROMISCUOUS_FUSION: {
                return true;
            }
            case FUSION_PAIR: {
                return true;
            }
            case HOTSPOT_MUTATION: {
                return true;
            }
            case CODON_MUTATION: {
                return true;
            }
            case EXON_MUTATION: {
                return true;
            }
            case ANY_MUTATION: {
                return true;
            }
            default: {
                throw new IllegalArgumentException("Evidence of unrecognized type detected: " + evidence.type());
            }
        }
    }

    private static boolean hasInclusiveSignatureRecord(@NotNull List<ServeRecord> records, @NotNull String event) {
        switch (event) {
            case "High tumor mutation load": {
                return hasInclusiveTumorLoadRecord(records);
            }
            case "Microsatellite unstable": {
                return hasInclusiveMicrosatelliteRecord(records);
            }
            case "HR deficiency": {
                return hasInclusiveHRDeficiencyRecord(records);
            }
            default: {
                throw new IllegalStateException("Unrecognized signature evidence detected: " + event);
            }
        }
    }

    private static boolean hasInclusiveTumorLoadRecord(@NotNull List<ServeRecord> records) {
        return true;
    }

    private static boolean hasInclusiveMicrosatelliteRecord(@NotNull List<ServeRecord> records) {
        return true;
    }

    private static boolean hasInclusiveHRDeficiencyRecord(@NotNull List<ServeRecord> records) {
        return true;
    }
}
