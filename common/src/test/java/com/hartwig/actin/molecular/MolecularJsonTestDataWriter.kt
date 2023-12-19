package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.createProperTestMolecularRecord
import com.hartwig.actin.molecular.serialization.MolecularRecordJson.write
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException

object MolecularJsonTestDataWriter {
    private val LOGGER = LogManager.getLogger(MolecularJsonTestDataWriter::class.java)
    private val WORK_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp"

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val testRecord = createProperTestMolecularRecord()
        LOGGER.info("Writing test molecular record to {}", WORK_DIRECTORY)
        write(testRecord, WORK_DIRECTORY)
    }
}
