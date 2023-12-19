package com.hartwig.actin.molecular;

import java.io.File;
import java.io.IOException;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.serialization.MolecularRecordJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class MolecularJsonTestDataWriter {

    private static final Logger LOGGER = LogManager.getLogger(MolecularJsonTestDataWriter.class);

    private static final String WORK_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";

    public static void main(@NotNull String[] args) throws IOException {
        MolecularRecord testRecord = TestMolecularFactory.createProperTestMolecularRecord();

        LOGGER.info("Writing test molecular record to {}", WORK_DIRECTORY);
        MolecularRecordJson.write(testRecord, WORK_DIRECTORY);
    }
}
