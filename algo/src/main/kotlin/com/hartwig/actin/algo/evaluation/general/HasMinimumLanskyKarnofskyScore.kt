package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasMinimumLanskyKarnofskyScore implements EvaluationFunction {

    @NotNull
    private final PerformanceScore performanceScore;
    private final int minScore;

    HasMinimumLanskyKarnofskyScore(@NotNull final PerformanceScore performanceScore, final int minScore) {
        this.performanceScore = performanceScore;
        this.minScore = minScore;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer who = record.clinical().clinicalStatus().who();
        if (who == null) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Cannot evaluate " + performanceScore.display() + " performance score because WHO is missing")
                    .addUndeterminedGeneralMessages("Missing " + performanceScore.display() + " score")
                    .build();
        }

        int passScore = toMinScoreForWHO(who);
        int undeterminedScore = toMaxScoreForWHO(who);
        int warnScore = toMaxScoreForWHO(Math.max(0, who - 1));

        if (passScore >= minScore) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(performanceScore.display() + " score based on WHO score is at least " + minScore)
                    .addPassGeneralMessages("Minimum " + performanceScore.display() + " requirements")
                    .build();
        } else if (undeterminedScore >= minScore) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Not clear whether " + performanceScore.display() + " score based on WHO score is at least " + minScore)
                    .addUndeterminedSpecificMessages("Undetermined minimum " + performanceScore.display() + " requirements")
                    .build();
        } else if (warnScore >= minScore) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(performanceScore.display() + " score based on WHO score exceeds requested score of " + minScore)
                    .addWarnSpecificMessages("Minimum " + performanceScore.display() + " requirements")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(performanceScore.display() + " score based on WHO score is below " + minScore)
                .addFailGeneralMessages("Minimum " + performanceScore.display() + " requirements")
                .build();
    }

    private static int toMinScoreForWHO(int who) {
        switch (who) {
            case 0:
                return 100;
            case 1:
                return 80;
            case 2:
                return 60;
            case 3:
                return 40;
            case 4:
                return 10;
            case 5:
                return 0;
            default:
                throw new IllegalStateException("Illegal who status: " + who);
        }
    }

    private static int toMaxScoreForWHO(int who) {
        switch (who) {
            case 0:
                return 100;
            case 1:
                return 90;
            case 2:
                return 70;
            case 3:
                return 50;
            case 4:
                return 30;
            case 5:
                return 0;
            default:
                throw new IllegalStateException("Illegal who status: " + who);
        }
    }
}
