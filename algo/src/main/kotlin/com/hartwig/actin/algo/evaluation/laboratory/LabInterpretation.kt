package com.hartwig.actin.algo.evaluation.laboratory

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

    fun mostRecentValue(measurement: LabMeasurement, highestFirst: Boolean = true): LabValue? {
        val values = sort(measurements[measurement] ?: emptyList(), highestFirst)
        return if (values.isNotEmpty()) values[0] else null
    }

    fun secondMostRecentValue(measurement: LabMeasurement, highestFirst: Boolean = true): LabValue? {
        val values = sort(measurements[measurement] ?: emptyList(), highestFirst)
        return if (values.size >= 2) values[1] else null
    }

    fun valuesOnDate(measurement: LabMeasurement, dateToFind: LocalDate): List<LabValue> {
        return measurements[measurement]?.filter { it.date == dateToFind } ?: emptyList()
    }

    private fun sort(labValues: List<LabValue>, highestFirst: Boolean): List<LabValue> {
        return labValues.sortedWith(LabValueDescendingDateComparator(highestFirst))
    }

    companion object {
        fun interpret(labValues: List<LabValue>): LabInterpretation {
            return LabInterpretation(
                LabMeasurement.entries.associateWith { measurement ->
                    labValues.filter { it.measurement == measurement }
                }.toSortedMap()
            )
        }
    }
}
