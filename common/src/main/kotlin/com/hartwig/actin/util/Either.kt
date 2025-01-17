package com.hartwig.actin.util

sealed class Either<out L, out R>(val isLeft: Boolean) {
    data class Left<L>(val left: L) : Either<L, Nothing>(true)
    data class Right<R>(val right: R) : Either<Nothing, R>(false)

    fun <R2> map(f: (R) -> R2): Either<L, R2> = flatMap { Right(f(it)) }
}

fun <L, R> Either<L, R>.getOrNull(): R? = when (this) {
    is Either.Left -> null
    is Either.Right -> this.right
}

fun <L, R> Either<L, R>.leftOrNull(): L? = when (this) {
    is Either.Left -> this.left
    is Either.Right -> null
}

fun <L, R, R2> Either<L, R>.flatMap(fn: (R) -> Either<L, R2>) = when (this) {
    is Either.Left -> this
    is Either.Right -> fn(this.right)
}

fun <L, R> Iterable<Either<L, R>>.partitionAndJoin(): Pair<List<L>, List<R>> {
    val (lefts, rights) = partition { it.isLeft }
    return lefts.mapNotNull(Either<L, R>::leftOrNull) to rights.mapNotNull(Either<L, R>::getOrNull)
}

fun <A> A.left(): Either<A, Nothing> = Either.Left(this)

fun <A> A.right(): Either<Nothing, A> = Either.Right(this)
