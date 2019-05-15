import de.sscholz.util.Coordi
import de.sscholz.util.i
import de.sscholz.util.printl
import de.sscholz.util.rearrangeNewlines
import org.junit.Test

class FooTest {

    @Test
    fun testHandler() {
        assert(false)
    }

    @Test
    fun `foo`() {
        print((-0.6f).i)
    }

    @Test
    fun `test coordi`() {
        val a = Coordi(2, 3)
        printl(a * 2, a > a * 2, a - Coordi(4, 4), a == Coordi(2, 3))
    }

    @Test
    fun `test newlines`() {
        val s = """
            a
            b
            c

            ddd

            eee


            ffff


            gggg
        """.trimIndent()
        printl(s.rearrangeNewlines())
    }
}