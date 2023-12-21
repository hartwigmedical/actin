package com.hartwig.actin.clinical.serialization

import com.google.common.io.Resources
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createMinimalTestClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createProperTestClinicalRecord
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.fromJson
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.read
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.readFromDir
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.toJson
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.IOException

class ClinicalRecordJsonTest {
    @Test
    fun canConvertBackAndForthJson() {
        val minimal = createMinimalTestClinicalRecord()
        val convertedMinimal = fromJson(toJson(minimal))
        Assert.assertEquals(minimal, convertedMinimal)
        val proper = createProperTestClinicalRecord()
        val convertedProper = fromJson(toJson(proper))
        Assert.assertEquals(proper, convertedProper)
    }

    @Test
    @Throws(IOException::class)
    fun canReadClinicalRecordDirectory() {
        val records = readFromDir(CLINICAL_DIRECTORY)
        Assert.assertEquals(1, records.size.toLong())
        assertClinicalRecord(records[0])
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(IOException::class)
    fun cannotReadFilesFromNonDir() {
        readFromDir(CLINICAL_JSON)
    }

    @Test
    @Throws(IOException::class)
    fun canReadClinicalRecordJson() {
        assertClinicalRecord(read(CLINICAL_JSON))
    }

    companion object {
        private val CLINICAL_DIRECTORY = Resources.getResource("clinical" + File.separator + "records").path
        private val CLINICAL_JSON = CLINICAL_DIRECTORY + File.separator + "patient.clinical.json"
        private fun assertClinicalRecord(record: ClinicalRecord) {
            Assert.assertEquals("ACTN01029999", record.patientId())
            Assert.assertEquals(1, record.priorSecondPrimaries().size.toLong())
            Assert.assertEquals(1, record.priorOtherConditions().size.toLong())
            Assert.assertEquals(1, record.complications()!!.size.toLong())
            Assert.assertEquals(2, record.labValues().size.toLong())
            Assert.assertEquals(2, record.toxicities().size.toLong())
            Assert.assertEquals(2, record.intolerances().size.toLong())
            Assert.assertEquals(1, record.surgeries().size.toLong())
            Assert.assertEquals(1, record.vitalFunctions().size.toLong())
            Assert.assertEquals(1, record.bloodTransfusions().size.toLong())
            Assert.assertEquals(2, record.medications().size.toLong())
        }
    }
}