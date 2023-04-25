package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.config.AdenoSquamousMapping;

import org.jetbrains.annotations.NotNull;

public class PrimaryTumorLocationBelongsToDoid implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToMatch;

    public PrimaryTumorLocationBelongsToDoid(@NotNull final DoidModel doidModel, @NotNull final String doidToMatch) {
        this.doidModel = doidModel;
        this.doidToMatch = doidToMatch;
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

        if (DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch)) {
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
                    .addWarnGeneralMessages("Unclear if tumor type is considered " + doidTerm)
                    .build();
        }

        if (isUndeterminateUnderMainCancerType(tumorDoids, doidToMatch)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine based on configured tumor type if patient may have " + doidTerm)
                    .addUndeterminedGeneralMessages("Undetermined if " + doidTerm)
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

    private boolean isUndeterminateUnderMainCancerType(@NotNull Set<String> tumorDoids, @NotNull String doidToMatch) {
        Set<String> fullDoidToMatchTree = doidModel.doidWithParents(doidToMatch);
        Set<String> mainCancerTypesToMatch = doidModel.mainCancerDoids(doidToMatch);
        for (String tumorDoid : tumorDoids) {
            Set<String> fullTumorDoidTree = doidModel.doidWithParents(tumorDoid);
            for (String doid : fullTumorDoidTree) {
                if (mainCancerTypesToMatch.contains(doid) && fullDoidToMatchTree.contains(tumorDoid)
                        && !fullTumorDoidTree.contains(doidToMatch)) {
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
