package com.hartwig.actin.clinical.datamodel.treatment

import com.hartwig.actin.Displayable
import java.util.*
import java.util.stream.Collectors

interface Treatment : Displayable {
    fun name(): String
    fun categories(): Set<TreatmentCategory>
    fun types(): Set<TreatmentType?>
    fun synonyms(): Set<String?>

    @JvmField
    val isSystemic: Boolean
    fun displayOverride(): String?
    override fun display(): String {
        val alternateDisplay = displayOverride()
        return alternateDisplay
            ?: Arrays.stream(name().replace("_", " ").split("\\+".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
                .map { name: String ->
                    if (name.length < 2) name else name.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1)
                        .lowercase(Locale.getDefault())
                }
                .collect(Collectors.joining("+"))
    }
}