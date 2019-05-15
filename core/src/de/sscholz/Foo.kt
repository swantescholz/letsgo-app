package de.sscholz

import de.sscholz.util.printl


class A {
    var x = 1
    fun f(p: ArrayList<Int> = ArrayList()) {
        p.add(2)
        printl(p)
    }
}

fun main() {
    val a = A()
    a.f()
    a.x = 4
    a.f()
}
