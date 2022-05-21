package com.hartwig.actin.clinical;

import java.io.File;
import java.io.IOException;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ClinicalJsonTestDataWriter {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalJsonTestDataWriter.class);

    private static final String WORK_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";

    public static void main(@NotNull String[] args) throws IOException {
        ClinicalRecord testRecord = TestClinicalFactory.createProperTestClinicalRecord();

        LOGGER.info("Writing test clinical record to {}", WORK_DIRECTORY);
        ClinicalRecordJson.write(Lists.newArrayList(testRecord), WORK_DIRECTORY);
    }
}
