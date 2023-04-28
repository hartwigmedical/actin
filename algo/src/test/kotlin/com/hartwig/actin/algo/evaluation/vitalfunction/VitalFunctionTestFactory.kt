package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.VitalFunction
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

internal object VitalFunctionTestFactory {
    fun withBodyWeights(bodyWeights: List<BodyWeight>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .bodyWeights(bodyWeights)
                    .build()
            )
            .build()
    }

    fun bodyWeight(): ImmutableBodyWeight.Builder {
        return ImmutableBodyWeight.builder().date(LocalDate.of(2017, 7, 7)).value(0.0).unit(Strings.EMPTY)
    }

    fun withVitalFunctions(vitalFunctions: List<VitalFunction>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .vitalFunctions(vitalFunctions)
                    .build()
            )
            .build()
    }

    fun vitalFunction(): ImmutableVitalFunction.Builder {
        return ImmutableVitalFunction.builder().date(LocalDate.of(2017, 7, 7)).subcategory(Strings.EMPTY).value(0.0).unit(Strings.EMPTY)
    }
}