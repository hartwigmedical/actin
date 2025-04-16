package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import java.util.function.Predicate

fun List<String>.joinWithConjunction(conjunction: String): String = when (size) {
    0 -> ""
    1 -> plural(this[0])
    2 -> "${plural(this[0])} $conjunction ${plural(this[1])}"
    else -> dropLast(1).joinToString(", ") { plural(it) } + " $conjunction " + plural(last())
}

fun plural(str: String) = str + "s"

fun any() = or(*MolecularTestTarget.entries.toTypedArray())

fun all() = and(*MolecularTestTarget.entries.toTypedArray())

fun atLeast(target: MolecularTestTarget) = and(target)

fun and(vararg targets: MolecularTestTarget) = combine(targets.toSet(), Predicate<List<MolecularTestTarget>>::and) {
    it.map { t -> t.name.lowercase() }.joinWithConjunction("and")
}

fun or(vararg targets: MolecularTestTarget): TargetCoveragePredicate = combine(targets.toSet(), Predicate<List<MolecularTestTarget>>::or) {
    it.map { t -> t.name.lowercase() }.joinWithConjunction("or")
}

private fun combine(
    targets: Set<MolecularTestTarget>,
    reducer: (Predicate<List<MolecularTestTarget>>, Predicate<List<MolecularTestTarget>>) -> Predicate<List<MolecularTestTarget>>,
    stringify: (Set<MolecularTestTarget>) -> String,
): TargetCoveragePredicate = TargetCoveragePredicate(
    targets, targets.map { target -> Predicate<List<MolecularTestTarget>> { it.contains(target) } }.reduce(reducer), stringify
)

class TargetCoveragePredicate(
    private val targets: Set<MolecularTestTarget>,
    private val predicate: Predicate<List<MolecularTestTarget>>,
    private val stringify: (Set<MolecularTestTarget>) -> String,
) :
    Predicate<List<MolecularTestTarget>> {

    override fun test(t: List<MolecularTestTarget>) = predicate.test(t)

    override fun toString(): String {
        return stringify.invoke(targets)
    }
}