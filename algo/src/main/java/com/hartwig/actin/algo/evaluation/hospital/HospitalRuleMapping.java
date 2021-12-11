package com.hartwig.actin.algo.evaluation.hospital;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class HospitalRuleMapping {

    private HospitalRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.PATIENT_IS_TREATED_IN_HOSPITAL_X, patientIsTreatedInHospitalCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator patientIsTreatedInHospitalCreator() {
        return function -> new PatientIsTreatedInHospital();
    }
}
