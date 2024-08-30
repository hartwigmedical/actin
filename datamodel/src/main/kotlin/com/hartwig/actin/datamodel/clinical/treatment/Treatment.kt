package com.hartwig.actin.datamodel.clinical.treatment

import com.hartwig.actin.datamodel.Displayable

interface Treatment : Displayable, Comparable<Treatment> {
    val name: String
    val synonyms: Set<String>
    val isSystemic: Boolean
    val displayOverride: String?
    val treatmentClass: TreatmentClass
    fun categories(): Set<TreatmentCategory>
    fun types(): Set<TreatmentType>
    
    override fun display(): String {
        return displayOverride ?: name.replace("_", " ").split("\\+".toRegex()).dropLastWhile { it.isEmpty() }
            .joinToString("+") { name: String ->
                if (name.length < 2) name else {
                    name.substring(0, 1).uppercase() + name.substring(1).lowercase()
                }
            }
    }

    override fun compareTo(other: Treatment): Int {
        return Comparator.comparing(Treatment::isSystemic)
            .thenComparing({ it.categories().firstOrNull() }, Comparator.nullsLast(Comparator.naturalOrder<TreatmentCategory?>()))
            .thenComparing(Treatment::name)
            .compare(this, other)
    }
}