package studio.magemonkey.codex.util.eval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import studio.magemonkey.codex.util.eval.javaluator.DoubleEvaluator;
import studio.magemonkey.codex.util.eval.javaluator.StaticVariableSet;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A unary minus right after {@code ^} must evaluate correctly (never a silently wrong number), in both evaluators. */
class NegativeExponentTest {
    private static final double X = 30;

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of("2^-1", 0.5),
                Arguments.of("x * 2^-1", 15.0),
                Arguments.of("x*2^-1", 15.0),
                Arguments.of("2^-x", Math.pow(2, -X)),
                Arguments.of("x + 2^-1", X + 0.5),
                Arguments.of("x - 2^-1", X - 0.5),
                Arguments.of("1 + x * 2^-1 * 2", 1 + X),
                Arguments.of("2^-1 * x", 15.0),
                Arguments.of("2^--1", 2.0),
                Arguments.of("2^---1", 0.5),
                Arguments.of("2^-(1+1)", 0.25),
                Arguments.of("2^-1^2", 0.5),
                Arguments.of("2^-3^2", Math.pow(2, -9)),
                Arguments.of("-2^2", -4.0),
                Arguments.of("-2^-2", -0.25),
                Arguments.of("x * -2^2", -120.0),
                Arguments.of("(2^-1)", 0.5),
                Arguments.of("max(2^-1, 0)", 0.5),
                Arguments.of("max(1, x * 2^-1)", 15.0),
                Arguments.of("x * 2^(-1)", 15.0),
                Arguments.of("2^(-1)", 0.5),
                Arguments.of("1 - -1", 2.0),
                Arguments.of("2 * -3", -6.0),
                Arguments.of("2 ^ - 1", 0.5),
                Arguments.of("4^-0.5", 0.5));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    void compiled(String expression, double expected) {
        assertEquals(expected, Evaluator.compile(expression, "x").eval(X), 1e-12, expression);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    void stringEvaluator(String expression, double expected) {
        StaticVariableSet<Double> vars = new StaticVariableSet<>();
        vars.set("x", X);
        assertEquals(expected, new DoubleEvaluator().evaluate(expression, vars), 1e-12, expression);
    }

    @Test
    void excelStyleUnchanged() {
        DoubleEvaluator excel = new DoubleEvaluator(DoubleEvaluator.getDefaultParameters(DoubleEvaluator.Style.EXCEL));
        assertEquals(4.0, excel.evaluate("-2^2"), 0);
        assertEquals(15.0, excel.evaluate("30 * 2^-1"), 1e-12);
        assertEquals(0.5, excel.evaluate("2^-1"), 1e-12);
    }
}
