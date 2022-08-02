package com.hartwig.actin.algo.evaluation.othercondition;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.DateComparison;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasHadPriorConditionWithDoidRecently implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToFind;
    @NotNull
    private final LocalDate minDate;

    HasHadPriorConditionWithDoidRecently(@NotNull final DoidModel doidModel, @NotNull final String doidToFind,
            @NotNull final LocalDate minDate) {
        this.doidModel = doidModel;
        this.doidToFind = doidToFind;
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        String doidTerm = doidModel.resolveTermForDoid(doidToFind);

        String matchingConditionAfterMinDate = null;
        boolean matchingConditionIsWithinWarnDate = false;
        boolean hasHadPriorConditionWithUnclearDate = false;

        for (PriorOtherCondition condition : OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            if (conditionHasDoid(condition, doidToFind)) {
                Boolean isAfterMinDate = DateComparison.isAfterDate(minDate, condition.year(), condition.month());
                if (isAfterMinDate == null) {
                    hasHadPriorConditionWithUnclearDate = true;
                } else if (isAfterMinDate) {
                    matchingConditionAfterMinDate = condition.name();
                    Boolean isBeforeWarnDate = DateComparison.isBeforeDate(minDate.plusMonths(2), condition.year(), condition.month());
                    matchingConditionIsWithinWarnDate = isBeforeWarnDate != null && isBeforeWarnDate;
                }
            }
        }

        if (matchingConditionAfterMinDate != null) {
            if (matchingConditionIsWithinWarnDate) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addWarnSpecificMessages("Patient has had recent " + matchingConditionAfterMinDate + " belonging to " + doidTerm)
                        .addWarnGeneralMessages("Recent " + doidTerm)
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has had recent " + matchingConditionAfterMinDate + " belonging to " + doidTerm)
                        .addPassGeneralMessages("Recent " + doidTerm)
                        .build();
            }
        }

        if (hasHadPriorConditionWithUnclearDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has had " + matchingConditionAfterMinDate + " belonging to " + doidTerm
                            + " but unclear whether that was recent")
                    .addUndeterminedGeneralMessages("Recent " + doidTerm)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has had no recent condition belonging to " + doidTerm)
                .addFailGeneralMessages("No relevant non-oncological condition")
                .build();
    }

    private boolean conditionHasDoid(@NotNull PriorOtherCondition condition, @NotNull String doidToFind) {
        for (String doid : condition.doids()) {
            if (doidModel.doidWithParents(doid).contains(doidToFind)) {
                return true;
            }
        }
        return false;
    }
}
