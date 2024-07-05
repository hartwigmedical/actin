package com.hartwig.actin.clinical.feed.mcgi

import com.hartwig.actin.clinical.feed.standard.ProvidedMolecularTest
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientDetail
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.clinical.feed.standard.ProvidedPriorOtherCondition
import com.hartwig.actin.clinical.feed.standard.ProvidedTumorDetail
import java.time.LocalDate

class McgiFeedAdapter {

    fun convert(mcgiPatient: McgiPatient): ProvidedPatientRecord {
        return ProvidedPatientRecord(
            patientDetails = ProvidedPatientDetail(
                birthYear = mcgiPatient.age,
                gender = mcgiPatient.gender,
                registrationDate = LocalDate.now(),
                hashedId = mcgiPatient.caseNumber.toString()
            ),
            tumorDetails = ProvidedTumorDetail(
                diagnosisDate = mcgiPatient.diagnosisDate,
                tumorStage = mcgiPatient.tumorStage,
                tumorLocation = mcgiPatient.tumorLocation,
                tumorType = mcgiPatient.tumorType,
                tumorGradeDifferentiation = mcgiPatient.tumorGradeDifferentiation
            ),
            priorOtherConditions = mcgiPatient.clinical.attributes.map { ProvidedPriorOtherCondition(it) },
            molecularTestHistory = mcgiPatient.molecular.variants.map {
                ProvidedMolecularTest(
                    resultDate = it.resultDate,
                    type = it.testType,
                    measure = it.gene,
                    result = it.hgvsCodingEffect,
                    resultType = "variant"
                )
            } + mcgiPatient.molecular.amplications.map {
                ProvidedMolecularTest(
                    resultDate = it.resultDate,
                    type = it.testType,
                    measure = it.gene,
                    result = it.chromosome,
                    resultType = "amplification"
                )
            } + listOf(
                ProvidedMolecularTest(
                    type = "MSI",
                    measure = "msi",
                    result = mcgiPatient.molecular.isMSI.toString(),
                    resultDate = LocalDate.now(),
                    resultType = "msi"
                ),
                ProvidedMolecularTest(
                    type = "TMB",
                    measure = "tmb",
                    result = mcgiPatient.molecular.tmb.toString(),
                    resultDate = LocalDate.now(),
                    resultType = "tmb"
                )
            )
        )
    }
}