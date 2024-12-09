package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Complication
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
        return base.copy(priorOtherConditions = conditions)
    }

    fun priorOtherCondition(
        name: String = "",
        year: Int? = null,
        month: Int? = null,
        doids: Set<String> = emptySet(),
        category: String = "",
        icdCode: String = "",
        isContraindication: Boolean = true
    ): PriorOtherCondition {
        return PriorOtherCondition(
            name = name,
            year = year,
            month = month,
            doids = doids,
            category = category,
            icdCode = icdCode,
            isContraindicationForTherapy = isContraindication,
        )
    }

    fun intolerance(name: String = "", icdCode: String = ""): Intolerance {
        return Intolerance(
            name = name,
            icdCode = icdCode,
            doids = emptySet(),
            category = "",
            subcategories = emptySet(),
            type = "",
            clinicalStatus = "",
            verificationStatus = "",
            criticality = "",
            treatmentCategories = emptySet()
        )
    }

    fun complication(name: String = "", categories: Set<String> = emptySet(), icdCode: String = ""): Complication {
        return Complication(name = name, categories = categories, icdCode = icdCode, year = null, month = null)
    }

    fun toxicity(
        name: String, toxicitySource: ToxicitySource, grade: Int?, icdCode: String = "code", date: LocalDate = LocalDate.of(2010, 1, 1)
    ): Toxicity {
        return Toxicity(
            name = name,
            categories = emptySet(),
            icdCode = icdCode,
            evaluatedDate = date,
            source = toxicitySource,
            grade = grade
        )
    }

    fun withComplications(complications: List<Complication>): PatientRecord {
        return base.copy(complications = complications)
    }

    fun withToxicities(toxicities: List<Toxicity>): PatientRecord {
        return base.copy(toxicities = toxicities)
    }

    fun withIntolerances(intolerances: List<Intolerance>): PatientRecord {
        return base.copy(intolerances = intolerances)
    }

    fun withMedications(medications: List<Medication>): PatientRecord {
        return base.copy(medications = medications)
    }

    fun withWHO(who: Int?): PatientRecord {
        return base.copy(clinicalStatus = ClinicalStatus(who = who))
    }
}