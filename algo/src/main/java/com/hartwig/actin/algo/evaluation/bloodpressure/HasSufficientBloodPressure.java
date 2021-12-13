package com.hartwig.actin.algo.evaluation.bloodpressure;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.BloodPressure;
import com.hartwig.actin.clinical.sort.BloodPressureDescendingDateComparator;

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
        List<BloodPressure> relevant = selectRelevant(record.clinical().bloodPressures());

        if (relevant.isEmpty()) {
            return Evaluation.UNDETERMINED;
        }

        double sum = 0;
        for (BloodPressure bloodPressure : relevant) {
            sum += bloodPressure.value();
        }

        double avg = sum / relevant.size();

        return Double.compare(avg, minAvgBloodPressure) >= 0 ? Evaluation.PASS : Evaluation.FAIL;
    }

    @NotNull
    private List<BloodPressure> selectRelevant(@NotNull List<BloodPressure> bloodPressures) {
        List<BloodPressure> filtered = Lists.newArrayList();
        for (BloodPressure bloodPressure : bloodPressures) {
            if (bloodPressure.category().equals(category.display())) {
                filtered.add(bloodPressure);
            }
        }

        filtered.sort(new BloodPressureDescendingDateComparator());

        return filtered.subList(0, Math.min(filtered.size(), MAX_BLOOD_PRESSURES_TO_USE));
    }
}
