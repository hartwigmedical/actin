package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasLeptomeningealDisease implements EvaluationFunction {

    private static final String LEPTOMENINGEAL_DISEASE_CATEGORY_PATTERN = "leptomeningeal disease";
    private static final Set<List<String>> LESION_WARNING_PATTERNS = Sets.newHashSet();

    static {
        LESION_WARNING_PATTERNS.add(Lists.newArrayList("leptomeningeal"));
        LESION_WARNING_PATTERNS.add(Lists.newArrayList("carcinomatous", "meningitis"));
    }

    HasLeptomeningealDisease() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> leptomeningealComplications = Sets.newHashSet();
        if (record.clinical().complications() != null) {
            for (Complication complication : record.clinical().complications()) {
                if (isPotentialLeptomeningealDisease(complication)) {
                    leptomeningealComplications.add(complication.name());
                }
            }
        }

        if (!leptomeningealComplications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has complication " + Format.concat(leptomeningealComplications))
                    .addPassGeneralMessages(Format.concat(leptomeningealComplications))
                    .build();
        }

        Boolean hasCnsLesions = record.clinical().tumor().hasCnsLesions();
        List<String> otherLesions = record.clinical().tumor().otherLesions();
        Set<String> potentialMeningealLesions = Sets.newHashSet();
        if (hasCnsLesions != null && otherLesions != null && hasCnsLesions) {
            for (String otherLesion : otherLesions) {
                if (isPotentialLeptomeningealLesion(otherLesion)) {
                    potentialMeningealLesions.add(otherLesion);
                }
            }
        }

        if (!potentialMeningealLesions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(
                            "Patient has lesions suggesting leptomeningeal disease: " + Format.concat(potentialMeningealLesions))
                    .addWarnGeneralMessages("Potential leptomeningeal disease")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no leptomeningeal disease")
                .addFailGeneralMessages("No leptomeningeal disease")
                .build();
    }

    private static boolean isPotentialLeptomeningealDisease(@NotNull Complication complication) {
       for (String category : complication.categories()) {
           if (category.toLowerCase().contains(LEPTOMENINGEAL_DISEASE_CATEGORY_PATTERN.toLowerCase())) {
               return true;
           }
       }
       return false;
    }

    private static boolean isPotentialLeptomeningealLesion(@NotNull String lesion) {
        return PatternMatcher.isMatch(lesion, LESION_WARNING_PATTERNS);
    }
}
