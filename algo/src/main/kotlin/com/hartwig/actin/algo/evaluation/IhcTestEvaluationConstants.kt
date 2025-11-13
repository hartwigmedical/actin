package com.hartwig.actin.algo.evaluation

object IhcTestEvaluationConstants {

    val EXACT_POSITIVE_TERMS = setOf("positive", "present", "detected", "strong positive", "overexpression")
    val EXACT_NEGATIVE_TERMS = setOf("negative", "absent", "loss")
    val EXACT_LOSS_TERMS = setOf("loss")
    val EXACT_NO_LOSS_TERMS = setOf("no loss", "positive", "normal", "normal expression")
    val WILD_TYPE_TERMS = setOf("wildtype", "wild-type", "wild type")
}