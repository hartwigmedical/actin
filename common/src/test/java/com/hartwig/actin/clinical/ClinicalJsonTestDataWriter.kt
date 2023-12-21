package com.hartwig.actin.clinical

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createProperTestClinicalRecord
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.write
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException

object ClinicalJsonTestDataWriter {
    private val LOGGER = LogManager.getLogger(ClinicalJsonTestDataWriter::class.java)
    private val WORK_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp"

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val testRecord = createProperTestClinicalRecord()
        LOGGER.info("Writing test clinical record to {}", WORK_DIRECTORY)
        write(Lists.newArrayList(testRecord), WORK_DIRECTORY)
    }
}
