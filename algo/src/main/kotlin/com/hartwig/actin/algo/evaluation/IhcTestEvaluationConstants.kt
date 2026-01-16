package com.hartwig.actin.algo.evaluation

object IhcTestEvaluationConstants {

    val POSITIVE_TERMS = setOf("positive", "strong positive")
    val BROAD_POSITIVE_TERMS = POSITIVE_TERMS + setOf("present", "detected", "overexpression")

    val BROAD_NEGATIVE_TERMS = setOf("negative", "absent", "loss")

    val LOSS_TERMS = setOf("loss")
    val NO_LOSS_TERMS = setOf("no loss", "positive", "normal", "normal expression")

    val WILD_TYPE_TERMS = setOf("wildtype", "wild-type", "wild type")

    val LOW_TERMS = setOf("low")
}