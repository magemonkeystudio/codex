package studio.magemonkey.codex.util.eval;

import studio.magemonkey.codex.util.eval.javaluator.DoubleEvaluator;
import studio.magemonkey.codex.util.eval.javaluator.StaticVariableSet;

import java.util.Arrays;

/**
 * Compares three ways of computing the damage formula: the string evaluator, a {@link CompiledExpression}, and the
 * same arithmetic written in Java. Not a JUnit test on purpose: run it with
 * <pre>
 * java -cp codex-api/target/classes:codex-api/target/test-classes:&lt;spigot-api and deps&gt; \
 *      studio.magemonkey.codex.util.eval.ExpressionBenchmark [string|compiled|java] [rounds] [iterations]
 * </pre>
 * Run one mode per JVM so the JIT profile of one does not shape another. {@code ExpressionBenchmarkTest} runs a tiny
 * pass of it in every build to check the three agree.
 */
public final class ExpressionBenchmark {
    static final String FORMULA = "max(d / 5, d - 4 * x / max(1, t + 8))";

    private static final int INPUTS = 1024; // power of two, so the index wraps with a mask

    private ExpressionBenchmark() {
    }

    static double handWritten(double d, double x, double t) {
        return Math.max(d / 5, d - 4 * x / Math.max(1, t + 8));
    }

    /** The values every mode evaluates, so their sums can be compared. */
    static double[][] inputs() {
        double[][] in = new double[3][INPUTS];
        java.util.Random r = new java.util.Random(42);
        for (int i = 0; i < INPUTS; i++) {
            in[0][i] = 1 + r.nextInt(2000) / 10.0; // damage
            in[1][i] = r.nextInt(200) / 4.0;       // x
            in[2][i] = r.nextInt(120);             // toughness
        }
        return in;
    }

    /** One pass of {@code iterations} evaluations. Returns the sum of results (which also keeps the JIT honest). */
    static double run(String mode, int iterations, double[][] in) {
        double sum = 0;
        switch (mode) {
            case "string": {
                DoubleEvaluator evaluator = new DoubleEvaluator();
                StaticVariableSet<Double> vars = new StaticVariableSet<>();
                for (int i = 0; i < iterations; i++) {
                    int k = i & (INPUTS - 1);
                    vars.set("d", in[0][k]);
                    vars.set("x", in[1][k]);
                    vars.set("t", in[2][k]);
                    sum += evaluator.evaluate(FORMULA, vars);
                }
                return sum;
            }
            case "compiled": {
                CompiledExpression ce = Evaluator.compile(FORMULA, "d", "x", "t");
                for (int i = 0; i < iterations; i++) {
                    int k = i & (INPUTS - 1);
                    sum += ce.eval(in[0][k], in[1][k], in[2][k]);
                }
                return sum;
            }
            case "java": {
                for (int i = 0; i < iterations; i++) {
                    int k = i & (INPUTS - 1);
                    sum += handWritten(in[0][k], in[1][k], in[2][k]);
                }
                return sum;
            }
            default:
                throw new IllegalArgumentException("mode must be string, compiled or java");
        }
    }

    private static double measure(String mode, int rounds, int iterations, double[][] in) {
        double[] nanos = new double[rounds];
        double   sink  = 0;
        for (int r = 0; r < rounds; r++) {
            long start = System.nanoTime();
            sink += run(mode, iterations, in);
            nanos[r] = (System.nanoTime() - start) / (double) iterations;
        }
        Arrays.sort(nanos);
        System.out.printf("%-9s median %9.2f ns/op   min %9.2f   max %9.2f   (%d rounds x %,d ops, checksum %.6f)%n",
                mode, nanos[rounds / 2], nanos[0], nanos[rounds - 1], rounds, iterations, sink);
        return nanos[rounds / 2];
    }

    public static void main(String[] args) {
        String   mode       = args.length > 0 ? args[0] : "compiled";
        int      rounds     = args.length > 1 ? Integer.parseInt(args[1]) : 15;
        int      iterations = args.length > 2 ? Integer.parseInt(args[2]) : "string".equals(mode) ? 200_000 : 20_000_000;
        double[][] in = inputs();
        // Warm up: let the JIT compile everything before the rounds that count.
        for (int i = 0; i < 5; i++) {
            run(mode, iterations / 4, in);
        }
        measure(mode, rounds, iterations, in);
    }
}
