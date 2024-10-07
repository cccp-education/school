package org.example

class Library {
    fun someLibraryMethod(): Boolean = true

    companion object {
        @JvmStatic
        fun main(args: Array<String>) = Library()
            .run(Library::someLibraryMethod)
            .let(::println)
    }
}
