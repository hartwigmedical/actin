package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants;
import com.hartwig.hmftools.datamodel.cuppa.CuppaData;
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction;
import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption;
import com.hartwig.hmftools.datamodel.linx.LinxBreakend;
import com.hartwig.hmftools.datamodel.linx.LinxSvAnnotation;
import com.hartwig.hmftools.datamodel.orange.OrangeRecord;
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion;
import com.hartwig.hmftools.datamodel.purple.PurpleQCStatus;

import org.jetbrains.annotations.NotNull;

public class OrangeInterpreter {

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public OrangeInterpreter(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceDatabase evidenceDatabase) {
        this.geneFilter = geneFilter;
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public MolecularRecord interpret(@NotNull OrangeRecord record) {
        validateOrangeRecord(record);
        DriverExtractor driverExtractor = DriverExtractor.create(geneFilter, evidenceDatabase);
        CharacteristicsExtractor characteristicsExtractor = new CharacteristicsExtractor(evidenceDatabase);

        return ImmutableMolecularRecord.builder()
                .patientId(toPatientId(record.sampleId()))
                .sampleId(record.sampleId())
                .type(determineExperimentType(record.experimentType()))
                .refGenomeVersion(determineRefGenomeVersion(record.refGenomeVersion()))
                .date(record.experimentDate())
                .evidenceSource(ActionabilityConstants.EVIDENCE_SOURCE.display())
                .externalTrialSource(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display())
                .containsTumorCells(record.purple().fit().containsTumorCells())
                .hasSufficientQualityAndPurity(hasSufficientQualityAndPurity(record))
                .hasSufficientQuality(hasSufficientQuality(record))
                .characteristics(characteristicsExtractor.extract(record))
                .drivers(driverExtractor.extract(record))
                .immunology(ImmunologyExtraction.extract(record))
                .pharmaco(PharmacoExtraction.extract(record))
                .build();
    }

    @NotNull
    static RefGenomeVersion determineRefGenomeVersion(@NotNull OrangeRefGenomeVersion refGenomeVersion) {
        switch (refGenomeVersion) {
            case V37: {
                return RefGenomeVersion.V37;
            }
            case V38: {
                return RefGenomeVersion.V38;
            }
        }

        throw new IllegalStateException("Could not determine ref genome version from: " + refGenomeVersion);
    }

    static boolean hasSufficientQualityAndPurity(@NotNull OrangeRecord record) {
        return recordQCStatusesInSet(record, Set.of(PurpleQCStatus.PASS));
    }

    static boolean hasSufficientQuality(@NotNull OrangeRecord record) {
        return recordQCStatusesInSet(record, Set.of(PurpleQCStatus.PASS, PurpleQCStatus.WARN_LOW_PURITY));
    }

    private static boolean recordQCStatusesInSet(OrangeRecord record, Set<PurpleQCStatus> allowableQCStatuses) {
        return allowableQCStatuses.containsAll(record.purple().fit().qc().status());
    }

    @VisibleForTesting
    @NotNull
    static String toPatientId(@NotNull String sampleId) {
        if (sampleId.length() < 12) {
            throw new IllegalArgumentException("Cannot convert sampleId to patientId: " + sampleId);
        }
        return sampleId.substring(0, 12);
    }

    @NotNull
    static ExperimentType determineExperimentType(com.hartwig.hmftools.datamodel.orange.ExperimentType experimentType) {
        switch (experimentType) {
            case TARGETED: {
                return ExperimentType.TARGETED;
            }
            case WHOLE_GENOME: {
                return ExperimentType.WHOLE_GENOME;
            }
        }

        throw new IllegalStateException("Could not determine experiment type from: " + experimentType);
    }

    private static void validateOrangeRecord(@NotNull OrangeRecord orange) {
        throwIfGermlineFieldNonEmpty(orange);
        throwIfAnyCuppaPredictionClassifierMissing(orange);
        throwIfPurpleQCMissing(orange);
    }

    private static void throwIfGermlineFieldNonEmpty(@NotNull OrangeRecord orange) {
        String message = "must be null or empty because ACTIN only accepts ORANGE output that has been "
                + "scrubbed of germline data. Please use the JSON output from the 'orange_no_germline' directory.";

        List<LinxSvAnnotation> allGermlineStructuralVariants = orange.linx().allGermlineStructuralVariants();
        if (allGermlineStructuralVariants != null) {
            throw new IllegalStateException("allGermlineStructuralVariants " + message);
        }

        List<LinxBreakend> allGermlineBreakends = orange.linx().allGermlineBreakends();
        if (allGermlineBreakends != null) {
            throw new IllegalStateException("allGermlineBreakends " + message);
        }

        List<HomozygousDisruption> germlineHomozygousDisruptions = orange.linx().germlineHomozygousDisruptions();
        if (germlineHomozygousDisruptions != null) {
            throw new IllegalStateException("germlineHomozygousDisruptions " + message);
        }
    }

    private static void throwIfAnyCuppaPredictionClassifierMissing(@NotNull OrangeRecord orange) {
        CuppaData cuppaData = orange.cuppa();
        if (cuppaData != null) {
            for (CuppaPrediction prediction : cuppaData.predictions()) {
                throwIfCuppaPredictionClassifierMissing(prediction);
            }
        }
    }

    private static void throwIfCuppaPredictionClassifierMissing(@NotNull CuppaPrediction prediction) {
        String message = "Missing field %s: cuppa not run in expected configuration";

        if (prediction.snvPairwiseClassifier() == null) {
            throw new IllegalStateException(String.format(message, "snvPairwiseClassifer"));
        }

        if (prediction.genomicPositionClassifier() == null) {
            throw new IllegalStateException(String.format(message, "genomicPositionClassifier"));
        }

        if (prediction.featureClassifier() == null) {
            throw new IllegalStateException(String.format(message, "featureClassifier"));
        }
    }

    private static void throwIfPurpleQCMissing(@NotNull OrangeRecord orange) {
        if (orange.purple().fit().qc().status().isEmpty()) {
            throw new IllegalStateException("Cannot interpret purple record with empty QC states");
        }
    }
}
