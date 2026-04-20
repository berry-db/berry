/*
 * Copyright (c) 2026 lyranie
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
