package com.hartwig.actin.molecular

import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.createProperTestMolecularRecord
import com.hartwig.actin.molecular.serialization.MolecularRecordJson.write
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

object MolecularJsonTestDataWriter {
    val LOGGER: Logger = LogManager.getLogger(MolecularJsonTestDataWriter::class.java)
    val WORK_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp"
}

fun main() {
    val testRecord = createProperTestMolecularRecord()
    MolecularJsonTestDataWriter.LOGGER.info("Writing test molecular record to {}", MolecularJsonTestDataWriter.WORK_DIRECTORY)
    write(testRecord, MolecularJsonTestDataWriter.WORK_DIRECTORY)
}
