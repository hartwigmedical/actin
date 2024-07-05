package com.hartwig.actin.clinical.feed.mcgi

import java.time.LocalDate


data class McgiPatient(
    val caseNumber: Int,
    val age: Int,
    val tumorStage: String,
    val gender: String,
    val diagnosisDate: LocalDate,
    val tumorLocation: String,
    val tumorType: String,
    val tumorGradeDifferentiation: String,
    val lesionSite: String,
    val clinical: McgiClinical,
    val molecular: McgiMolecular,
)

data class McgiClinical(val attributes: List<String>)

data class McgiMolecular(val variants: List<McgiVariant>, val amplications: List<McgiAmplification>, val isMSI: Boolean, val tmb: Double)

data class McgiVariant(val resultDate: LocalDate, val testType: String, val gene: String, val hgvsCodingEffect: String, val vaf: Double)

data class McgiAmplification(val resultDate: LocalDate, val testType: String, val gene: String, val chromosome: String)