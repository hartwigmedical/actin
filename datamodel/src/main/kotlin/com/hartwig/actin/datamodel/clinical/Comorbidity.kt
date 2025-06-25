package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable
import java.time.LocalDate

interface Comorbidity : Displayable {
    val name: String?
    val icdCodes: Set<IcdCode>
    val year: Int?
    val month: Int?
    val comorbidityClass: ComorbidityClass

    override fun display(): String {
        return name ?: ""
    }

    fun withDefaultDate(date: LocalDate): Comorbidity
}

data class BaseComorbidity(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    override val year: Int? = null,
    override val month: Int? = null,
) : Comorbidity {
    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        BaseComorbidity(name, icdCodes, date.year, date.monthValue)
    }

    override val comorbidityClass: ComorbidityClass
        get() = ComorbidityClass.BASE_COMORBIDITY // Default class, should be overridden in subclasses
}