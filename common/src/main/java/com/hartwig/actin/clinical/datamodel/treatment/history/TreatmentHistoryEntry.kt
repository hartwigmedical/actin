package com.hartwig.actin.clinical.datamodel.treatment.history

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class TreatmentHistoryEntry {
    abstract fun treatments(): Set<Treatment>
    abstract fun startYear(): Int?
    abstract fun startMonth(): Int?
    abstract fun intents(): Set<Intent?>?

    @get:Value.Default
    open val isTrial: Boolean
        get() = false

    abstract fun trialAcronym(): String?
    abstract fun treatmentHistoryDetails(): TreatmentHistoryDetails?
    fun allTreatments(): Set<Treatment> {
        val details = treatmentHistoryDetails() ?: return treatments()
        val maintenanceTreatmentStream = if (details.maintenanceTreatment() == null) Stream.empty() else Stream.of(
            details.maintenanceTreatment()!!.treatment()
        )
        val switchToTreatmentStream = if (details.switchToTreatments() == null) Stream.empty() else details.switchToTreatments()!!
            .stream().map { obj: TreatmentStage? -> obj!!.treatment() }
        return Stream.of(treatments().stream(), maintenanceTreatmentStream, switchToTreatmentStream)
            .flatMap(Function.identity())
            .collect(Collectors.toSet())
    }

    fun treatmentName(): String {
        return treatmentStringUsingFunction(allTreatments()) { obj: Treatment -> obj.name() }
    }

    fun categories(): Set<TreatmentCategory> {
        return allTreatments().stream().flatMap { treatment: Treatment -> treatment.categories().stream() }
            .collect(Collectors.toSet())
    }

    fun isOfType(typeToFind: TreatmentType): Boolean? {
        return matchesTypeFromSet(java.util.Set.of(typeToFind))
    }

    fun matchesTypeFromSet(types: Set<TreatmentType>): Boolean? {
        return if (hasTypeConfigured()) isTypeFromCollection(types) else null
    }

    fun hasTypeConfigured(): Boolean {
        return allTreatments().stream().noneMatch { treatment: Treatment -> treatment.types().isEmpty() }
    }

    private fun isTypeFromCollection(types: Set<TreatmentType>): Boolean {
        return allTreatments().stream().flatMap { treatment: Treatment -> treatment.types().stream() }
            .anyMatch { o: TreatmentType -> types.contains(o) }
    }

    fun treatmentDisplay(): String {
        val treatmentNames = treatments().stream().map { obj: Treatment -> obj.display() }
            .map { obj: String -> obj.lowercase(Locale.getDefault()) }.collect(Collectors.toSet())
        val chemoradiationTherapyNames = setOf("chemotherapy", "radiotherapy")
        if (treatmentNames.containsAll(chemoradiationTherapyNames)) {
            val remainingTreatments = treatments().stream()
                .filter { treatment: Treatment -> !chemoradiationTherapyNames.contains(treatment.display().lowercase(Locale.getDefault())) }
                .collect(Collectors.toList())
            if (remainingTreatments.isEmpty()) {
                return "Chemoradiation"
            } else if (remainingTreatments.size == 1) {
                val remainingTreatment = remainingTreatments[0]
                return if (remainingTreatment.categories().contains(TreatmentCategory.CHEMOTHERAPY)) {
                    String.format("Chemoradiation (with %s)", remainingTreatment.display())
                } else {
                    String.format("Chemoradiation and %s", remainingTreatment.display())
                }
            }
        }
        return treatmentStringUsingFunction(treatments()) { obj: Treatment -> obj.display() }
    }

    companion object {
        private const val DELIMITER = ";"
        private fun treatmentStringUsingFunction(treatments: Set<Treatment>, treatmentField: Function<Treatment, String>): String {
            val nameString = treatments.stream().map(treatmentField).distinct().collect(Collectors.joining(DELIMITER))
            return if (!nameString.isEmpty()) nameString else treatmentCategoryDisplay(treatments)
        }

        private fun treatmentCategoryDisplay(treatments: Set<Treatment>): String {
            return treatments.stream().flatMap { t: Treatment -> t.categories().stream().map { obj: TreatmentCategory -> obj.display() } }
                .distinct()
                .collect(Collectors.joining(DELIMITER))
        }
    }
}
