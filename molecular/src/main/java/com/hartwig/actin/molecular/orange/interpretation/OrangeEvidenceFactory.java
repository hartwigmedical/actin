package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableEvidenceEntry;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.filter.ApplicabilityFilter;
import com.hartwig.actin.molecular.orange.util.EvidenceFormatter;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

class OrangeEvidenceFactory {

    private static final Logger LOGGER = LogManager.getLogger(OrangeEvidenceFactory.class);

    static final String ACTIN_SOURCE = "ACTIN";
    static final String ICLUSION_SOURCE = "ICLUSION";
    static final String CKB_SOURCE = "CKB";

    @NotNull
    private final EvidenceEvaluator evidenceEvaluator;

    @NotNull
    public static OrangeEvidenceFactory fromServeRecords(@NotNull List<ServeRecord> records) {
        return new OrangeEvidenceFactory(OrangeEvidenceEvaluator.fromServeRecords(records));
    }

    @VisibleForTesting
    OrangeEvidenceFactory(@NotNull final EvidenceEvaluator evidenceEvaluator) {
        this.evidenceEvaluator = evidenceEvaluator;
    }

    @NotNull
    public Set<EvidenceEntry> createActinTrials(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, ACTIN_SOURCE)) {
            if (evidenceEvaluator.isPotentiallyForTrialInclusion(evidence)) {
                result.add(toMolecularEvidence(evidence));
            } else {
                String evidenceString = EvidenceFormatter.format(evidence);
                LOGGER.debug("Filtered evidence from ACTIN trials because it is not used as inclusion criteria: {}", evidenceString);
            }
        }
        return result;
    }

    @NotNull
    public Set<EvidenceEntry> createExternalTrials(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, ICLUSION_SOURCE)) {
            result.add(toMolecularEvidence(evidence));
        }
        return result;
    }

    @NotNull
    public Set<EvidenceEntry> createApprovedResponsiveEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, CKB_SOURCE)) {
            if (evidence.direction().isResponsive() && isApproved(evidence)) {
                result.add(toMolecularEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    public Set<EvidenceEntry> createExperimentalResponsiveEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();

        List<ProtectEvidence> ckbEvidences = reportedApplicableForSource(evidences, CKB_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isExperimental(evidence)) {
                result.add(toMolecularEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    public Set<EvidenceEntry> createOtherResponsiveEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();

        List<ProtectEvidence> ckbEvidences = reportedApplicableForSource(evidences, CKB_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isOther(evidence)) {
                result.add(toMolecularEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    public Set<EvidenceEntry> createResistanceEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();

        List<ProtectEvidence> reportedCkbEvidences = reportedApplicableForSource(evidences, CKB_SOURCE);
        for (ProtectEvidence evidence : reportedCkbEvidences) {
            boolean hasEqualOrWorseResponsive = hasEqualOrWorseResponsive(reportedCkbEvidences, evidence.treatment(), evidence.level());

            if (evidence.direction().isResistant() && !evidence.direction().isPredicted() && hasEqualOrWorseResponsive) {
                result.add(toMolecularEvidence(evidence));
            }
        }

        return result;
    }

    private static boolean hasEqualOrWorseResponsive(@NotNull Iterable<ProtectEvidence> evidences, @NotNull String treatment,
            @NotNull EvidenceLevel resistanceLevel) {
        for (ProtectEvidence evidence : evidences) {
            boolean isRelevant = isApproved(evidence) || isExperimental(evidence) || isOther(evidence);

            if (evidence.direction().isResponsive() && evidence.treatment().equals(treatment) && isRelevant
                    && resistanceLevel.isBetterOrEqual(evidence.level())) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private static List<ProtectEvidence> reportedApplicableForSource(@NotNull Iterable<ProtectEvidence> evidences, @NotNull String source) {
        List<ProtectEvidence> filtered = Lists.newArrayList();
        for (ProtectEvidence evidence : evidences) {
            if (evidence.reported() && evidence.sources().contains(source) && ApplicabilityFilter.isPotentiallyApplicable(evidence)) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }

    private static boolean isApproved(@NotNull ProtectEvidence evidence) {
        return evidence.onLabel() && evidence.level() == EvidenceLevel.A && !evidence.direction().isPredicted();
    }

    private static boolean isExperimental(@NotNull ProtectEvidence evidence) {
        boolean isOffLabelA = !evidence.onLabel() && evidence.level() == EvidenceLevel.A;
        boolean isOnLabelB = evidence.onLabel() && evidence.level() == EvidenceLevel.B && !evidence.direction().isPredicted();

        return isOffLabelA || isOnLabelB;
    }

    private static boolean isOther(@NotNull ProtectEvidence evidence) {
        if (isApproved(evidence) || isExperimental(evidence)) {
            return false;
        }

        return evidence.level() != EvidenceLevel.D && evidence.level() != EvidenceLevel.C && !evidence.direction().isPredicted();
    }

    @NotNull
    private static EvidenceEntry toMolecularEvidence(@NotNull ProtectEvidence evidence) {
        return ImmutableEvidenceEntry.builder().event(OrangeUtil.toEvent(evidence)).treatment(evidence.treatment()).build();
    }
}
