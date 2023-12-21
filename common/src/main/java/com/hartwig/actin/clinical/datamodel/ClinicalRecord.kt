package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ClinicalRecord {
    abstract fun patientId(): String
    abstract fun patient(): PatientDetails
    abstract fun tumor(): TumorDetails
    abstract fun clinicalStatus(): ClinicalStatus
    abstract fun oncologicalHistory(): List<TreatmentHistoryEntry?>
    abstract fun priorSecondPrimaries(): List<PriorSecondPrimary?>
    abstract fun priorOtherConditions(): List<PriorOtherCondition?>
    abstract fun priorMolecularTests(): List<PriorMolecularTest?>
    abstract fun complications(): List<Complication?>?
    abstract fun labValues(): List<LabValue?>
    abstract fun toxicities(): List<Toxicity?>
    abstract fun intolerances(): List<Intolerance?>
    abstract fun surgeries(): List<Surgery?>
    abstract fun bodyWeights(): List<BodyWeight?>
    abstract fun vitalFunctions(): List<VitalFunction?>
    abstract fun bloodTransfusions(): List<BloodTransfusion?>
    abstract fun medications(): List<Medication?>
}
