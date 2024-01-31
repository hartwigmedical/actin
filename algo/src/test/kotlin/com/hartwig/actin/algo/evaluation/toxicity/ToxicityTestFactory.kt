package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import java.time.LocalDate

internal object ToxicityTestFactory {
    val base = TestDataFactory.createMinimalTestPatientRecord()
    
    fun withToxicities(toxicities: List<Toxicity>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(toxicities = toxicities))
    }

    fun withToxicityThatIsAlsoComplication(toxicity: Toxicity): PatientRecord {
        val complication = Complication(name = toxicity.name, categories = emptySet(), year = null, month = null)
        return base.copy(clinical = base.clinical.copy(toxicities = listOf(toxicity), complications = listOf(complication)))
    }

    fun toxicity(
        name: String = "", source: ToxicitySource, grade: Int? = null, evaluatedDate: LocalDate = LocalDate.of(2010, 1, 1)
    ): Toxicity {
        return Toxicity(
            name = name,
            categories = emptySet(),
            evaluatedDate = evaluatedDate,
            source = source,
            grade = grade
        )
    }

    fun withIntolerance(intolerance: Intolerance): PatientRecord {
        return withIntolerances(listOf(intolerance))
    }

    fun withIntolerances(intolerances: List<Intolerance>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(intolerances = intolerances))
    }

    fun intolerance(name: String = "", category: String = "", clinicalStatus: String = "", doids: Set<String> = emptySet()): Intolerance {
        return Intolerance(
            name = name,
            doids = doids,
            category = category,
            subcategories = emptySet(),
            type = "",
            clinicalStatus = clinicalStatus,
            verificationStatus = "",
            criticality = ""
        )
    }
}