package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.molecular.MolecularInterpretation;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.jetbrains.annotations.NotNull;

public class HasCancerWithNeuroendocrineComponent implements EvaluationFunction {

    static final Set<String> NEUROENDOCRINE_DOIDS = Sets.newHashSet();
    static final Set<String> NEUROENDOCRINE_TERMS = Sets.newHashSet();
    static final Set<String> NEUROENDOCRINE_EXTRA_DETAILS = Sets.newHashSet();

    static {
        NEUROENDOCRINE_DOIDS.add(DoidConstants.NEUROENDOCRINE_TUMOR_DOID);
        NEUROENDOCRINE_DOIDS.add(DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID);

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
        Set<String> tumorDoids = record.clinical().tumor().doids();
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) && record.clinical().tumor().primaryTumorExtraDetails() == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether tumor of patient may have a neuroendocrine component")
                    .addUndeterminedGeneralMessages("Neuroendocrine component")
                    .build();
        }

        boolean hasNeuroendocrineDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, NEUROENDOCRINE_DOIDS);
        boolean hasNeuroendocrineTerm = DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, tumorDoids, NEUROENDOCRINE_TERMS);

        boolean hasNeuroendocrineDetails =
                TumorTypeEvaluationFunctions.hasTumorWithDetails(record.clinical().tumor(), NEUROENDOCRINE_EXTRA_DETAILS);

        if (hasNeuroendocrineDoid || hasNeuroendocrineTerm || hasNeuroendocrineDetails) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has cancer with neuroendocrine component")
                    .addPassGeneralMessages("Neuroendocrine component")
                    .build();
        }

        boolean hasSmallCellDoid =
                DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, HasCancerWithSmallCellComponent.SMALL_CELL_DOIDS);

        boolean hasSmallCellDoidTerm =
                DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, tumorDoids, HasCancerWithSmallCellComponent.SMALL_CELL_TERMS);

        boolean hasSmallCellDetails = TumorTypeEvaluationFunctions.hasTumorWithDetails(record.clinical().tumor(),
                HasCancerWithSmallCellComponent.SMALL_CELL_EXTRA_DETAILS);

        if (hasSmallCellDoid || hasSmallCellDoidTerm || hasSmallCellDetails) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has cancer with small cell component, "
                            + "undetermined if neuroendocrine component could be present as well")
                    .addUndeterminedGeneralMessages("Neuroendocrine component")
                    .build();
        }

        if (hasNeuroendocrineMolecularProfile(record.molecular())) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has cancer with neuroendocrine molecular profile")
                    .addUndeterminedGeneralMessages("Neuroendocrine component")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have cancer with neuroendocrine component")
                .addFailGeneralMessages("Neuroendocrine component")
                .build();
    }

    private static boolean hasNeuroendocrineMolecularProfile(@NotNull MolecularRecord molecular) {
        int inactivationCount = 0;
        if (MolecularInterpretation.hasGeneInactivated(molecular, "TP53")) {
            inactivationCount++;
        }

        if (MolecularInterpretation.hasGeneInactivated(molecular, "PTEN")) {
            inactivationCount++;
        }

        if (MolecularInterpretation.hasGeneInactivated(molecular, "RB1")) {
            inactivationCount++;
        }

        return inactivationCount >= 2;
    }
}
