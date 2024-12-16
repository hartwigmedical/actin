package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.Toxicity

internal object ToxicityTestFactory {
    val base = TestPatientFactory.createMinimalTestWGSPatientRecord()

    fun withToxicities(toxicities: List<Toxicity>): PatientRecord {
        return base.copy(toxicities = toxicities)
    }

    fun withToxicityThatIsAlsoComplication(toxicity: Toxicity, icdCode: String = ""): PatientRecord {
        val complication =
            Complication(name = toxicity.name, icdCode = IcdCode(icdCode), year = null, month = null)
        return base.copy(toxicities = listOf(toxicity.copy(icdCode = IcdCode(icdCode))), complications = listOf(complication))
    }

    fun withIntolerance(intolerance: Intolerance): PatientRecord {
        return withIntolerances(listOf(intolerance))
    }

    fun withIntolerances(intolerances: List<Intolerance>): PatientRecord {
        return base.copy(intolerances = intolerances)
    }

    fun intolerance(
        name: String = "",
        category: String = "",
        clinicalStatus: String = "",
        icdMainCode: String = "",
        icdExtensionCode: String? = null
    ): Intolerance {
        return Intolerance(
            name = name,
            icdCode = IcdCode(icdMainCode, icdExtensionCode),
            category = category,
            subcategories = emptySet(),
            type = "",
            clinicalStatus = clinicalStatus,
            verificationStatus = "",
            criticality = "",
            treatmentCategories = emptySet()
        )
    }
}