package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import java.util.function.Predicate

class TargetPredicate(val targets: Set<MolecularTestTarget>, private val predicate: Predicate<List<MolecularTestTarget>>) :
    Predicate<List<MolecularTestTarget>> {

    override fun test(t: List<MolecularTestTarget>) = predicate.test(t)

    companion object {
        fun any() = or(*MolecularTestTarget.entries.toTypedArray())

        fun all() = and(*MolecularTestTarget.entries.toTypedArray())

        fun exactly(target: MolecularTestTarget) = and(target)

        fun and(vararg targets: MolecularTestTarget) = combine(targets.toList(), Predicate<List<MolecularTestTarget>>::and)

        fun or(vararg targets: MolecularTestTarget): TargetPredicate = combine(targets.toList(), Predicate<List<MolecularTestTarget>>::or)

        private fun combine(
            targets: List<MolecularTestTarget>,
            reducer: (Predicate<List<MolecularTestTarget>>, Predicate<List<MolecularTestTarget>>) -> Predicate<List<MolecularTestTarget>>
        ): TargetPredicate = TargetPredicate(targets.toSet(),
            targets.map { target -> Predicate<List<MolecularTestTarget>> { it.contains(target) } }
                .reduce(reducer)
        )
    }
}