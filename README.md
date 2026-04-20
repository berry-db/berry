<p align="center">
    <img src="src/main/resources/logo.svg" alt="logo" width="10%"/>
</p>

<h1 align="center">Berry DB</h1>
<p align="center">A database. That's it. Why are you still reading? Stop it!</p>

## Example

```kotlin
fun main() {
    val db = Berry.open("database.bdb")

    println(db.get<User> { it.username.contains("ab") })
}
```

## Schemas

Berry comes with schemas - Simple and easy to understand. It eliminates user error when it comes to creating the data
classes.

```
database<Example> {
    table<User> {
        id:       number [primary]
        username: string
        email:    string
        password: string
    }

    table<Post> {
        id:       number [primary]
        content:  string
        author:   User
    }
}
```
