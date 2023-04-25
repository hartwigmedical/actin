package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Optional;
import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasLeftSidedColorectalTumor implements EvaluationFunction {

    private static final Set<String> LEFT_SUB_LOCATIONS =
            Set.of("rectum", "descending colon", "colon sigmoid", "colon descendens", "rectosigmoid");
    private static final Set<String> RIGHT_SUB_LOCATIONS = Set.of("ascending colon",
            "colon ascendens",
            "caecum",
            "cecum",
            "transverse colon",
            "colon transversum",
            "flexura hepatica",
            "hepatic flexure");
    @NotNull
    private final DoidModel doidModel;

    HasLeftSidedColorectalTumor(@NotNull DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> tumorDoids = record.clinical().tumor().doids();
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Unable to identify tumor type", "Tumor type");
        } else if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.COLORECTAL_CANCER_DOID)) {
            return EvaluationFactory.fail("Tumor is not colorectal cancer", "Tumor type");
        } else {
            return Optional.ofNullable(record.clinical().tumor().primaryTumorSubLocation())
                    .filter(subLocation -> !subLocation.isEmpty())
                    .map(String::toLowerCase)
                    .map(subLocation -> {
                        if (LEFT_SUB_LOCATIONS.stream().anyMatch(subLocation::contains)) {
                            return EvaluationFactory.pass(String.format("Tumor sub-location %s is on left side", subLocation),
                                    "Left-sided CRC tumor");
                        } else if (RIGHT_SUB_LOCATIONS.stream().anyMatch(subLocation::contains)) {
                            return EvaluationFactory.fail(String.format("Tumor sub-location %s is on right side", subLocation),
                                    "Right-sided CRC tumor");
                        } else {
                            return EvaluationFactory.undetermined(
                                    "Unknown tumor sub-location " + subLocation + ", left-sidedness is unknown",
                                    "Unknown sidedness of tumor");
                        }
                    })
                    .orElse(EvaluationFactory.undetermined("Tumor sub-location not provided, left-sidedness is unknown",
                            "Unknown sidedness of tumor"));
        }
    }
}
