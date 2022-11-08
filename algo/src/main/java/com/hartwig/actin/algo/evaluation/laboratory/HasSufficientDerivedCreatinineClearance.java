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


    //TODO: Implement logics for method = "measured"
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

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Cockcroft-Gault is insufficient");
            builder.addFailGeneralMessages("Cockcroft-Gault insufficient");
        } else if (result == EvaluationResult.UNDETERMINED) {
            if (weight == null) {
                builder.addUndeterminedSpecificMessages("Cockcroft-Gault is likely insufficient, but weight of patient is not known");
                builder.addUndeterminedGeneralMessages("Cockcroft-Gault evaluation weight unknown");
            } else {
                builder.addUndeterminedSpecificMessages("Cockcroft-Gault evaluation led to ambiguous results");
                builder.addUndeterminedGeneralMessages("Cockcroft-Gault evaluation ambiguous");
            }
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Cockcroft-Gault is sufficient");
            builder.addPassGeneralMessages("Cockcroft-Gault sufficient");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Cockcroft-Gault is likely sufficient, but body weight of patient is not known");
            builder.addWarnGeneralMessages("Cockcroft-Gault evaluation weight unknown");
        }
        return builder.build();
    }

    @NotNull
    private Evaluation evaluateValues(@NotNull String code, @NotNull List<Double> values, @NotNull String comparator) {
        Set<EvaluationResult> evaluations = Sets.newHashSet();
        for (Double value : values) {
            evaluations.add(ValueComparison.evaluateVersusMinValue(value, comparator, minCreatinineClearance));
        }

        EvaluationResult result = CreatinineFunctions.interpretEGFREvaluations(evaluations);

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(code + " is insufficient");
            builder.addFailGeneralMessages(code + " insufficient");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(code + " evaluation led to ambiguous results");
            builder.addUndeterminedGeneralMessages(code + " undetermined");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(code + " is sufficient");
            builder.addPassGeneralMessages(code + " sufficient");
        }

        return builder.build();
    }
}
