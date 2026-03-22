package dev.tanvd.kcli.di

fun di(init: DIBuilder.() -> Unit): DIContainer {
    return DIBuilder().apply(init).build()
}

inline fun <reified T : Any> DIContainer.get(name: String = T::class.simpleName ?: "Unknown"): T {
    return get(DIKey.of<T>(name))
}
