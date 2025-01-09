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
        return base.copy(comorbidities = toxicities)
    }

    fun withToxicityThatIsAlsoComplication(toxicity: Toxicity, icdCode: String = ""): PatientRecord {
        val complication =
            Complication(name = toxicity.name, year = null, month = null, icdCodes = setOf(IcdCode(icdCode)))
        return base.copy(comorbidities = listOf(toxicity.copy(icdCodes = setOf(IcdCode(icdCode)))) + complication)
    }

    fun withIntolerance(intolerance: Intolerance): PatientRecord {
        return withIntolerances(listOf(intolerance))
    }

    fun withIntolerances(intolerances: List<Intolerance>): PatientRecord {
        return base.copy(comorbidities = intolerances)
    }

    fun intolerance(
        name: String = "",
        clinicalStatus: String = "",
        icdMainCode: String = "",
        icdExtensionCode: String? = null
    ): Intolerance {
        return Intolerance(
            name = name,
            icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode)),
            type = "",
            clinicalStatus = clinicalStatus,
            verificationStatus = "",
            criticality = ""
        )
    }
}