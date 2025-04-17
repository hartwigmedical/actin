package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import java.util.function.Predicate

private fun List<String>.joinWithConjunction(conjunction: String): String = when (size) {
    0 -> ""
    1 -> plural(this[0])
    2 -> "${plural(this[0])} $conjunction ${plural(this[1])}"
    else -> dropLast(1).joinToString(", ") { plural(it) } + " $conjunction " + plural(last())
}

private fun plural(str: String) = str + "s"

fun any(messagePrefix: String? = null) = or(*MolecularTestTarget.entries.toTypedArray(), messagePrefix = messagePrefix)

fun all(messagePrefix: String) = and(*MolecularTestTarget.entries.toTypedArray(), messagePrefix = messagePrefix)

fun atLeast(target: MolecularTestTarget, messagePrefix: String) = and(target, messagePrefix = messagePrefix)

fun and(vararg targets: MolecularTestTarget, messagePrefix: String) =
    combine(targets.toSet(), messagePrefix = messagePrefix, Predicate<List<MolecularTestTarget>>::and) {
        it.map { t -> t.name.lowercase() }.joinWithConjunction("and")
    }

fun or(vararg targets: MolecularTestTarget, messagePrefix: String? = null): TargetCoveragePredicate =
    combine(targets.toSet(), messagePrefix = messagePrefix, Predicate<List<MolecularTestTarget>>::or) {
        it.map { t -> t.name.lowercase() }.joinWithConjunction("or")
    }

private fun combine(
    targets: Set<MolecularTestTarget>,
    messagePrefix: String? = null,
    reducer: (Predicate<List<MolecularTestTarget>>, Predicate<List<MolecularTestTarget>>) -> Predicate<List<MolecularTestTarget>>,
    stringify: (Set<MolecularTestTarget>) -> String,
): TargetCoveragePredicate = TargetCoveragePredicate(
    targets,
    targets.map { target -> Predicate<List<MolecularTestTarget>> { it.contains(target) } }.reduce(reducer),
    stringify,
    messagePrefix
)

class TargetCoveragePredicate(
    private val targets: Set<MolecularTestTarget>,
    private val predicate: Predicate<List<MolecularTestTarget>>,
    private val stringify: (Set<MolecularTestTarget>) -> String,
    private val messagePrefix: String? = null
) :
    Predicate<List<MolecularTestTarget>> {

    override fun test(t: List<MolecularTestTarget>) = predicate.test(t)

    fun message(gene: String): String {
        return "${if (messagePrefix != null) "$messagePrefix " else ""}$gene undetermined (not tested for ${stringify.invoke(targets)})"
    }
}