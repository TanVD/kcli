package dev.tanvd.kcli.di

data class DIValue<T>(
    val key: DIKey<T>,
    val dependencies: List<DIKey<*>>,
    val provider: (dependencies: Map<DIKey<*>, Any>) -> T,
) {
    @Suppress("UNCHECKED_CAST")
    fun value(container: DIContainer): T {
        val deps = dependencies.associateWith {
            container.get(it as DIKey<Any>)
        }
        return provider(deps)
    }
}
