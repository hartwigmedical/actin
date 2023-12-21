package com.hartwig.actin.clinical.interpretation

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator
import java.time.LocalDate

class LabInterpretation private constructor(private val measurements: Map<LabMeasurement, List<LabValue>>) {
    fun mostRecentRelevantDate(): LocalDate? {
        var mostRecentDate: LocalDate? = null
        for (measurement in LabMeasurement.values()) {
            val allValues = allValues(measurement)
            if (allValues != null && !allValues.isEmpty()) {
                val mostRecent = allValues[0]
                if (mostRecentDate == null || mostRecent.date().isAfter(mostRecentDate)) {
                    mostRecentDate = mostRecent.date()
                }
            }
        }
        return mostRecentDate
    }

    fun allDates(): Set<LocalDate> {
        val dates: MutableSet<LocalDate> = Sets.newTreeSet(Comparator.reverseOrder())
        for ((_, value) in measurements) {
            for (lab in value) {
                dates.add(lab.date())
            }
        }
        return dates
    }

    fun allValues(measurement: LabMeasurement): List<LabValue>? {
        val values = measurements[measurement]
        return if (values == null || values.isEmpty()) {
            null
        } else values
    }

    fun mostRecentValue(measurement: LabMeasurement): LabValue? {
        val values = measurements[measurement]
        return if (values != null && !values.isEmpty()) values[0] else null
    }

    fun secondMostRecentValue(measurement: LabMeasurement): LabValue? {
        val values = measurements[measurement]
        return if (values != null && values.size >= 2) values[1] else null
    }

    fun valuesOnDate(measurement: LabMeasurement, dateToFind: LocalDate): List<LabValue>? {
        if (!measurements.containsKey(measurement)) {
            return Lists.newArrayList()
        }
        val filtered: MutableList<LabValue> = Lists.newArrayList()
        for (lab in measurements[measurement]!!) {
            if (lab.date() == dateToFind) {
                filtered.add(lab)
            }
        }
        return filtered
    }

    companion object {
        @JvmStatic
        fun fromMeasurements(measurements: Multimap<LabMeasurement, LabValue>): LabInterpretation {
            val sortedMap: MutableMap<LabMeasurement, List<LabValue>> = Maps.newHashMap()
            for (measurement in measurements.keySet()) {
                val values: List<LabValue> = Lists.newArrayList(measurements[measurement])
                values.sort(LabValueDescendingDateComparator())
                sortedMap[measurement] = values
            }
            return LabInterpretation(sortedMap)
        }
    }
}
