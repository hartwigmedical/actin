package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasCancerWithNeuroendocrineComponent implements EvaluationFunction {

    static final Set<String> NEUROENDOCRINE_DOIDS = Sets.newHashSet();
    static final Set<String> NEUROENDOCRINE_TERMS = Sets.newHashSet();
    static final Set<String> NEUROENDOCRINE_EXTRA_DETAILS = Sets.newHashSet();

    static {
        NEUROENDOCRINE_DOIDS.add("169"); // neuroendocrine tumor
        NEUROENDOCRINE_DOIDS.add("1800"); // neuroendocrine carcinoma

        NEUROENDOCRINE_TERMS.add("neuroendocrine");

        NEUROENDOCRINE_EXTRA_DETAILS.add("neuroendocrine");
        NEUROENDOCRINE_EXTRA_DETAILS.add("NEC");
        NEUROENDOCRINE_EXTRA_DETAILS.add("NET");
    }

    @NotNull
    private final DoidModel doidModel;

    HasCancerWithNeuroendocrineComponent(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasNeuroendocrineComponent = DoidEvaluationFunctions.hasTumorOfCertainType(doidModel,
                record.clinical().tumor(),
                NEUROENDOCRINE_DOIDS,
                NEUROENDOCRINE_TERMS,
                NEUROENDOCRINE_EXTRA_DETAILS);

        if (hasNeuroendocrineComponent) {
            return EvaluationFactory.unrecoverable().result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has cancer with neuroendocrine component")
                    .addPassGeneralMessages("Neuroendocrine component")
                    .build();
        }

        boolean hasSmallCellComponent = DoidEvaluationFunctions.hasTumorOfCertainType(doidModel,
                record.clinical().tumor(),
                HasCancerWithSmallCellComponent.SMALL_CELL_DOIDS,
                HasCancerWithSmallCellComponent.SMALL_CELL_TERMS,
                HasCancerWithSmallCellComponent.SMALL_CELL_EXTRA_DETAILS);

        if (hasSmallCellComponent) {
            return EvaluationFactory.unrecoverable().result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has small cell component so may have neuroendocrine cancer")
                    .addUndeterminedGeneralMessages("Neuroendocrine component")
                    .build();
        }

        return EvaluationFactory.unrecoverable().result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have cancer with neuroendocrine component")
                .addFailGeneralMessages("Neuroendocrine component")
                .build();
    }
}
