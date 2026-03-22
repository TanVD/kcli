package dev.tanvd.kcli.di

class DIBuilder {
    val container = DIContainer()

    inline fun <reified T> singleton(
        name: String = T::class.simpleName ?: "Unknown",
        noinline provider: () -> T
    ) {
        container.value(DIKey.of<T>(name), provider)
    }

    inline fun <reified T, reified D> singleton(
        name: String = T::class.simpleName ?: "Unknown",
        dependencyName: String = D::class.simpleName ?: "Unknown",
        noinline provider: (D) -> T
    ) {
        container.value(DIKey.of<T>(name), DIKey.of<D>(dependencyName), provider)
    }

    inline fun <reified T, reified D1, reified D2> singleton(
        name: String = T::class.simpleName ?: "Unknown",
        dependency1Name: String = D1::class.simpleName ?: "Unknown",
        dependency2Name: String = D2::class.simpleName ?: "Unknown",
        noinline provider: (D1, D2) -> T
    ) {
        container.value(
            DIKey.of<T>(name),
            DIKey.of<D1>(dependency1Name),
            DIKey.of<D2>(dependency2Name),
            provider
        )
    }

    inline fun <reified T> override(
        name: String = T::class.simpleName ?: "Unknown",
        noinline provider: () -> T
    ) {
        container.override(DIKey.of<T>(name), provider)
    }

    fun build(): DIContainer = container
}
