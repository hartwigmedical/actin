package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import java.time.LocalDate

internal object OtherConditionTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    
    fun withPriorOtherCondition(condition: PriorOtherCondition): PatientRecord {
        return withPriorOtherConditions(listOf(condition))
    }

    fun withPriorOtherConditions(conditions: List<PriorOtherCondition>): PatientRecord {
        return withComorbidities(conditions)
    }

    fun priorOtherCondition(
        name: String = "",
        year: Int? = null,
        month: Int? = null,
        icdMainCode: String = "",
        icdExtensionCode: String? = null
    ): PriorOtherCondition {
        return PriorOtherCondition(
            name = name,
            year = year,
            month = month,
            icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode)),
        )
    }

    fun intolerance(name: String = "", icdMainCode: String = "", icdExtensionCode: String? = null): Intolerance {
        return Intolerance(
            name = name,
            icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode)),
            type = "",
            clinicalStatus = "",
            verificationStatus = "",
            criticality = ""
        )
    }

    fun complication(name: String = "", icdMainCode: String = "", icdExtensionCode: String? = null): Complication {
        return Complication(name = name, year = null, month = null, icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode)))
    }

    fun toxicity(
        name: String, toxicitySource: ToxicitySource, grade: Int?, icdMainCode: String = "code", icdExtensionCode: String? = null, date: LocalDate = LocalDate.of(2010, 1, 1)
    ): Toxicity {
        return Toxicity(
            name = name,
            icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode)),
            evaluatedDate = date,
            source = toxicitySource,
            grade = grade
        )
    }

    fun withComplications(complications: List<Complication>): PatientRecord {
        return withComorbidities(complications)
    }

    fun withToxicities(toxicities: List<Toxicity>): PatientRecord {
        return withComorbidities(toxicities)
    }

    fun withIntolerances(intolerances: List<Intolerance>): PatientRecord {
        return withComorbidities(intolerances)
    }

    private fun withComorbidities(comorbidities: List<Comorbidity>): PatientRecord {
        return base.copy(comorbidities = comorbidities)
    }

    fun withMedications(medications: List<Medication>): PatientRecord {
        return base.copy(medications = medications)
    }

    fun withWHO(who: Int?): PatientRecord {
        return base.copy(clinicalStatus = ClinicalStatus(who = who))
    }
}