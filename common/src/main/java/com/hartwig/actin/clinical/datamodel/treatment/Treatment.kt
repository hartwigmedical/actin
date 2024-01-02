package com.hartwig.actin.clinical.datamodel.treatment

import com.hartwig.actin.Displayable

interface Treatment : Displayable {
    val name: String
    val categories: Set<TreatmentCategory>
    val types: Set<TreatmentType>
    val synonyms: Set<String>
    val isSystemic: Boolean
    val displayOverride: String?
    val treatmentClass: TreatmentClass
    
    override fun display(): String {
        return displayOverride ?: name.replace("_", " ").split("\\+".toRegex()).dropLastWhile { it.isEmpty() }
            .joinToString("+") { name: String ->
                if (name.length < 2) name else {
                    name.substring(0, 1).uppercase() + name.substring(1).lowercase()
                }
            }
    }
}