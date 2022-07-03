package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.sort.PriorTumorTreatmentDescendingDateComparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SystemicTreatmentAnalyser {

    private SystemicTreatmentAnalyser() {
    }

    public static int maxSystemicTreatments(@NotNull List<PriorTumorTreatment> treatments) {
        int systemicCount = 0;
        for (PriorTumorTreatment treatment : treatments) {
            if (treatment.isSystemic()) {
                systemicCount++;
            }
        }
        return systemicCount;
    }

    public static int minSystemicTreatments(@NotNull List<PriorTumorTreatment> treatments) {
        Map<String, List<PriorTumorTreatment>> systemicByName = Maps.newHashMap();

        for (PriorTumorTreatment treatment : treatments) {
            if (treatment.isSystemic()) {
                List<PriorTumorTreatment> systemic = systemicByName.get(treatment.name());
                if (systemic == null) {
                    systemic = Lists.newArrayList();
                }
                systemic.add(treatment);
                systemicByName.put(treatment.name(), systemic);
            }
        }

        int systemicCount = 0;
        for (List<PriorTumorTreatment> systemic : systemicByName.values()) {
            systemicCount++;
            if (systemic.size() > 1) {
                systemic.sort(new PriorTumorTreatmentDescendingDateComparator());
                for (int i = 1; i < systemic.size(); i++) {
                    if (isInterrupted(systemic.get(i), systemic.get(i-1), treatments)) {
                        systemicCount++;
                    }
                }
            }
        }

        return systemicCount;
    }

    @Nullable
    public static String stopReasonOnLastSystemicTreatment(@NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        PriorTumorTreatment last = null;
        for (PriorTumorTreatment treatment : priorTumorTreatments) {
            if (treatment.isSystemic()) {
                if (last == null || (last.startYear() == null && treatment.startYear() != null) || isAfter(treatment, last)) {
                    last = treatment;
                }
            }
        }

        return last != null ? last.stopReason() : null;
    }

    private static boolean isInterrupted(@NotNull PriorTumorTreatment mostRecent, @NotNull PriorTumorTreatment leastRecent,
            @NotNull List<PriorTumorTreatment> treatments) {
        // Treatments with ambiguous timeline are never considered interrupted.
        if (!isAfter(mostRecent, leastRecent)) {
            return false;
        }

        for (PriorTumorTreatment treatment : treatments) {
            if (!treatment.name().equals(mostRecent.name()) && isAfter(treatment, leastRecent) && isBefore(treatment, mostRecent)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isBefore(@NotNull PriorTumorTreatment first, @NotNull PriorTumorTreatment second) {
        if (isLower(first.startYear(), second.startYear())) {
            return true;
        } else {
            return isEqual(first.startYear(), second.startYear()) && isLower(first.startMonth(), second.startMonth());
        }
    }

    private static boolean isAfter(@NotNull PriorTumorTreatment first, @NotNull PriorTumorTreatment second) {
        if (isHigher(first.startYear(), second.startYear())) {
            return true;
        } else {
            return isEqual(first.startYear(), second.startYear()) && isHigher(first.startMonth(), second.startMonth());
        }
    }

    private static boolean isHigher(@Nullable Integer int1, @Nullable Integer int2) {
        if (int1 == null || int2 == null) {
            return false;
        }

        return int1 > int2;
    }

    private static boolean isLower(@Nullable Integer int1, @Nullable Integer int2) {
        if (int1 == null || int2 == null) {
            return false;
        }

        return int1 < int2;
    }

    private static boolean isEqual(@Nullable Integer int1, @Nullable Integer int2) {
        if (int1 == null || int2 == null) {
            return false;
        }

        return int1.equals(int2);
    }
}
