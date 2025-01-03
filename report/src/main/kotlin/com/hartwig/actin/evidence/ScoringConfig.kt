package com.hartwig.actin.evidence

data class ScoringConfig(val levels: List<ScoringLevel>)

data class ScoringLevel(val factor: Int, val scoring: Map<String, Int>)

fun create() = ScoringConfig(listOf(ScoringLevel(20, mapOf(None	0
        FDA approved - On Companion Diagnostic	100
        FDA approval	100
        Guideline	95
        Phase III	90
        Phase II	85
        Clinical Study--Meta analysis	50
        Clinical Study--Cohort	45
        Phase Ib/II	40
        Phase I	35
        Clinical Study	30
        Case Reports/Case Series	25
        Pre-Clinical PDX	10
        Pre-Clinical PDX and Cell Culture	9
        Pre-Clinical Cell Line Xenograft	8
        Pre-Clinical Patient Cell Culture	7
        Pre-Clinical Cell culture	6
        Pre-Clinical	5))