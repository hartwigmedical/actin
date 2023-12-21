package com.hartwig.actin.clinical.datamodel.treatment

import com.hartwig.actin.Displayable
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.*

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Drug : Displayable {
    abstract fun name(): String
    abstract fun drugTypes(): Set<DrugType?>
    abstract fun category(): TreatmentCategory
    abstract fun displayOverride(): String?
    override fun display(): String {
        val alternateDisplay = displayOverride()
        return alternateDisplay ?: name().replace("_", " ").lowercase(Locale.getDefault())
    }
}
