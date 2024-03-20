package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import java.time.LocalDate

internal object OtherConditionTestFactory {
    private val base = TestPatientFactory.createMinimalTestPatientRecord()
    
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
        isContraindication: Boolean = true
    ): PriorOtherCondition {
        return PriorOtherCondition(
            name = name,
            year = year,
            month = month,
            doids = doids,
            category = category,
            isContraindicationForTherapy = isContraindication,
        )
    }

    fun intolerance(name: String = ""): Intolerance {
        return Intolerance(
            name = name,
            doids = emptySet(),
            category = "",
            subcategories = emptySet(),
            type = "",
            clinicalStatus = "",
            verificationStatus = "",
            criticality = ""
        )
    }

    fun complication(name: String = "", categories: Set<String> = emptySet()): Complication {
        return Complication(name = name, categories = categories, year = null, month = null)
    }

    fun toxicity(name: String, toxicitySource: ToxicitySource, grade: Int?): Toxicity {
        return Toxicity(
            name = name,
            categories = emptySet(),
            evaluatedDate = LocalDate.of(2010, 1, 1),
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