import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.tensorflow.Graph

class TfTest {
    @Test
    fun `my first tensorflow test`() {
        assertThat(1).isEqualTo(1)

    }


    @Test
    fun graphDefRoundTrip() {
        val g = Graph()

    }
}