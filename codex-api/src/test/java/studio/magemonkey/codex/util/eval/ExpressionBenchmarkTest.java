package studio.magemonkey.codex.util.eval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Runs a short pass of {@link ExpressionBenchmark}; checks the three ways of computing the formula agree. No timing. */
class ExpressionBenchmarkTest {
    @Test
    void allThreeModesComputeTheSameSum() {
        double[][] in = ExpressionBenchmark.inputs();
        long string   = Double.doubleToLongBits(ExpressionBenchmark.run("string", 3000, in));
        long compiled = Double.doubleToLongBits(ExpressionBenchmark.run("compiled", 3000, in));
        long java     = Double.doubleToLongBits(ExpressionBenchmark.run("java", 3000, in));
        assertEquals(java, compiled);
        assertEquals(java, string);
    }
}
