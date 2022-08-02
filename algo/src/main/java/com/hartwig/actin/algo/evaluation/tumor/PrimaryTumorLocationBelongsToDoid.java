package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.config.AdenoSquamousMapping;

import org.jetbrains.annotations.NotNull;

public class PrimaryTumorLocationBelongsToDoid implements EvaluationFunction {

    private static final String UNCLEAR_TUMOR_TYPE = "carcinoma";

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToMatch;
    private final boolean requireExclusive;
    private final boolean requireExact;

    public PrimaryTumorLocationBelongsToDoid(@NotNull final DoidModel doidModel, @NotNull final String doidToMatch,
            final boolean requireExclusive, final boolean requireExact) {
        this.doidModel = doidModel;
        this.doidToMatch = doidToMatch;
        this.requireExclusive = requireExclusive;
        this.requireExact = requireExact;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        String doidTerm = doidModel.resolveTermForDoid(doidToMatch);
        Set<String> doids = record.clinical().tumor().doids();

        if (doids == null || doids.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No tumor type known for patient")
                    .addUndeterminedGeneralMessages("Unknown tumor type")
                    .build();
        }

        if (isMatch(doids, doidToMatch)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has " + doidTerm)
                    .addPassGeneralMessages("Tumor type")
                    .build();
        }

        if (isPotentialAdenoSquamousMatch(doids, doidToMatch)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Unclear whether patient may have " + doidTerm + " due to adeno-squamous nature of tumor")
                    .addUndeterminedGeneralMessages("Tumor type")
                    .build();
        }

        if (isPotentialMatchWithMainCancerType(record.clinical().tumor(), doids, doidToMatch)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine if patient may have " + doidTerm)
                    .addUndeterminedGeneralMessages("Tumor type")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no " + doidTerm)
                .addFailGeneralMessages("Tumor type")
                .build();
    }

    private boolean isMatch(@NotNull Set<String> doids, @NotNull String doidToMatch) {
        int numMatches = 0;
        int numMismatches = 0;
        for (String doid : doids) {
            boolean isMatch = requireExact ? doid.equals(doidToMatch) : doidModel.expandedDoidWithParents(doid).contains(doidToMatch);
            if (isMatch) {
                numMatches++;
            } else {
                numMismatches++;
            }
        }

        return requireExclusive ? numMatches > 0 && numMismatches == 0 : numMatches > 0;
    }

    private boolean isPotentialAdenoSquamousMatch(@NotNull Set<String> patientDoids, @NotNull String doidToMatch) {
        Set<String> doidTreeToMatch = doidModel.expandedDoidWithParents(doidToMatch);

        Set<String> patientDoidTree = expandToFullDoidTree(patientDoids);
        for (String doidEntryToMatch : doidTreeToMatch) {
            for (AdenoSquamousMapping mapping : doidModel.adenoSquamousMappingsForDoid(doidEntryToMatch)) {
                if (patientDoidTree.contains(mapping.adenoSquamousDoid())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPotentialMatchWithMainCancerType(@NotNull TumorDetails tumor, @NotNull Set<String> doids,
            @NotNull String doidToMatch) {
        String primaryTumorType = tumor.primaryTumorType();
        String primaryTumorSubType = tumor.primaryTumorSubType();

        if (primaryTumorType != null && primaryTumorSubType != null && !(
                (primaryTumorType.equalsIgnoreCase(UNCLEAR_TUMOR_TYPE) || primaryTumorType.isEmpty()) && primaryTumorSubType.isEmpty())) {
            return false;
        }

        Set<String> mainCancerTypesToMatch = doidModel.mainCancerDoids(doidToMatch);
        for (String doid : doids) {
            for (String entry : doidModel.expandedDoidWithParents(doid)) {
                if (mainCancerTypesToMatch.contains(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    private Set<String> expandToFullDoidTree(@NotNull Set<String> doids) {
        Set<String> doidTree = Sets.newHashSet();
        for (String doid : doids) {
            doidTree.addAll(doidModel.expandedDoidWithParents(doid));
        }
        return doidTree;
    }
}
