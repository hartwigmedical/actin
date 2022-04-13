package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class PrimaryTumorLocationBelongsToDoid implements EvaluationFunction {

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
        Set<String> doids = record.clinical().tumor().doids();

        if (doids == null || doids.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No tumor type known for patient")
                    .addUndeterminedGeneralMessages("Unknown tumor type")
                    .build();
        }

        EvaluationResult result = isDoidMatch(doids, doidToMatch) ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no " + doidModel.term(doidToMatch));
            builder.addFailGeneralMessages("Tumor type");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has " + doidModel.term(doidToMatch));
            builder.addPassGeneralMessages("Tumor type");
        }

        return builder.build();
    }

    private boolean isDoidMatch(@NotNull Set<String> doids, @NotNull String doidToMatch) {
        int numMatches = 0;
        int numMismatches = 0;
        for (String doid : doids) {
            boolean isMatch = requireExact ? doid.equals(doidToMatch) : doidModel.doidWithParents(doid).contains(doidToMatch);
            if (isMatch) {
                numMatches++;
            } else {
                numMismatches++;
            }
        }

        return requireExclusive ? numMatches > 0 && numMismatches == 0 : numMatches > 0;
    }
}
