package com.hartwig.actin.clinical.interpretation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LabInterpretation {

    @NotNull
    private final Map<LabMeasurement, List<LabValue>> measurements;

    @NotNull
    static LabInterpretation fromMeasurements(@NotNull Multimap<LabMeasurement, LabValue> measurements) {
        Map<LabMeasurement, List<LabValue>> sortedMap = Maps.newHashMap();
        for (LabMeasurement measurement : measurements.keySet()) {
            List<LabValue> values = Lists.newArrayList(measurements.get(measurement));
            values.sort(new LabValueDescendingDateComparator());
            sortedMap.put(measurement, values);
        }

        return new LabInterpretation(sortedMap);
    }

    private LabInterpretation(@NotNull final Map<LabMeasurement, List<LabValue>> measurements) {
        this.measurements = measurements;
    }

    @Nullable
    public LocalDate mostRecentRelevantDate() {
        LocalDate mostRecentDate = null;

        for (LabMeasurement measurement : LabMeasurement.values()) {
            List<LabValue> allValues = allValues(measurement);
            if (allValues != null && !allValues.isEmpty()) {
                LabValue mostRecent = allValues.get(0);
                if (mostRecentDate == null || mostRecent.date().isAfter(mostRecentDate)) {
                    mostRecentDate = mostRecent.date();
                }
            }
        }

        return mostRecentDate;
    }

    @NotNull
    public Set<LocalDate> allDates() {
        Set<LocalDate> dates = Sets.newTreeSet(Comparator.reverseOrder());

        for (Map.Entry<LabMeasurement, List<LabValue>> entry : measurements.entrySet()) {
            for (LabValue lab : entry.getValue()) {
                dates.add(lab.date());
            }
        }

        return dates;
    }

    @Nullable
    public List<LabValue> allValues(@NotNull LabMeasurement measurement) {
        List<LabValue> values = measurements.get(measurement);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values;
    }

    @Nullable
    public LabValue mostRecentValue(@NotNull LabMeasurement measurement) {
        List<LabValue> values = measurements.get(measurement);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    @Nullable
    public LabValue secondMostRecentValue(@NotNull LabMeasurement measurement) {
        List<LabValue> values = measurements.get(measurement);
        return values != null && values.size() >= 2 ? values.get(1) : null;
    }

    @Nullable
    public List<LabValue> valuesOnDate(@NotNull LabMeasurement measurement, @NotNull LocalDate dateToFind) {
        if (!measurements.containsKey(measurement)) {
            return Lists.newArrayList();
        }

        List<LabValue> filtered = Lists.newArrayList();
        for (LabValue lab : measurements.get(measurement)) {
            if (lab.date().equals(dateToFind)) {
                filtered.add(lab);
            }
        }
        return filtered;
    }
}
