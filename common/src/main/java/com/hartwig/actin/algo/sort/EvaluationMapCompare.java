package com.hartwig.actin.algo.sort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.sort.EligibilityComparator;

import org.jetbrains.annotations.NotNull;

final class EvaluationMapCompare {

    private static final Comparator<Eligibility> ELIGIBILITY_COMPARATOR = new EligibilityComparator();

    private EvaluationMapCompare() {
    }

    public static int compare(@NotNull Map<Eligibility, Evaluation> map1, @NotNull Map<Eligibility, Evaluation> map2) {
        List<Eligibility> keys1 = Lists.newArrayList(map1.keySet());
        List<Eligibility> keys2 = Lists.newArrayList(map2.keySet());
        int sizeCompare = keys1.size() - keys2.size();
        if (sizeCompare != 0) {
            return sizeCompare > 0 ? 1 : -1;
        }

        int index = 0;
        while (index < keys1.size()) {
            Eligibility key1 = keys1.get(index);
            Eligibility key2 = keys2.get(index);
            int eligibilityCompare = ELIGIBILITY_COMPARATOR.compare(key1, key2);
            if (eligibilityCompare != 0) {
                return eligibilityCompare;
            }

            int evaluationCompare = map1.get(key1).result().compareTo(map2.get(key2).result());
            if (evaluationCompare != 0) {
                return evaluationCompare;
            }

            index++;
        }

        return 0;
    }
}
