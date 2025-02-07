package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class LabInterpretation(private val measurements: Map<LabMeasurement, List<LabValue>>) {
    
    fun mostRecentRelevantDate(): LocalDate? {
        return measurements.mapNotNull { (_, values) -> values.firstOrNull()?.date }.maxWithOrNull(LocalDate::compareTo)
    }

    fun allDates(): Set<LocalDate> {
        return measurements.values.flatMap { it.map(LabValue::date) }.toSortedSet(Comparator.reverseOrder())
    }

    fun allValues(measurement: LabMeasurement): List<LabValue>? {
        return measurements[measurement]?.ifEmpty { null }
    }

    fun mostRecentValue(measurement: LabMeasurement): LabValue? {
        val values = measurements[measurement]
        return if (!values.isNullOrEmpty()) values[0] else null
    }

    fun secondMostRecentValue(measurement: LabMeasurement): LabValue? {
        val values = measurements[measurement]
        return if (values != null && values.size >= 2) values[1] else null
    }

    fun valuesOnDate(measurement: LabMeasurement, dateToFind: LocalDate): List<LabValue> {
        return measurements[measurement]?.filter { it.date == dateToFind } ?: emptyList()
    }

    companion object {
        fun fromMeasurements(measurements: Map<LabMeasurement, List<LabValue>>): LabInterpretation {
            return LabInterpretation(
                measurements.mapValues { (_, values) -> values.sortedWith(LabValueDescendingDateComparator()) }.toSortedMap()
            )
        }
    }
}
