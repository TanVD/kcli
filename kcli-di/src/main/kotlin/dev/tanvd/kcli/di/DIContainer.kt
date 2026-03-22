package dev.tanvd.kcli.di

/**
 * Simple dependency injection container without reflection
 */
class DIContainer {
    private val values = mutableMapOf<DIKey<*>, DIValue<*>>()
    private val cache = mutableMapOf<DIKey<*>, Any?>()

    protected fun value(key: DIKey<*>, value: DIValue<*>) {
        values[key] = value
    }

    fun <T> value(key: DIKey<T>, value: () -> T) {
        require(values[key] == null) { "Cannot register a new value for key: ${key.name}" }
        value(key, DIValue(key, emptyList()) { value() })
    }

    fun <T, D> value(key: DIKey<T>, dependency: DIKey<D>, value: (D) -> T) {
        require(values[key] == null) { "Cannot register a new value for key: ${key.name}" }
        value(key, DIValue(key, listOf(dependency)) { value(it[dependency] as D) })
    }

    fun <T, D1, D2> value(key: DIKey<T>, dependency1: DIKey<D1>, dependency2: DIKey<D2>, value: (D1, D2) -> T) {
        require(values[key] == null) { "Cannot register a new value for key: ${key.name}" }
        value(
            key,
            DIValue(key, listOf(dependency1, dependency2)) { value(it[dependency1] as D1, it[dependency2] as D2) })
    }

    fun <T, D1, D2, D3> value(
        key: DIKey<T>,
        dependency1: DIKey<D1>,
        dependency2: DIKey<D2>,
        dependency3: DIKey<D3>,
        value: (D1, D2, D3) -> T
    ) {
        require(values[key] == null) { "Cannot register a new value for key: ${key.name}" }
        value(
            key,
            DIValue(key, listOf(dependency1, dependency2, dependency3)) {
                value(
                    it[dependency1] as D1,
                    it[dependency2] as D2,
                    it[dependency3] as D3
                )
            })
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: DIKey<T>): T {
        return cache.getOrPut(key) {
            (values[key] ?: error("Unable to find the provider for $key")).value(this)
        } as T
    }

    fun isNull(key: DIKey<*>): Boolean {
        return get(key) == null
    }

    fun reset(key: DIKey<*>) {
        var toReset = listOf(key)
        while (toReset.isNotEmpty()) {
            toReset.forEach { cache.remove(it) }
            toReset = values.filter { it.value.dependencies.intersect(toReset).isNotEmpty() }.map { it.key }
        }
    }

    fun <T> override(key: DIKey<T>, value: () -> T) {
        require(values[key] != null) { "Cannot override non-existent singleton: ${key.name}" }
        override(key, DIValue(key, emptyList()) { value() })
    }

    protected fun override(key: DIKey<*>, value: DIValue<*>) {
        values[key] = value
        reset(key)
    }

    fun <T> has(key: DIKey<T>): Boolean {
        return values.containsKey(key)
    }

    fun extend(other: DIContainer, override: Boolean = true): DIContainer {
        for ((key, value) in other.values) {
            require(values[key] == null || override) { "Cannot register a new value for key: ${key.name}" }
            if (values[key] == null) {
                value(key, value)
            } else {
                override(key, value)
            }
        }
        return this
    }
}
