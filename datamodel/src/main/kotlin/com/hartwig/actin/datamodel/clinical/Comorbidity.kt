package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable
import java.time.LocalDate

sealed interface Comorbidity : Displayable {
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

data class Complication(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    override val year: Int? = null,
    override val month: Int? = null,
): Comorbidity {
    override val comorbidityClass = ComorbidityClass.COMPLICATION

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}

data class Ecg(
    override val name: String?,
    val qtcfMeasure: EcgMeasure?,
    val jtcMeasure: EcgMeasure?,
    override val icdCodes: Set<IcdCode> = emptySet(),
    override val year: Int? = null,
    override val month: Int? = null
) : Comorbidity {
    override val comorbidityClass = ComorbidityClass.ECG

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}

data class Intolerance(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    val type: String? = null,
    val clinicalStatus: String? = null,
    val verificationStatus: String? = null,
    val criticality: String? = null,
    override val year: Int? = null,
    override val month: Int? = null
): Comorbidity {
    override val comorbidityClass = ComorbidityClass.INTOLERANCE

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}

data class OtherCondition(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    override val year: Int? = null,
    override val month: Int? = null
): Comorbidity {
    override val comorbidityClass = ComorbidityClass.OTHER_CONDITION

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}

data class Toxicity(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    val evaluatedDate: LocalDate?,
    val source: ToxicitySource,
    val grade: Int?,
    val endDate: LocalDate? = null
) : Comorbidity {
    override val comorbidityClass = ComorbidityClass.TOXICITY
    override val year: Int?
        get() = evaluatedDate?.year
    override val month: Int?
        get() = evaluatedDate?.monthValue

    override fun withDefaultDate(date: LocalDate): Comorbidity = copy(evaluatedDate = date)
}