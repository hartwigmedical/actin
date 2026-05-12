package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.write
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory.createProperTestClinicalRecord
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

object ClinicalJsonTestDataWriter {
    val logger = KotlinLogging.logger {}
    val WORK_DIRECTORY = Path.of(System.getProperty("user.home"), "hmf", "tmp")
}

fun main() {
    val testRecord = createProperTestClinicalRecord()
    ClinicalJsonTestDataWriter.logger.info { "Writing test clinical record to ${ClinicalJsonTestDataWriter.WORK_DIRECTORY}" }
    write(listOf(testRecord), ClinicalJsonTestDataWriter.WORK_DIRECTORY)
}
