package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.write
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory.createProperTestClinicalRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

object ClinicalJsonTestDataWriter {
    val LOGGER: Logger = LogManager.getLogger(ClinicalJsonTestDataWriter::class.java)
    val WORK_DIRECTORY = listOf(System.getProperty("user.home"), "hmf", "tmp").joinToString(File.separator)
}

fun main() {
    val testRecord = createProperTestClinicalRecord()
    ClinicalJsonTestDataWriter.LOGGER.info("Writing test clinical record to {}", ClinicalJsonTestDataWriter.WORK_DIRECTORY)
    write(listOf(testRecord), ClinicalJsonTestDataWriter.WORK_DIRECTORY)
}
