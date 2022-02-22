package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;
import com.hartwig.actin.clinical.sort.VitalFunctionDescendingDateComparator;

import org.jetbrains.annotations.NotNull;

public class HasSufficientBloodPressure implements EvaluationFunction {

    private static final int MAX_BLOOD_PRESSURES_TO_USE = 5;

    @NotNull
    private final BloodPressureCategory category;
    private final double minAvgBloodPressure;

    HasSufficientBloodPressure(@NotNull final BloodPressureCategory category, final double minAvgBloodPressure) {
        this.category = category;
        this.minAvgBloodPressure = minAvgBloodPressure;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<VitalFunction> relevant = selectRelevant(record.clinical().vitalFunctions());

        if (relevant.isEmpty()) {
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        }

        double sum = 0;
        for (VitalFunction vitalFunction : relevant) {
            sum += vitalFunction.value();
        }

        double avg = sum / relevant.size();

        EvaluationResult result = Double.compare(avg, minAvgBloodPressure) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
        return EvaluationFactory.create(result);
    }

    @NotNull
    private List<VitalFunction> selectRelevant(@NotNull List<VitalFunction> vitalFunctions) {
        List<VitalFunction> filtered = Lists.newArrayList();
        for (VitalFunction vitalFunction : vitalFunctions) {
            if (isBloodPressure(vitalFunction) && vitalFunction.subcategory().equals(category.display())) {
                filtered.add(vitalFunction);
            }
        }

        filtered.sort(new VitalFunctionDescendingDateComparator());

        return filtered.subList(0, Math.min(filtered.size(), MAX_BLOOD_PRESSURES_TO_USE));
    }

    private static boolean isBloodPressure(@NotNull VitalFunction vitalFunction) {
        return vitalFunction.category() == VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
                || vitalFunction.category() == VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE;
    }
}