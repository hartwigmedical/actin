package com.hartwig.actin.algo.evaluation.laboratory

import com.google.common.collect.Lists
import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

internal object LabTestFactory {
    fun withLabValue(labValue: LabValue): PatientRecord {
        return withLabValues(Lists.newArrayList(labValue))
    }

    fun withLabValues(labValues: List<LabValue>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .labValues(labValues)
                    .build()
            )
            .build()
    }

    fun forMeasurement(measurement: LabMeasurement): ImmutableLabValue.Builder {
        return builder().code(measurement.code()).unit(measurement.defaultUnit())
    }

    fun builder(): ImmutableLabValue.Builder {
        return ImmutableLabValue.builder()
            .date(LocalDate.of(2020, 1, 1))
            .name(Strings.EMPTY)
            .code(Strings.EMPTY)
            .comparator(Strings.EMPTY)
            .value(0.0)
            .unit(LabUnit.NONE)
    }
}