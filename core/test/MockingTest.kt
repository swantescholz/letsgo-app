import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class Dependency1(val value1: Int)
class Dependency2(val value2: String)

class SystemUnderTest(
        val dependency1: Dependency1,
        val dependency2: Dependency2
) {
    fun calculate() =
            dependency1.value1 + dependency2.value2.toInt()
}

class MockingTest {
    @Test
    fun calculateAddsValues() {
        val doc1 = mockk<Dependency1>()
        val doc2 = mockk<Dependency2>()

        every { doc1.value1 } returns 5
        every { doc2.value2 } returns "6"

        val sut = SystemUnderTest(doc1, doc2)
        assertThat(12).isEqualTo(sut.calculate())
    }
}