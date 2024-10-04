package org.example

class Library {
    fun someLibraryMethod(): Boolean {
        return true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit {
            Library()
                .run(Library::someLibraryMethod)
                .let(::println)
        }
    }
}
