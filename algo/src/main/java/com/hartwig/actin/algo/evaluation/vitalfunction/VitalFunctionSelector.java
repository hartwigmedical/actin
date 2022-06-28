package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;
import com.hartwig.actin.clinical.sort.VitalFunctionDescendingDateComparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class VitalFunctionSelector {

    private static final int MAX_BLOOD_PRESSURES_TO_USE = 5;
    private static final int MAX_AGE_MONTHS = 1;

    private VitalFunctionSelector() {
    }

    @NotNull
    public static List<VitalFunction> select(@NotNull List<VitalFunction> vitalFunctions, @NotNull VitalFunctionCategory categoryToFind,
            @Nullable String unitToFind, int maxEntries) {
        List<VitalFunction> result = Lists.newArrayList();
        for (VitalFunction vitalFunction : vitalFunctions) {
            if (vitalFunction.category() == categoryToFind && (unitToFind == null || vitalFunction.unit().equalsIgnoreCase(unitToFind))) {
                result.add(vitalFunction);
            }
        }

        result.sort(new VitalFunctionDescendingDateComparator());

        result = result.subList(0, Math.min(result.size(), maxEntries));

        return result;
    }

    @NotNull
    public static List<VitalFunction> selectBloodPressures(@NotNull List<VitalFunction> vitalFunctions,
            @NotNull BloodPressureCategory category) {
        Multimap<LocalDate, VitalFunction> vitalFunctionsPerDate = ArrayListMultimap.create();
        for (VitalFunction vitalFunction : vitalFunctions) {
            boolean isBloodPressure = isBloodPressure(vitalFunction);
            boolean isCategoryMatch = vitalFunction.subcategory().equalsIgnoreCase(category.display());
            if (isBloodPressure && isCategoryMatch) {
                vitalFunctionsPerDate.put(vitalFunction.date(), vitalFunction);
            }
        }

        List<VitalFunction> result = extractMedians(vitalFunctionsPerDate);
        result.sort(new VitalFunctionDescendingDateComparator());

        result = result.subList(0, Math.min(result.size(), MAX_BLOOD_PRESSURES_TO_USE));

        if (!result.isEmpty()) {
            LocalDate mostRecent = result.get(0).date();
            result.removeIf(function -> function.date().isBefore(mostRecent.minusMonths(MAX_AGE_MONTHS)));
        }

        return result;
    }

    @NotNull
    private static List<VitalFunction> extractMedians(@NotNull Multimap<LocalDate, VitalFunction> vitalFunctionsPerDate) {
        List<VitalFunction> mediansPerDay = Lists.newArrayList();
        for (LocalDate date : vitalFunctionsPerDate.keySet()) {
            mediansPerDay.add(VitalFunctionFunctions.selectMedianFunction(vitalFunctionsPerDate.get(date)));
        }
        return mediansPerDay;
    }

    private static boolean isBloodPressure(@NotNull VitalFunction vitalFunction) {
        return vitalFunction.category() == VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
                || vitalFunction.category() == VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE;
    }
}
