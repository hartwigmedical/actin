package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.util.ValueComparison;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class HasSufficientDerivedCreatinineClearance implements LabEvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasSufficientDerivedCreatinineClearance.class);

    private final int referenceYear;
    @NotNull
    private final CreatinineClearanceMethod method;
    private final double minCreatinineClearance;

    HasSufficientDerivedCreatinineClearance(final int referenceYear, @NotNull final CreatinineClearanceMethod method,
            final double minCreatinineClearance) {
        this.referenceYear = referenceYear;
        this.method = method;
        this.minCreatinineClearance = minCreatinineClearance;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        switch (method) {
            case EGFR_MDRD:
                return evaluateMDRD(record, labValue);
            case EGFR_CKD_EPI:
                return evaluateCKDEPI(record, labValue);
            case COCKCROFT_GAULT:
                return evaluateCockcroftGault(record, labValue);
            default: {
                LOGGER.warn("No creatinine clearance function implemented for '{}'", method);
                return EvaluationFactory.recoverable().result(EvaluationResult.NOT_IMPLEMENTED).build();
            }
        }
    }

    @NotNull
    private Evaluation evaluateMDRD(@NotNull PatientRecord record, @NotNull LabValue creatinine) {
        List<Double> mdrdValues = CreatinineFunctions.calcMDRD(record.clinical().patient().birthYear(),
                referenceYear,
                record.clinical().patient().gender(),
                creatinine);

        return evaluateValues("MDRD", mdrdValues, creatinine.comparator());
    }

    @NotNull
    private Evaluation evaluateCKDEPI(@NotNull PatientRecord record, @NotNull LabValue creatinine) {
        List<Double> ckdepiValues = CreatinineFunctions.calcCKDEPI(record.clinical().patient().birthYear(),
                referenceYear,
                record.clinical().patient().gender(),
                creatinine);

        return evaluateValues("CKDEPI", ckdepiValues, creatinine.comparator());
    }

    @NotNull
    private Evaluation evaluateCockcroftGault(@NotNull PatientRecord record, @NotNull LabValue creatinine) {
        Double weight = CreatinineFunctions.determineWeight(record.clinical().bodyWeights());
        double cockcroftGault = CreatinineFunctions.calcCockcroftGault(record.clinical().patient().birthYear(),
                referenceYear,
                record.clinical().patient().gender(),
                weight,
                creatinine);

        EvaluationResult result = ValueComparison.evaluateVersusMinValue(cockcroftGault, creatinine.comparator(), minCreatinineClearance);

        if (weight == null) {
            if (result == EvaluationResult.FAIL) {
                result = EvaluationResult.UNDETERMINED;
            } else if (result == EvaluationResult.PASS) {
                result = EvaluationResult.WARN;
            }
        }

        return toEvaluation(result, "Cockcroft-Gault");
    }

    @NotNull
    private Evaluation evaluateValues(@NotNull String code, @NotNull List<Double> values, @NotNull String comparator) {
        Set<EvaluationResult> evaluations = Sets.newHashSet();
        for (Double value : values) {
            evaluations.add(ValueComparison.evaluateVersusMinValue(value, comparator, minCreatinineClearance));
        }

        return toEvaluation(CreatinineFunctions.interpretEGFREvaluations(evaluations), code);
    }

    //TODO: Improve messaging
    @NotNull
    private static Evaluation toEvaluation(@NotNull EvaluationResult result, @NotNull String code) {
        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(code + " is insufficient");
            builder.addFailGeneralMessages(code + " insufficient");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(code + " evaluation led to ambiguous results");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(code + " sufficient");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages(code + " sufficient");
        }
        return builder.build();
    }
}
