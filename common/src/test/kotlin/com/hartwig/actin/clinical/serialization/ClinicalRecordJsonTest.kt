package com.hartwig.actin.clinical.serialization

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.fromJson
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.read
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.readFromDir
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.toJson
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory.createMinimalTestClinicalRecord
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory.createProperTestClinicalRecord
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class ClinicalRecordJsonTest {

    private val clinicalDirectory = resourceOnClasspath("clinical" + File.separator + "records")
    private val clinicalJson = clinicalDirectory + File.separator + "patient.clinical.json"

    @Test
    fun `Should be able to convert clinical JSON back and forth`() {
        val minimal = createMinimalTestClinicalRecord()
        val convertedMinimal = fromJson(toJson(minimal))
        assertThat(convertedMinimal).isEqualTo(minimal)

        val proper = createProperTestClinicalRecord()
        val convertedProper = fromJson(toJson(proper))
        assertThat(convertedProper).isEqualTo(proper)
    }

    @Test
    fun `Should be able to read a clinical directory`() {
        val records = readFromDir(clinicalDirectory)
        assertThat(records).hasSize(1)
        assertClinicalRecord(records[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception when attempting to read a directory from a file`() {
        readFromDir(clinicalJson)
    }

    @Test
    fun `Should correctly read test clinical JSON`() {
        assertClinicalRecord(read(clinicalJson))
    }

    private fun assertClinicalRecord(record: ClinicalRecord) {
        assertRequiredFieldsForClass(record, ClinicalRecord::class)
        assertThat(record.patientId).isEqualTo("ACTN01029999")
        assertRequiredFieldsForClass(record.patient, PatientDetails::class)
        assertRequiredFieldsForClass(record.tumor, TumorDetails::class)
        assertRequiredFieldsForClass(record.clinicalStatus, ClinicalStatus::class)

        assertCollectionOfClassWithSize(record.priorPrimaries, PriorPrimary::class, 1)
        assertCollectionOfClassWithSize(record.oncologicalHistory, TreatmentHistoryEntry::class, 1)
        assertCollectionOfClassWithSize(
            record.oncologicalHistory.flatMap(TreatmentHistoryEntry::allTreatments), Treatment::class, 1
        )

        assertThat(record.comorbidities).hasSize(7)
        assertCollectionOfClassWithSize(record.otherConditions, OtherCondition::class, 1)
        assertCollectionOfClassWithSize(record.complications, Complication::class, 1)
        assertCollectionOfClassWithSize(record.toxicities, Toxicity::class, 2)
        assertCollectionOfClassWithSize(record.intolerances, Intolerance::class, 2)
        assertCollectionOfClassWithSize(record.ecgs, Ecg::class, 1)

        assertCollectionOfClassWithSize(record.ihcTests, IHCTest::class, 1)
        assertCollectionOfClassWithSize(record.labValues, LabValue::class, 2)
        assertCollectionOfClassWithSize(record.surgeries, Surgery::class, 1)
        assertCollectionOfClassWithSize(record.bodyWeights, BodyWeight::class, 1)
        assertCollectionOfClassWithSize(record.vitalFunctions, VitalFunction::class, 1)
        assertCollectionOfClassWithSize(record.bloodTransfusions, BloodTransfusion::class, 1)
        assertCollectionOfClassWithSize(record.medications, Medication::class, 2)
    }

    private fun <T : Any> assertCollectionOfClassWithSize(collection: List<T>?, kotlinClass: KClass<T>, expectedSize: Int) {
        assertThat(collection).isNotNull
        assertThat(collection).hasSize(expectedSize)
        collection!!.forEach { assertRequiredFieldsForClass(it, kotlinClass) }
    }

    private fun <T : Any> assertRequiredFieldsForClass(instance: T, kotlinClass: KClass<T>) {
        kotlinClass.memberProperties.filterNot { it.returnType.isMarkedNullable }.forEach {
            assertThat(it.get(instance)).withFailMessage { "Field ${it.name} is null for ${kotlinClass.simpleName}" }.isNotNull
        }
    }
}
