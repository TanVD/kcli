# kcli-di

Reflection-free dependency injection container. No annotation processing, no code generation, no runtime reflection. All
wiring is explicit Kotlin code.

Package: `dev.tanvd.kcli.di`

## Core Types

### DIKey

```kotlin
data class DIKey<T>(val name: String) {
    companion object {
        inline fun <reified T> of(name: String = T::class.simpleName ?: "Unknown"): DIKey<T>
    }
}
```

A typed key used to register and retrieve values. The name defaults to the simple class name of `T`. Two keys are equal
when their names match — the type parameter is erased at runtime.

Use named keys to register multiple instances of the same type:

```kotlin
val primaryDb = DIKey.of<Database>("primaryDb")
val replicaDb = DIKey.of<Database>("replicaDb")
```

### DIValue

```kotlin
data class DIValue<T>(
    val key: DIKey<T>,
    val dependencies: List<DIKey<*>>,
    val provider: (dependencies: Map<DIKey<*>, Any>) -> T,
)
```

A lazy factory. `value(container)` resolves each declared dependency from the container, then calls `provider`. Not
typically constructed directly; use `DIContainer.value()` or `DIBuilder.singleton()`.

### DIContainer

```kotlin
class DIContainer
```

Holds registrations (`DIValue` per `DIKey`) and a cache of already-instantiated values. Values are singletons: each
key's provider is called at most once per container instance.

## DIContainer API

### Registering values

```kotlin
fun <T> value(key: DIKey<T>, value: () -> T)
fun <T, D> value(key: DIKey<T>, dependency: DIKey<D>, value: (D) -> T)
fun <T, D1, D2> value(key: DIKey<T>, dependency1: DIKey<D1>, dependency2: DIKey<D2>, value: (D1, D2) -> T)
fun <T, D1, D2, D3> value(
    key: DIKey<T>,
    dependency1: DIKey<D1>,
    dependency2: DIKey<D2>,
    dependency3: DIKey<D3>,
    value: (D1, D2, D3) -> T
)
```

Registers a factory for `key`. Throws `IllegalArgumentException` if a registration already exists for that key (use
`override` to replace one).

```kotlin
val container = DIContainer()
container.value(DIKey.of<Config>()) { Config.load() }
container.value(DIKey.of<Database>(), DIKey.of<Config>()) { cfg -> Database(cfg.url) }
```

### Retrieving values

```kotlin
fun <T> get(key: DIKey<T>): T
fun isNull(key: DIKey<*>): Boolean
fun has(key: DIKey<*>): Boolean
```

`get` resolves dependencies recursively, caches the result, and returns it. Throws `IllegalStateException` if no
registration exists for the key.

`has` checks whether a key is registered (does not instantiate).

`isNull` instantiates the value and checks whether it is `null`.

```kotlin
val db = container.get(DIKey.of<Database>())
```

The reified extension (from `Extensions.kt`) is preferred:

```kotlin
val db = container.get<Database>()
val named = container.get<Database>("replicaDb")
```

### Overriding values

```kotlin
fun <T> override(key: DIKey<T>, value: () -> T)
```

Replaces the factory for an existing key. Throws `IllegalArgumentException` if the key is not registered. After
overriding, `reset` is called on the key and all dependents automatically.

```kotlin
container.override(DIKey.of<Database>()) { TestDatabase() }
```

### Resetting cached values

```kotlin
fun reset(key: DIKey<*>)
```

Evicts the cached instance for `key` and recursively evicts all values that depend on it (transitively). The next `get`
call will re-invoke providers.

```kotlin
container.reset(DIKey.of<Config>())
// Config and everything that depends on it will be recreated on next get()
```

### Extending containers

```kotlin
fun extend(other: DIContainer, override: Boolean = true): DIContainer
```

Merges all registrations from `other` into the receiver. When `override = true` (default), existing keys are replaced;
when `false`, existing keys are preserved and a conflict throws `IllegalArgumentException`. Returns `this`.

```kotlin
val base = di { singleton<Config> { Config.load() } }
val extended = di {
    singleton<Database, Config> { cfg -> Database(cfg.url) }
}.also { it.extend(base) }
```

## DIBuilder DSL

`DIBuilder` provides reified `singleton` and `override` functions for ergonomic registration.

```kotlin
class DIBuilder {
    inline fun <reified T> singleton(name: String = ..., noinline provider: () -> T)
    inline fun <reified T, reified D> singleton(name: String = ..., dependencyName: String = ..., noinline provider: (D) -> T)
    inline fun <reified T, reified D1, reified D2> singleton(name: String = ..., dependency1Name: String = ..., dependency2Name: String = ..., noinline provider: (D1, D2) -> T)
    inline fun <reified T> override(name: String = ..., noinline provider: () -> T)
    fun build(): DIContainer
}
```

Name parameters default to the simple class name of each type parameter.

## di {} Entry Point

```kotlin
fun di(init: DIBuilder.() -> Unit): DIContainer
```

Creates a `DIBuilder`, applies `init`, and returns the built `DIContainer`.

```kotlin
val container = di {
    singleton<Config> { Config.load() }
    singleton<Database, Config> { cfg -> Database(cfg.url) }
    singleton<UserRepository, Database> { db -> UserRepository(db) }
    singleton { repo: UserRepository, cfg: Config ->
        UserService(repo, cfg.adminEmail)
    }
}
```

## get() Extension

```kotlin
inline fun <reified T : Any> DIContainer.get(name: String = T::class.simpleName ?: "Unknown"): T
```

Type-inferred retrieval. Resolves `DIKey.of<T>(name)` from the container.

```kotlin
val service = container.get<UserService>()
val primary = container.get<Database>("primaryDb")
```

## DIGlobal

```kotlin
val DIGlobal: DIContainer = di { }
```

An empty global container. Extend it at application startup with your registrations:

```kotlin
DIGlobal.extend(di {
    singleton<Config> { Config.load() }
    singleton<Database, Config> { cfg -> Database(cfg.url) }
})

// Later, anywhere:
val db = DIGlobal.get<Database>()
```

## Named Registrations

When multiple instances of the same type are needed, supply explicit names to both `singleton` and `get`:

```kotlin
val container = di {
    singleton<Database>("primaryDb")  { Database(primaryUrl) }
    singleton<Database>("replicaDb")  { Database(replicaUrl) }
    singleton<Database>("testDb")     { Database("jdbc:h2:mem:test") }

    // depend on a named instance explicitly
    singleton(
        name = "UserRepository",
        dependencyName = "primaryDb"
    ) { db: Database -> UserRepository(db) }
}

val primary = container.get<Database>("primaryDb")
val replica = container.get<Database>("replicaDb")
```

Key identity is the `name` string only — the type parameter is informational at the Kotlin level but erased at runtime.

## Overrides in Tests

```kotlin
class MyServiceTest {
    @Test
    fun `uses stub database`() {
        val container = di {
            singleton<Database> { ProductionDatabase() }
            singleton<UserService, Database> { db -> UserService(db) }
            override<Database> { StubDatabase() }
        }

        val service = container.get<UserService>()
        // service.db is StubDatabase
    }
}
```

`override` inside the `di {}` block calls `DIBuilder.override`, which requires the key to exist at the time of the call.
Register with `singleton` first, then `override`.

At runtime, after the container is built:

```kotlin
container.override(DIKey.of<Database>()) { StubDatabase() }
// automatically resets Database and all dependents
val service = container.get<UserService>()  // picks up new Database
```

## Singleton Semantics

Each key's provider is invoked once; the result is cached. Subsequent `get` calls return the same instance.

```kotlin
val container = di {
    singleton<Counter> { Counter() }
}

val a = container.get<Counter>()
val b = container.get<Counter>()
assert(a === b)  // same instance
```

Cache is per-container. Extending containers does not share caches — each container caches independently.
