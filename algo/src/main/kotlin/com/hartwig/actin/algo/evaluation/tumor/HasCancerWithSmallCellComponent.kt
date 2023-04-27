package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasCancerWithSmallCellComponent implements EvaluationFunction {

    static final Set<String> SMALL_CELL_DOIDS = Sets.newHashSet();
    static final Set<String> SMALL_CELL_TERMS = Sets.newHashSet();
    static final Set<String> SMALL_CELL_EXTRA_DETAILS = Sets.newHashSet();

    static {
        SMALL_CELL_DOIDS.add(DoidConstants.SMALL_CELL_CARCINOMA_DOID);

        SMALL_CELL_TERMS.add("small cell");

        SMALL_CELL_EXTRA_DETAILS.add("small cell");
        SMALL_CELL_EXTRA_DETAILS.add("SCNEC");
    }

    @NotNull
    private final DoidModel doidModel;

    HasCancerWithSmallCellComponent(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> tumorDoids = record.clinical().tumor().doids();
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) && record.clinical().tumor().primaryTumorExtraDetails() == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether tumor of patient may have a small component")
                    .addUndeterminedGeneralMessages("Undetermined small cell component")
                    .build();
        }

        boolean hasSmallCellDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, SMALL_CELL_DOIDS);
        boolean hasSmallCellTerm = DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, tumorDoids, SMALL_CELL_TERMS);
        boolean hasSmallCellDetails = TumorTypeEvaluationFunctions.hasTumorWithDetails(record.clinical().tumor(), SMALL_CELL_EXTRA_DETAILS);

        if (hasSmallCellDoid || hasSmallCellTerm || hasSmallCellDetails) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has cancer with small cell component")
                    .addPassGeneralMessages("Presence of small cell component")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have cancer with small cell component")
                .addFailGeneralMessages("No small cell component")
                .build();
    }
}
