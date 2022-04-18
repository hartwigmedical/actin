package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableEvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.filter.ApplicabilityFilter;
import com.hartwig.actin.molecular.orange.util.EvidenceFormatter;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

class OrangeEvidenceFactory {

    private static final Logger LOGGER = LogManager.getLogger(OrangeEvidenceFactory.class);

    static final String ACTIN_SOURCE = "ACTIN";
    static final String EXTERNAL_SOURCE = "ICLUSION";
    static final String EVIDENCE_SOURCE = "CKB";

    static final String ACTIN_SOURCE_NAME = "Erasmus MC";
    static final String EXTERNAL_SOURCE_NAME = "iClusion";
    static final String EVIDENCE_SOURCE_NAME = "CKB";

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
    public MolecularEvidence create(@NotNull ProtectRecord protect) {
        Set<ProtectEvidence> evidences = protect.evidences();

        return ImmutableMolecularEvidence.builder()
                .actinSource(ACTIN_SOURCE_NAME)
                .actinTrials(createActinTrials(evidences))
                .externalTrialSource(EXTERNAL_SOURCE_NAME)
                .externalTrials(createExternalTrials(evidences))
                .evidenceSource(EVIDENCE_SOURCE_NAME)
                .approvedEvidence(createApprovedEvidence(evidences))
                .onLabelExperimentalEvidence(createOnLabelExperimentalEvidence(evidences))
                .offLabelExperimentalEvidence(createOffLabelExperimentalEvidence(evidences))
                .preClinicalEvidence(createPreClinicalEvidence(evidences))
                .knownResistanceEvidence(createKnownResistanceEvidence(evidences))
                .suspectResistanceEvidence(createSuspectResistanceEvidence(evidences))
                .build();
    }

    @NotNull
    private Set<EvidenceEntry> createActinTrials(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, ACTIN_SOURCE)) {
            if (evidenceEvaluator.isPotentiallyForTrialInclusion(evidence)) {
                result.add(toEvidenceEntry(evidence));
            } else {
                String evidenceString = EvidenceFormatter.format(evidence);
                LOGGER.debug("Filtered evidence from ACTIN trials because it is not used as inclusion criteria: {}", evidenceString);
            }
        }
        return result;
    }

    @NotNull
    private Set<EvidenceEntry> createExternalTrials(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, EXTERNAL_SOURCE)) {
            result.add(toEvidenceEntry(evidence));
        }
        return result;
    }

    @NotNull
    private Set<EvidenceEntry> createApprovedEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, EVIDENCE_SOURCE)) {
            if (evidence.direction().isResponsive() && isApproved(evidence)) {
                result.add(toEvidenceEntry(evidence));
            }
        }
        return result;
    }

    @NotNull
    private Set<EvidenceEntry> createOnLabelExperimentalEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();

        Set<ProtectEvidence> ckbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isOnLabelExperimental(evidence)) {
                result.add(toEvidenceEntry(evidence));
            }
        }
        return result;
    }

    @NotNull
    private Set<EvidenceEntry> createOffLabelExperimentalEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();

        Set<ProtectEvidence> ckbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isOffLabelExperimental(evidence)) {
                result.add(toEvidenceEntry(evidence));
            }
        }
        return result;
    }

    @NotNull
    private Set<EvidenceEntry> createPreClinicalEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();

        Set<ProtectEvidence> ckbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isPreClinical(evidence)) {
                result.add(toEvidenceEntry(evidence));
            }
        }
        return result;
    }

    @NotNull
    private Set<EvidenceEntry> createKnownResistanceEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();

        Set<ProtectEvidence> reportedCkbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : reportedCkbEvidences) {
            if (evidence.direction().isResistant()) {
                boolean hasEqualOrWorseResponsive =
                        hasEqualOrWorseResponsive(reportedCkbEvidences, evidence.treatment(), evidence.level(), true);
                boolean hasValidEvidenceLevel =
                        (evidence.level() == EvidenceLevel.A || evidence.level() == EvidenceLevel.B) && !evidence.direction().isPredicted();

                if (hasEqualOrWorseResponsive && hasValidEvidenceLevel) {
                    result.add(toEvidenceEntry(evidence));
                }
            }
        }

        return result;
    }

    @NotNull
    private Set<EvidenceEntry> createSuspectResistanceEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<EvidenceEntry> result = Sets.newHashSet();

        Set<EvidenceEntry> known = createKnownResistanceEvidence(evidences);

        Set<ProtectEvidence> reportedCkbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : reportedCkbEvidences) {
            if (evidence.direction().isResistant()) {
                if (hasEqualOrWorseResponsive(reportedCkbEvidences, evidence.treatment(), evidence.level(), false)) {
                    EvidenceEntry entry = toEvidenceEntry(evidence);
                    if (!known.contains(entry)) {
                        result.add(entry);
                    }
                }
            }
        }

        return result;
    }

    private static boolean hasEqualOrWorseResponsive(@NotNull Iterable<ProtectEvidence> evidences, @NotNull String treatment,
            @NotNull EvidenceLevel resistanceLevel, boolean filterPreClinical) {
        for (ProtectEvidence evidence : evidences) {
            boolean isResponsive = evidence.direction().isResponsive();
            boolean isValidResponsiveLevel = !filterPreClinical || !isPreClinical(evidence);
            boolean isEvidenceForSameTreatment = evidence.treatment().equals(treatment);
            boolean hasBetterOrEqualResistanceLevel = resistanceLevel.isBetterOrEqual(evidence.level());

            if (isResponsive && isValidResponsiveLevel && isEvidenceForSameTreatment && hasBetterOrEqualResistanceLevel) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private static Set<ProtectEvidence> reportedApplicableForSource(@NotNull Iterable<ProtectEvidence> evidences,
            @NotNull String sourceName) {
        Set<ProtectEvidence> filtered = Sets.newHashSet();
        for (ProtectEvidence evidence : evidences) {
            ProtectSource applicableSource = null;
            for (ProtectSource source : evidence.sources()) {
                if (source.name().equals(sourceName)) {
                    applicableSource = source;
                }
            }

            if (evidence.reported() && applicableSource != null && ApplicabilityFilter.isPotentiallyApplicable(evidence)) {
                filtered.add(ImmutableProtectEvidence.builder().from(evidence).sources(Sets.newHashSet(applicableSource)).build());
            }
        }
        return filtered;
    }

    private static boolean isApproved(@NotNull ProtectEvidence evidence) {
        return evidence.onLabel() && evidence.level() == EvidenceLevel.A && !evidence.direction().isPredicted();
    }

    private static boolean isOnLabelExperimental(@NotNull ProtectEvidence evidence) {
        boolean isOffLabelA = evidence.level() == EvidenceLevel.A && (!evidence.onLabel() || evidence.direction().isPredicted());
        boolean isOnLabelB = evidence.onLabel() && evidence.level() == EvidenceLevel.B && !evidence.direction().isPredicted();

        return isOffLabelA || isOnLabelB;
    }

    private static boolean isOffLabelExperimental(@NotNull ProtectEvidence evidence) {
        return evidence.level() == EvidenceLevel.B && !evidence.direction().isPredicted() && !evidence.onLabel();
    }

    private static boolean isPreClinical(@NotNull ProtectEvidence evidence) {
        return !(isApproved(evidence) || isOnLabelExperimental(evidence) || isOffLabelExperimental(evidence));
    }

    @NotNull
    private static EvidenceEntry toEvidenceEntry(@NotNull ProtectEvidence evidence) {
        return ImmutableEvidenceEntry.builder().event(OrangeUtil.toEvent(evidence)).treatment(evidence.treatment()).build();
    }
}
