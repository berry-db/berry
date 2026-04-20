package dev.lyranie.berry

import dev.lyranie.berry.table.Post
import dev.lyranie.berry.table.User

// FOR TESTING ONLY; WILL BE REMOVED
fun main() {
    val db = Berry.open("test.bdb")

    for (i in 0 until 100) {
        db.insert(User(i, "$i", "$i", "$i"))
        db.insert(Post(i, "$i", i))
    }

    println(db.get<User>())
    println(db.get<Post>())
}
