package com.hartwig.actin.clinical.datamodel

data class Intolerance(
    val name: String,
    val doids: Set<String>,
    val category: String,
    val subcategories: Set<String>,
    val type: String,
    val clinicalStatus: String,
    val verificationStatus: String,
    val criticality: String,
    val drugAllergyType: String,
)
