package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;
import com.hartwig.actin.molecular.orange.curation.ExternalTreatmentMapper;
import com.hartwig.actin.molecular.orange.curation.ExternalTreatmentMapping;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.filter.ApplicabilityFilter;
import com.hartwig.actin.molecular.sort.evidence.ActinTrialEvidenceComparator;
import com.hartwig.actin.molecular.sort.evidence.TreatmentEvidenceComparator;

import org.jetbrains.annotations.NotNull;

class EvidenceExtractor {

    static final String ACTIN_SOURCE = "ACTIN";
    static final String EXTERNAL_SOURCE = "ICLUSION";
    static final String EVIDENCE_SOURCE = "CKB";

    static final String ACTIN_SOURCE_NAME = "Erasmus MC";
    static final String EXTERNAL_SOURCE_NAME = "iClusion";
    static final String EVIDENCE_SOURCE_NAME = "CKB";

    @NotNull
    private final ExternalTreatmentMapper externalTreatmentMapper;

    @NotNull
    public static EvidenceExtractor extract(@NotNull List<ExternalTreatmentMapping> mappings) {
        return new EvidenceExtractor(new ExternalTreatmentMapper(mappings));
    }

    @VisibleForTesting
    EvidenceExtractor(@NotNull final ExternalTreatmentMapper externalTreatmentMapper) {
        this.externalTreatmentMapper = externalTreatmentMapper;
    }

    @NotNull
    public MolecularEvidence extract(@NotNull OrangeRecord record) {
        Set<ProtectEvidence> evidences = record.protect().evidences();

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
    private static Set<ActinTrialEvidence> createActinTrials(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<ActinTrialEvidence> result = Sets.newTreeSet(new ActinTrialEvidenceComparator());
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, ACTIN_SOURCE)) {
            result.add(ActinTrialEvidenceFactory.create(evidence));
        }
        return result;
    }

    @NotNull
    private Set<TreatmentEvidence> createExternalTrials(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, EXTERNAL_SOURCE)) {
            result.add(mapExternalToActinTreatment(toTreatmentEvidence(evidence)));
        }
        return result;
    }

    @NotNull
    private TreatmentEvidence mapExternalToActinTreatment(@NotNull TreatmentEvidence entry) {
        return ImmutableTreatmentEvidence.builder().from(entry).treatment(externalTreatmentMapper.map(entry.treatment())).build();
    }

    @NotNull
    private static Set<TreatmentEvidence> createApprovedEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());
        for (ProtectEvidence evidence : reportedApplicableForSource(evidences, EVIDENCE_SOURCE)) {
            if (evidence.direction().isResponsive() && isApproved(evidence)) {
                result.add(toTreatmentEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createOnLabelExperimentalEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());

        Set<ProtectEvidence> ckbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isOnLabelExperimental(evidence)) {
                result.add(toTreatmentEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createOffLabelExperimentalEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<TreatmentEvidence> result = Sets.newHashSet();

        Set<ProtectEvidence> ckbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isOffLabelExperimental(evidence)) {
                result.add(toTreatmentEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createPreClinicalEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());

        Set<ProtectEvidence> ckbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isPreClinical(evidence)) {
                result.add(toTreatmentEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createKnownResistanceEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());

        Set<ProtectEvidence> reportedCkbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : reportedCkbEvidences) {
            if (evidence.direction().isResistant()) {
                boolean hasEqualOrWorseResponsive =
                        hasEqualOrWorseResponsive(reportedCkbEvidences, evidence.treatment(), evidence.level(), true);
                boolean hasValidEvidenceLevel =
                        (evidence.level() == EvidenceLevel.A || evidence.level() == EvidenceLevel.B) && !evidence.direction().isPredicted();

                if (hasEqualOrWorseResponsive && hasValidEvidenceLevel) {
                    result.add(toTreatmentEvidence(evidence));
                }
            }
        }

        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createSuspectResistanceEvidence(@NotNull Iterable<ProtectEvidence> evidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());

        Set<TreatmentEvidence> known = createKnownResistanceEvidence(evidences);

        Set<ProtectEvidence> reportedCkbEvidences = reportedApplicableForSource(evidences, EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : reportedCkbEvidences) {
            if (evidence.direction().isResistant()) {
                if (hasEqualOrWorseResponsive(reportedCkbEvidences, evidence.treatment(), evidence.level(), false)) {
                    TreatmentEvidence entry = toTreatmentEvidence(evidence);
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
    private static TreatmentEvidence toTreatmentEvidence(@NotNull ProtectEvidence evidence) {
        return ImmutableTreatmentEvidence.builder().event(EvidenceEventExtraction.extract(evidence)).treatment(evidence.treatment()).build();
    }
}
