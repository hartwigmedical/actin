package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsMicrosatelliteUnstable implements EvaluationFunction {

    static final Set<String> MSI_GENES = Sets.newHashSet();

    static {
        MSI_GENES.add("MLH1");
        MSI_GENES.add("MSH2");
        MSI_GENES.add("MSH6");
        MSI_GENES.add("PMS2");
        MSI_GENES.add("EPCAM");
    }

    IsMicrosatelliteUnstable() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean isMicrosatelliteUnstable = record.molecular().characteristics().isMicrosatelliteUnstable();

        if (isMicrosatelliteUnstable == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Unknown microsatellite stability status")
                    .addFailGeneralMessages("Unknown MS status")
                    .build();
        }

        EvaluationResult result = isMicrosatelliteUnstable ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor is microsatellite stable");
            builder.addFailGeneralMessages("Molecular requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor is microsatellite unstable");
            builder.addPassGeneralMessages("Molecular requirements");
        }

        return builder.build();
    }
}
