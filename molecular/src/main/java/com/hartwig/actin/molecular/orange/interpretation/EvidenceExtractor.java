package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;
import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapper;
import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.filter.ApplicabilityFilter;
import com.hartwig.actin.molecular.sort.evidence.ActinTrialEvidenceComparator;
import com.hartwig.actin.molecular.sort.evidence.ExternalTrialEvidenceComparator;
import com.hartwig.actin.molecular.sort.evidence.TreatmentEvidenceComparator;

import org.jetbrains.annotations.NotNull;

class EvidenceExtractor {

    static final String ACTIN_SOURCE_NAME = "Erasmus MC";
    static final String EXTERNAL_SOURCE_NAME = "iClusion";
    static final String EVIDENCE_SOURCE_NAME = "CKB";

    @NotNull
    private final ExternalTrialMapper externalTrialMapper;

    @NotNull
    public static EvidenceExtractor extract(@NotNull List<ExternalTrialMapping> mappings) {
        return new EvidenceExtractor(new ExternalTrialMapper(mappings));
    }

    @VisibleForTesting
    EvidenceExtractor(@NotNull final ExternalTrialMapper externalTrialMapper) {
        this.externalTrialMapper = externalTrialMapper;
    }

    @NotNull
    public MolecularEvidence extract(@NotNull OrangeRecord record) {
        return ImmutableMolecularEvidence.builder()
                .actinSource(ACTIN_SOURCE_NAME)
                .actinTrials(createActinTrials(record.protect().reportableTrials()))
                .externalTrialSource(EXTERNAL_SOURCE_NAME)
                .externalTrials(createExternalTrials(record.protect().reportableTrials()))
                .evidenceSource(EVIDENCE_SOURCE_NAME)
                .approvedEvidence(createApprovedEvidence(record.protect().reportableEvidences()))
                .onLabelExperimentalEvidence(createOnLabelExperimentalEvidence(record.protect().reportableEvidences()))
                .offLabelExperimentalEvidence(createOffLabelExperimentalEvidence(record.protect().reportableEvidences()))
                .preClinicalEvidence(createReportedPreClinicalEvidence(record.protect().reportableEvidences()))
                .knownResistanceEvidence(createKnownResistanceEvidence(record.protect().reportableEvidences()))
                .suspectResistanceEvidence(createSuspectResistanceEvidence(record.protect().reportableEvidences()))
                .build();
    }

    @NotNull
    private static Set<ActinTrialEvidence> createActinTrials(@NotNull Iterable<ProtectEvidence> reportableTrials) {
        Set<ActinTrialEvidence> result = Sets.newTreeSet(new ActinTrialEvidenceComparator());
        for (ProtectEvidence evidence : allForSource(reportableTrials, EvidenceConstants.ACTIN_SOURCE)) {
            result.add(ActinTrialEvidenceFactory.create(evidence));
        }
        return result;
    }

    @NotNull
    private Set<ExternalTrialEvidence> createExternalTrials(@NotNull Iterable<ProtectEvidence> reportableTrials) {
        Set<ExternalTrialEvidence> result = Sets.newTreeSet(new ExternalTrialEvidenceComparator());
        for (ProtectEvidence evidence : applicableForSource(reportableTrials, EvidenceConstants.EXTERNAL_SOURCE)) {
            result.add(mapExternalToActinTreatment(toExternalTrialEvidence(evidence)));
        }
        return result;
    }

    @NotNull
    private ExternalTrialEvidence mapExternalToActinTreatment(@NotNull ExternalTrialEvidence evidence) {
        return ImmutableExternalTrialEvidence.builder().from(evidence).trial(externalTrialMapper.map(evidence.trial())).build();
    }

    @NotNull
    private static Set<TreatmentEvidence> createApprovedEvidence(@NotNull Iterable<ProtectEvidence> reportableEvidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());
        for (ProtectEvidence evidence : applicableForSource(reportableEvidences, EvidenceConstants.EVIDENCE_SOURCE)) {
            if (evidence.direction().isResponsive() && isApproved(evidence)) {
                result.add(toTreatmentEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createOnLabelExperimentalEvidence(@NotNull Iterable<ProtectEvidence> reportableEvidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());

        Set<ProtectEvidence> ckbEvidences = applicableForSource(reportableEvidences, EvidenceConstants.EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isOnLabelExperimental(evidence)) {
                result.add(toTreatmentEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createOffLabelExperimentalEvidence(@NotNull Iterable<ProtectEvidence> reportableEvidences) {
        Set<TreatmentEvidence> result = Sets.newHashSet();

        Set<ProtectEvidence> ckbEvidences = applicableForSource(reportableEvidences, EvidenceConstants.EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isOffLabelExperimental(evidence)) {
                result.add(toTreatmentEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createReportedPreClinicalEvidence(@NotNull Iterable<ProtectEvidence> reportableEvidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());

        Set<ProtectEvidence> ckbEvidences = applicableForSource(reportableEvidences, EvidenceConstants.EVIDENCE_SOURCE);
        for (ProtectEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isPreClinical(evidence)) {
                result.add(toTreatmentEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createKnownResistanceEvidence(@NotNull Iterable<ProtectEvidence> reportableEvidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());

        Set<ProtectEvidence> reportedCkbEvidences = applicableForSource(reportableEvidences, EvidenceConstants.EVIDENCE_SOURCE);
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
    private static Set<TreatmentEvidence> createSuspectResistanceEvidence(@NotNull Iterable<ProtectEvidence> reportableEvidences) {
        Set<TreatmentEvidence> result = Sets.newTreeSet(new TreatmentEvidenceComparator());

        Set<TreatmentEvidence> known = createKnownResistanceEvidence(reportableEvidences);

        Set<ProtectEvidence> reportedCkbEvidences = applicableForSource(reportableEvidences, EvidenceConstants.EVIDENCE_SOURCE);
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
    private static Set<ProtectEvidence> allForSource(@NotNull Iterable<ProtectEvidence> evidences, @NotNull String sourceName) {
        Set<ProtectEvidence> filtered = Sets.newHashSet();
        for (ProtectEvidence evidence : evidences) {
            boolean hasApplicableSource = false;
            for (ProtectSource source : evidence.sources()) {
                if (source.name().equals(sourceName)) {
                    hasApplicableSource = true;
                }
            }

            if (hasApplicableSource) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }

    @NotNull
    private static Set<ProtectEvidence> applicableForSource(@NotNull Iterable<ProtectEvidence> evidences, @NotNull String sourceName) {
        Set<ProtectEvidence> filtered = Sets.newHashSet();
        for (ProtectEvidence evidence : allForSource(evidences, sourceName)) {
            if (ApplicabilityFilter.isPotentiallyApplicable(evidence)) {
                filtered.add(evidence);
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
    private static ExternalTrialEvidence toExternalTrialEvidence(@NotNull ProtectEvidence evidence) {
        return ImmutableExternalTrialEvidence.builder()
                .event(EvidenceEventExtraction.extract(evidence))
                .trial(evidence.treatment())
                .build();
    }

    @NotNull
    private static TreatmentEvidence toTreatmentEvidence(@NotNull ProtectEvidence evidence) {
        return ImmutableTreatmentEvidence.builder()
                .event(EvidenceEventExtraction.extract(evidence))
                .treatment(evidence.treatment())
                .build();
    }
}
