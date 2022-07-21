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

public class HasCancerWithSmallCellComponent implements EvaluationFunction {

    static final Set<String> SMALL_CELL_DOIDS = Sets.newHashSet();
    static final Set<String> SMALL_CELL_TERMS = Sets.newHashSet();
    static final Set<String> SMALL_CELL_EXTRA_DETAILS = Sets.newHashSet();

    static {
        SMALL_CELL_DOIDS.add("0050685"); // small cell carcinoma

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
        if (record.clinical().tumor().doids() == null && record.clinical().tumor().primaryTumorExtraDetails() == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has small component")
                    .addUndeterminedGeneralMessages("Small cell component")
                    .build();
        }

        boolean hasSmallCellDoid =
                DoidEvaluationFunctions.hasDoidOfCertainType(doidModel, record.clinical().tumor(), SMALL_CELL_DOIDS, SMALL_CELL_TERMS);

        boolean hasSmallCellDetails = TumorTypeEvaluationFunctions.hasTumorWithDetails(record.clinical().tumor(), SMALL_CELL_EXTRA_DETAILS);

        if (hasSmallCellDoid || hasSmallCellDetails) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has cancer with small cell component")
                    .addPassGeneralMessages("Small cell component")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have cancer with small cell component")
                .addFailGeneralMessages("Small cell component")
                .build();
    }
}
