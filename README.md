# KBuilder

[![CircleCI](https://circleci.com/gh/Jintin/KBuilder.svg?style=shield)](https://app.circleci.com/pipelines/github/Jintin/KBuilder)

KBuilder is a library to generate Builder pattern automatically vis ksp(Kotlin Symbol Processing)

## Usage

Any classes that store values via primary constructor like data class does are eligible to enable
this feature. Just simply add `@KBuilder` to the class and after successfully compile the Builder
class and the extension function to generate the builder will create for you.

### Make IDE aware of ksp classes

Currently your IDE might not able to reason about the generated code under ksp folder, add this
block in your module level's build.gradle or any other way to include `build/generated/ksp/` path.

```groovy
sourceSets {
    main {
        java {
            srcDir "${buildDir.absolutePath}/generated/ksp/"
        }
    }
}
```

More info here : https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code

### Example

If you have a `MyObj` class like below:

```kotlin
@KBuilder
data class MyObj(
    val value1: String
)
```

Then you can use the builder pattern with auto-generated class `MyObjBuilder` without any extra
effort after compile:

```kotlin
val obj = MyObj("init value")
val newObj = obj.toBuilder().apply {
    this.value1 = "updated value"
}.build()
println(newObj.value1) // "updated value"

val builder = MyObjBuilder()
builder.value1 = "dynamic value"
val newObj2 = builder.build()
println(newObj2.value1) // "dynamic value"
```

## Contributing

Bug reports and pull requests are welcome on GitHub at <https://github.com/Jintin/KBuilder>.

## License

The module is available as open source under the terms of
the [MIT License](http://opensource.org/licenses/MIT).
