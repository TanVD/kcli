package dev.tanvd.kcli.di

data class DIKey<T>(val name: String) {
    companion object {
        inline fun <reified T> of(name: String = T::class.simpleName ?: "Unknown"): DIKey<T> = DIKey(name)
    }
}
