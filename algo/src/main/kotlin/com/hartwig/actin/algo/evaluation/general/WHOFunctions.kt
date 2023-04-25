package com.hartwig.actin.algo.evaluation.general;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions;

public final class WHOFunctions {

    public static final List<String> COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS =
            Arrays.asList("Ascites", "Pleural effusion", "Pericardial effusion", "Pain", "Spinal cord compression");

    public static Set<String> findComplicationCategoriesAffectingWHOStatus(PatientRecord record) {
        return ComplicationFunctions.findComplicationCategoriesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS);
    }
}
