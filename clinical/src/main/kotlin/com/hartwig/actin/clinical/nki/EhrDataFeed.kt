package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import java.nio.file.Files
import java.nio.file.Paths

class EhrDataFeed(private val directory: String, private val primaryTumorCuration: CurationDatabase<PrimaryTumorConfig>) {

    fun ingest(): List<EhrPatientRecord> {
        Files.list(Paths.get(directory)).map {
            val ehrPatientRecord = feedJson.decodeFromString(EhrPatientRecord.serializer(), Files.readString(it))
            val patientDetails = ImmutablePatientDetails.builder().gender(Gender.valueOf(ehrPatientRecord.patientDetails.gender))
                .birthYear(ehrPatientRecord.patientDetails.birthYear).registrationDate(ehrPatientRecord.patientDetails.registrationDate)
                .build()

            primaryTumorCuration.find(ehrPatientRecord.tumorDetails.tumorTypeDetails)

            val curatedTumor = CurationResponse.createFromConfigs(
                primaryTumorCuration.find(
                    tumorInput(
                        ehrPatientRecord.tumorDetails.tumorLocalization,
                        ehrPatientRecord.tumorDetails.tumorTypeDetails
                    )
                ), ehrPatientRecord.patientId, CurationCategory.PRIMARY_TUMOR, tumorInput(
                    ehrPatientRecord.tumorDetails.tumorLocalization,
                    ehrPatientRecord.tumorDetails.tumorTypeDetails
                ), "tumor type", true
            )

            val tumorDetails = ImmutableTumorDetails.builder().primaryTumorLocation(ehrPatientRecord.tumorDetails.tumorLocalization)
                .primaryTumorType(ehrPatientRecord.tumorDetails.tumorTypeDetails)
                .stage(TumorStage.valueOf(ehrPatientRecord.tumorDetails.tumorStage))
                .doids(curatedTumor.config()!!.doids)
                .hasBoneLesions(curatedTumor.config())
                .hasMeasurableDisease(ehrPatientRecord.tumorDetails.measurableDisease)
                .build()
        }
        return emptyList()
    }

    private fun tumorInput(inputTumorLocation: String, inputTumorType: String): String {
        return CurationUtil.fullTrim(listOf(inputTumorLocation, inputTumorType).joinToString(" | ") { it })
    }
}