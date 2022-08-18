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
        Set<String> tumorDoids = record.clinical().tumor().doids();

        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Tumor type of patient is not configured")
                    .addUndeterminedGeneralMessages("Unknown tumor type")
                    .build();
        }

        boolean hasExactMatch = false;
        for (String doid : tumorDoids) {
            if (doid.equals(doidToMatch)) {
                hasExactMatch = true;
                break;
            }
        }

        boolean isPass;
        if (requireExclusive) {
            boolean isExclusiveMatch = DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, tumorDoids, doidToMatch);
            isPass = requireExact ? isExclusiveMatch && hasExactMatch : isExclusiveMatch;
        } else {
            boolean isMatch = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch);
            isPass = requireExact ? isMatch && hasExactMatch : isMatch;
        }

        if (isPass) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has " + doidTerm)
                    .addPassGeneralMessages("Tumor type")
                    .build();
        }

        if (isPotentialAdenoSquamousMatch(tumorDoids, doidToMatch)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Unclear whether tumor type of patient can be considered " + doidTerm
                            + ", because patient has adenosquamous tumor type")
                    .addWarnGeneralMessages("Tumor type")
                    .build();
        }

        if (isPotentialMatchWithMainCancerType(record.clinical().tumor(), tumorDoids, doidToMatch)) {
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

    private boolean isPotentialAdenoSquamousMatch(@NotNull Set<String> patientDoids, @NotNull String doidToMatch) {
        Set<String> doidTreeToMatch = doidModel.doidWithParents(doidToMatch);

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

    private boolean isPotentialMatchWithMainCancerType(@NotNull TumorDetails tumor, @NotNull Set<String> tumorDoids,
            @NotNull String doidToMatch) {
        String primaryTumorType = tumor.primaryTumorType();
        String primaryTumorSubType = tumor.primaryTumorSubType();

        if (primaryTumorType != null && primaryTumorSubType != null && !(
                (primaryTumorType.equalsIgnoreCase(UNCLEAR_TUMOR_TYPE) || primaryTumorType.isEmpty()) && primaryTumorSubType.isEmpty())) {
            return false;
        }

        Set<String> mainCancerTypesToMatch = doidModel.mainCancerDoids(doidToMatch);
        for (String doid : tumorDoids) {
            for (String entry : doidModel.doidWithParents(doid)) {
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
            doidTree.addAll(doidModel.doidWithParents(doid));
        }
        return doidTree;
    }
}
