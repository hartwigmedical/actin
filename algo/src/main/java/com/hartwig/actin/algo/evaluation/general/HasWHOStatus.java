package com.hartwig.actin.algo.evaluation.general;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.complication.PatternMatcher;
import com.hartwig.actin.algo.evaluation.util.Format;
import org.jetbrains.annotations.NotNull;

public class HasWHOStatus implements EvaluationFunction {

    public static final List<String> COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS = Arrays.asList(
            "Ascites", "Pleural effusion", "Pericardial effusion", "Pain", "Spinal cord compression"
    );

    private final int requiredWHO;

    HasWHOStatus(final int requiredWHO) {
        this.requiredWHO = requiredWHO;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer who = record.clinical().clinicalStatus().who();

        if (who == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("WHO status is missing")
                    .addUndeterminedGeneralMessages("WHO status missing")
                    .build();
        }

        Set<String> warningComplicationCategories = PatternMatcher.findComplicationCategoriesMatchingCategories(record,
                COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);

        if (who == requiredWHO && !warningComplicationCategories.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient WHO status " + who + " matches requested but patient " +
                            "has complication categories of concern: " + Format.concat(warningComplicationCategories))
                    .addWarnGeneralMessages("Adequate WHO status but complication categories of concern")
                    .build();
        } else if (who == requiredWHO) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient WHO status " + who + " is requested WHO (WHO " + requiredWHO + ")")
                    .addPassGeneralMessages("Adequate WHO status")
                    .build();
        } else if (Math.abs(who - requiredWHO) == 1) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient WHO status " + who + " is close t requested WHO (WHO " + requiredWHO + ")")
                    .addWarnGeneralMessages("Inadequate WHO status")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient WHO status " + who + " is not requested WHO (WHO " + requiredWHO + ")")
                    .addFailGeneralMessages("Inadequate WHO status")
                    .build();
        }
    }
}
