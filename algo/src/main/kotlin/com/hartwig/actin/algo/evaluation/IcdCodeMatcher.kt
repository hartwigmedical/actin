package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.IcdCodeHolder
import com.hartwig.actin.icd.IcdModel

interface IcdCodeMatcher {
    fun <T : IcdCodeHolder> findInstancesMatchingAnyIcdCode(
        icdModel: IcdModel,
        instances: List<T>?,
        targetIcdCodes: Set<IcdCode>
    ): IcdMatches<T> {

        val (fullMatches, unknownExtensionMatches) = if (instances == null) {
            Pair(emptyList(), emptyList())
        } else {
            targetIcdCodes.fold(Pair(emptyList<T>(), emptyList<T>())) { acc, targetCode ->
                val (fullMatch, unknownMatch) = returnIcdMatches(icdModel, targetCode, instances)
                Pair(acc.first + fullMatch, acc.second + unknownMatch)
            }
        }

        return object : IcdMatches<T> {
            override val fullMatches: List<T> = fullMatches
            override val mainCodeMatchesWithUnknownExtension: List<T> = unknownExtensionMatches
        }
    }

    private fun <T : IcdCodeHolder> returnIcdMatches(icdModel: IcdModel, targetCode: IcdCode, instances: List<T>): Pair<List<T>, List<T>> {
        val mainMatches = instances.filter { instance ->
            icdModel.returnCodeWithParents(instance.icdCode.mainCode).any(targetCode.mainCode::equals)
        }

        return when {
            targetCode.extensionCode == null -> Pair(mainMatches, emptyList())

            else -> {
                val unknownExtension = mainMatches.filter { it.icdCode.extensionCode == null }
                val extensionMatches = mainMatches.filter { instance ->
                    icdModel.returnCodeWithParents(instance.icdCode.extensionCode).any(targetCode.extensionCode::equals)
                }
                Pair(extensionMatches, unknownExtension)
            }
        }
    }
}