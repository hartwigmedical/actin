package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Report {
    abstract fun patientId(): String
    abstract fun clinical(): ClinicalRecord
    abstract fun molecular(): MolecularRecord
    abstract fun treatmentMatch(): TreatmentMatch
}