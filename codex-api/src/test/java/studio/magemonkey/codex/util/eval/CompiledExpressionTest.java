package studio.magemonkey.codex.util.eval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import studio.magemonkey.codex.util.eval.javaluator.DoubleEvaluator;
import studio.magemonkey.codex.util.eval.javaluator.Function;
import studio.magemonkey.codex.util.eval.javaluator.StaticVariableSet;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CompiledExpressionTest {
    private static final String[] NAMES = {"d", "x", "t"};

    /** Value sets for d, x, t: ordinary, zero, negative, extreme, infinite, negative zero and NaN inputs. */
    private static final double[][] VALUE_SETS = {
            {10, 2, 3},
            {0, 0, 0},
            {-5.5, 7.25, -8},
            {100, 4, 0},
            {1e300, 1e-300, 1},
            {Double.MAX_VALUE, Double.MIN_VALUE, -1},
            {Double.POSITIVE_INFINITY, -1, 0.5},
            {-0.0, 0.0, -0.0},
            {0.1, 0.2, 0.3},
            {Double.NaN, 1, 2},
            {37.5, 0.5, 1234.5678},
            {-1, -1, -1},
    };

    private static final String FORMULA = "max(d / 5, d - 4 * x / max(1, t + 8))";

    private static final String[] EXPRESSIONS = {
            // + - * / and associativity
            "d+x+t", "d-x-t", "d-x+t", "d+x-t", "d/x/t", "d/x*t", "d*x/t", "d-x*t", "d+x*t", "d*x-t", "d/x-t",
            "5-3-2", "100/10/5", "d-x-t-d-x-t", "d/x/t/d/x",
            // brackets
            "(d+x)*t", "d*(x+t)*(d-t)", "((((d))))", "d-(x-t)", "d/(x/t)", "(d-x)-t", "((d+x)*(t-d))/(x+t)",
            // exponent: javaluator makes ^ left associative, and binds tighter than unary minus
            "d^x", "d^x^t", "2^3^2", "2^(-1)", "-d^2", "(-d)^2", "-d^(-x)", "x^0.5", "0^0", "-x^t^d", "d*x^t", "d^x*t",
            // modulo
            "d%x", "d%x%t", "d*x%t", "-d%x", "d%0", "d+x%t",
            // unary minus
            "-d", "--d", "-(-d)", "d- -x", "d--x", "d+-x", "2*-d", "-d*-x", "-(d+x)*t", "-0", "0*-d", "-x-t", "-(-(-d))",
            // constants
            "pi", "e", "pi*d", "e^x", "pi/e", "-pi", "pi-e-d",
            // functions
            "abs(x)", "abs(-d)", "min(d,x)", "max(d,x)", "max(d,x,t)", "min(d,x,t,1,2,3)", "max(max(d,x),min(x,t))",
            "min(d)", "max(d)", "sum(d,x,t)", "avg(d,x,t)", "sum(d)", "avg(d)", "sum(-0)", "min(-0,0)", "max(-0,0)",
            "round(d/3)", "round(-d/3)", "round(x*1000/7)", "ceil(x/3)", "floor(-x/3)", "ceil(d)", "floor(d)",
            "sqrt(abs(d))", "sin(d)+cos(x)", "tan(t)", "asin(x/d)", "acos(x/d)", "atan(d)", "sinh(x)", "cosh(x)",
            "tanh(x)", "ln(abs(d)+1)", "log(abs(d)+1)", "sqrt(d*d+x*x)", "min(max(d,x)-t,abs(min(-d,x*t)))",
            "round(0/0)", "round(d/0)", "max(0/0,1)",
            // whitespace
            "  d  +  x  ", "d\t+\tx", "max( d , x )", "( d )", " - d ", "max ( d , x )",
            // numbers
            "1000", "0.5*d", ".5*d", "5.*d", "007+d", "1234567.891*d", "123456789012345678901234567890*d",
            "0.1+0.2", "3.141592653589793238*d", "9007199254740993+d", "0.30000000000000004*d",
            "100000000000000000000000000000000000000000000000000*d", "0.000000000000000000000000000001*d", "0",
            // the damage formula and spellings of it
            FORMULA, "max(d/5,d-4*x/max(1,t+8))", "max(d / 5,\td - 4 * x / max( 1 , t + 8 ))",
            "d - 4 * x / max(1, t + 8)", "max(d/5, (d - 4*x) / max(1, t+8))",
    };

    static Stream<Arguments> expressions() {
        return Arrays.stream(EXPRESSIONS).map(Arguments::of);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Reference: DoubleEvaluator with a StaticVariableSet
    // ---------------------------------------------------------------------------------------------------------------

    private static final DoubleEvaluator REFERENCE = new DoubleEvaluator();

    /** DoubleEvaluator's result, or null if it threw because a function returned NaN. Any other failure propagates. */
    private static Double reference(String expression, String[] names, double[] values) {
        StaticVariableSet<Double> vars = new StaticVariableSet<>();
        for (int i = 0; i < names.length; i++) {
            vars.set(names[i], values[i]);
        }
        try {
            return REFERENCE.evaluate(expression, vars);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Invalid argument passed to ")) {
                return null;
            }
            throw e;
        }
    }

    private static String bits(double v) {
        return v + " (0x" + Long.toHexString(Double.doubleToRawLongBits(v)) + ")";
    }

    /**
     * Asserts the compiled form equals DoubleEvaluator bit for bit, wherever DoubleEvaluator gives a result.
     */
    private static void assertSameAsReference(CompiledExpression ce, String expression, String[] names, double[] values) {
        Double expected = reference(expression, names, values);
        double actual   = ce.eval(values);
        String message  = "'" + expression + "' with " + Arrays.toString(values);
        if (expected == null) {
            // DoubleEvaluator threw on a NaN function result. The compiled form carries on (round(NaN) is 0, for
            // one), so there is nothing to compare; it only must not throw.
            return;
        } else {
            // doubleToLongBits folds every NaN into one value; the sign of a NaN depends on how it was produced.
            assertEquals(Double.doubleToLongBits(expected), Double.doubleToLongBits(actual),
                    message + ": expected " + bits(expected) + " but got " + bits(actual));
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Differential tests
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void tableHasEnoughExpressions() {
        assertTrue(new HashSet<>(Arrays.asList(EXPRESSIONS)).size() >= 40);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("expressions")
    void matchesDoubleEvaluatorBitForBit(String expression) {
        CompiledExpression ce = Evaluator.compile(expression, NAMES);
        for (double[] values : VALUE_SETS) {
            assertSameAsReference(ce, expression, NAMES, values);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("expressions")
    void matchesWithVariablesInAnotherOrder(String expression) {
        // Position-to-name resolution has to follow the given list, not alphabetical or first-use order.
        String[] reversed = {"t", "x", "d"};
        CompiledExpression ce = Evaluator.compile(expression, reversed);
        for (double[] values : VALUE_SETS) {
            double[] swapped = {values[2], values[1], values[0]}; // t, x, d
            assertSameAsReference(ce, expression, reversed, swapped);
        }
    }

    @Test
    void damageFormulaMatchesHandWrittenJava() {
        CompiledExpression ce = Evaluator.compile(FORMULA, "d", "x", "t");
        for (double[] v : VALUE_SETS) {
            double d = v[0], x = v[1], t = v[2];
            double expected = Math.max(d / 5, d - 4 * x / Math.max(1, t + 8));
            assertEquals(Double.doubleToLongBits(expected), Double.doubleToLongBits(ce.eval(d, x, t)),
                    Arrays.toString(v));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"2^-1", "-d^-x", "d^-1"})
    void unaryMinusRightAfterExponentIsRejectedLikeDoubleEvaluatorDoes(String expression) {
        // The shared parser pops "^" when it meets a unary minus (lower precedence), leaving it one operand short.
        assertThrows(RuntimeException.class, () -> reference(expression, NAMES, VALUE_SETS[0]));
        assertMessage(compileFails(expression, NAMES), expression);
    }

    @Test
    void variablesNotUsedByTheExpressionAreFine() {
        CompiledExpression ce = Evaluator.compile("x * 2", "d", "x", "t");
        assertEquals(14, ce.eval(1, 7, 3));
    }

    @Test
    void expressionWithoutVariables() {
        CompiledExpression ce = Evaluator.compile("2 + 3 * 4");
        assertEquals(14, ce.eval());
        assertTrue(ce.getVariableNames().isEmpty());
    }

    @Test
    void longVariableNames() {
        CompiledExpression ce = Evaluator.compile("damage_taken - armor2 * Armor", "damage_taken", "armor2", "Armor");
        assertEquals(10 - 3 * 4, ce.eval(10, 3, 4));
    }

    @Test
    void variableNamesAreCaseSensitiveLikeFunctions() {
        assertEquals(5, Evaluator.compile("D + x", "D", "x").eval(2, 3));
        assertThrows(IllegalArgumentException.class, () -> Evaluator.compile("D + x", "d", "x"));
    }

    @Test
    void everyFunctionOfDoubleEvaluatorIsSupported() {
        // If a function is ever added to DoubleEvaluator, this fails until the compiled form learns it too.
        for (Function f : REFERENCE.getFunctions()) {
            int args = Math.max(f.getMinimumArgumentCount(), 1);
            if (f.getMaximumArgumentCount() == 0) {
                args = 0;
            }
            StringBuilder sb = new StringBuilder(f.getName()).append('(');
            for (int i = 0; i < args; i++) {
                sb.append(i == 0 ? "" : ",").append(i == 0 ? "d" : "x");
            }
            String expression = sb.append(')').toString();
            if (f.getMaximumArgumentCount() == Integer.MAX_VALUE) {
                expression = f.getName() + "(d,x,t)";
            }
            CompiledExpression ce = Evaluator.compile(expression, NAMES);
            if (!f.getName().equals("random")) {
                for (double[] values : VALUE_SETS) {
                    assertSameAsReference(ce, expression, NAMES, values);
                }
            }
        }
    }

    @Test
    void everyConstantOfDoubleEvaluatorIsSupported() {
        REFERENCE.getConstants().forEach(c -> {
            CompiledExpression ce = Evaluator.compile(c.getName());
            assertEquals(Double.doubleToLongBits(REFERENCE.evaluate(c.getName())), Double.doubleToLongBits(ce.eval()));
        });
    }

    @Test
    void everyOperatorOfDoubleEvaluatorIsSupported() {
        REFERENCE.getOperators().forEach(op -> {
            String expression = op.getOperandCount() == 1 ? op.getSymbol() + "d" : "d " + op.getSymbol() + " x";
            CompiledExpression ce = Evaluator.compile(expression, NAMES);
            for (double[] values : VALUE_SETS) {
                assertSameAsReference(ce, expression, NAMES, values);
            }
        });
    }

    @Test
    void randomGivesANewNumberEachCall() {
        CompiledExpression ce = Evaluator.compile("random()");
        Set<Double> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            double v = ce.eval();
            assertTrue(v >= 0 && v < 1, "random() out of range: " + v);
            seen.add(v);
        }
        assertTrue(seen.size() > 90, "random() should not repeat: " + seen.size());
        double scaled = Evaluator.compile("random() * 10 + d", "d").eval(100);
        assertTrue(scaled >= 100 && scaled < 110);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Seeded random expressions
    // ---------------------------------------------------------------------------------------------------------------

    private static final String[] UNARY_FUNCTIONS = {"abs", "sqrt", "ceil", "floor", "round", "sin", "cos", "tan",
            "atan", "asin", "acos", "sinh", "cosh", "tanh", "ln", "log"};
    private static final String[] VARIADIC        = {"min", "max", "sum", "avg"};
    private static final String[] OPERATORS       = {"+", "-", "*", "/", "^", "%"};

    private static String ws(Random r) {
        switch (r.nextInt(4)) {
            case 0:
                return " ";
            case 1:
                return "  ";
            case 2:
                return "\t";
            default:
                return "";
        }
    }

    private static String number(Random r) {
        switch (r.nextInt(6)) {
            case 0:
                return String.valueOf(r.nextInt(10));
            case 1:
                return String.valueOf(r.nextInt(1000));
            case 2:
                return r.nextInt(100) + "." + r.nextInt(1000);
            case 3:
                return "." + r.nextInt(100);
            case 4:
                return r.nextInt(100) + ".";
            default:
                return String.valueOf(r.nextLong() >>> r.nextInt(64));
        }
    }

    private static String atom(Random r) {
        switch (r.nextInt(7)) {
            case 0:
            case 1:
                return NAMES[r.nextInt(NAMES.length)];
            case 2:
                return r.nextBoolean() ? "pi" : "e";
            default:
                return number(r);
        }
    }

    private static String randomExpression(Random r, int depth) {
        if (depth <= 0 || r.nextInt(5) == 0) {
            return (r.nextInt(6) == 0 ? "-" + ws(r) : "") + atom(r);
        }
        switch (r.nextInt(7)) {
            case 0:
            case 1:
            case 2: {
                String left  = randomExpression(r, depth - 1);
                String right = randomExpression(r, depth - 1);
                return left + ws(r) + OPERATORS[r.nextInt(OPERATORS.length)] + ws(r) + right;
            }
            case 3:
                return "(" + ws(r) + randomExpression(r, depth - 1) + ws(r) + ")";
            case 4:
                return "-" + ws(r) + "(" + randomExpression(r, depth - 1) + ")";
            case 5:
                return UNARY_FUNCTIONS[r.nextInt(UNARY_FUNCTIONS.length)] + ws(r) + "(" + ws(r)
                        + randomExpression(r, depth - 1) + ws(r) + ")";
            default: {
                int           n  = 1 + r.nextInt(4);
                StringBuilder sb = new StringBuilder(VARIADIC[r.nextInt(VARIADIC.length)]).append('(');
                for (int i = 0; i < n; i++) {
                    sb.append(i == 0 ? "" : "," + ws(r)).append(randomExpression(r, depth - 1));
                }
                return sb.append(')').toString();
            }
        }
    }

    @Test
    void seededRandomExpressionsMatchDoubleEvaluator() {
        Random r = new Random(0xC0DE_2_1DL);
        int compared = 0, nanFunction = 0;
        for (int i = 0; i < 4000; i++) {
            String expression = randomExpression(r, 1 + r.nextInt(5));
            CompiledExpression ce;
            try {
                ce = Evaluator.compile(expression, NAMES);
            } catch (IllegalArgumentException e) {
                // The string evaluator must reject exactly what the compiler rejects.
                StaticVariableSet<Double> vars = new StaticVariableSet<>();
                for (int k = 0; k < NAMES.length; k++) {
                    vars.set(NAMES[k], VALUE_SETS[0][k]);
                }
                // Any exception counts: an expression that is malformed, or whose first NaN function result it rejects.
                assertThrows(RuntimeException.class, () -> REFERENCE.evaluate(expression, vars), expression);
                continue;
            }
            for (double[] values : VALUE_SETS) {
                if (reference(expression, NAMES, values) == null) {
                    nanFunction++;
                }
                assertSameAsReference(ce, expression, NAMES, values);
                compared++;
            }
        }
        // Guard against a generator that quietly stops producing valid input.
        assertTrue(compared > 40_000, "only " + compared + " comparisons ran");
        assertTrue(nanFunction > 0, "the NaN-function path was never exercised");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Errors
    // ---------------------------------------------------------------------------------------------------------------

    private static IllegalArgumentException compileFails(String expression, String... names) {
        return assertThrows(IllegalArgumentException.class, () -> Evaluator.compile(expression, names), expression);
    }

    private static void assertMessage(IllegalArgumentException e, String expression, String... fragments) {
        String message = e.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("'" + expression + "'"), "message should name the expression: " + message);
        assertFalse(message.contains("\n"), "message should be one line: " + message);
        for (String fragment : fragments) {
            assertTrue(message.contains(fragment), "message should mention '" + fragment + "': " + message);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"1 +", "* 2", "d d", "d x", "1 2", "d + * x", "d +", "()", "", "   ", "d,x", "(d,x)", ",",
            "max(,)", "max(d,)", "max(,d)", "d ^", "- ", "1..2", "1.2.3", "5d"})
    void syntaxErrorsAreRejected(String expression) {
        IllegalArgumentException e = compileFails(expression, "d", "x");
        assertMessage(e, expression);
        // And DoubleEvaluator rejects the same input, so nothing valid is turned away.
        StaticVariableSet<Double> vars = new StaticVariableSet<>();
        vars.set("d", 1.0);
        vars.set("x", 2.0);
        assertThrows(RuntimeException.class, () -> REFERENCE.evaluate(expression, vars));
    }

    @Test
    void syntaxErrorMessageSaysWhatIsWrong() {
        assertMessage(compileFails("d + * x", "d", "x"), "d + * x");
        assertMessage(compileFails("1.2.3"), "1.2.3", "invalid number '1.2.3'");
        assertMessage(compileFails("d d", "d"), "d d", "unknown variable or function 'd d'");
    }

    @Test
    void unknownVariableIsRejectedAndNamed() {
        IllegalArgumentException e = compileFails("d + y", "d");
        assertMessage(e, "d + y", "unknown variable or function 'y'");
        assertTrue(e.getMessage().contains("(at position "), e.getMessage());
    }

    @Test
    void unknownFunctionIsRejectedAndNamed() {
        assertMessage(compileFails("foo(d) + 1", "d"), "foo(d) + 1", "unknown variable or function 'foo'");
        assertMessage(compileFails("MAX(d, 1)", "d"), "MAX(d, 1)", "'MAX'");
    }

    @Test
    void variableUsedWithoutBeingDeclared() {
        assertMessage(compileFails("d * x"), "d * x", "'d'");
    }

    @Test
    void mismatchedBracketsAreRejected() {
        assertMessage(compileFails("(d + 1", "d"), "(d + 1", "mismatched");
        assertMessage(compileFails("d + 1)", "d"), "d + 1)", "mismatched");
        assertMessage(compileFails("max(d, 1", "d"), "max(d, 1", "mismatched");
        assertMessage(compileFails("((d)", "d"), "((d)", "mismatched");
        assertMessage(compileFails(")(", "d"), ")(", "close bracket");
        assertMessage(compileFails("max(d, 1))", "d"), "max(d, 1))", "mismatched");
    }

    @Test
    void wrongArgumentCountIsRejected() {
        assertMessage(compileFails("max()", "d"), "max()", "max", "at least 1", "got 0");
        assertMessage(compileFails("min()", "d"), "min()", "min", "got 0");
        assertMessage(compileFails("sin(d, 1)", "d"), "sin(d, 1)", "sin", "expected 1", "got 2");
        assertMessage(compileFails("abs()", "d"), "abs()", "abs", "expected 1", "got 0");
        assertMessage(compileFails("random(d)", "d"), "random(d)", "random", "expected 0", "got 1");
        assertMessage(compileFails("sqrt(1, 2, 3)", "d"), "sqrt(1, 2, 3)", "sqrt", "got 3");
        assertMessage(compileFails("1 + max(d, d) + atan(d, d)", "d"), "1 + max(d, d) + atan(d, d)", "atan", "got 2");
    }

    @Test
    void variableNamesThatCollideWithFunctionsOrConstantsAreRejected() {
        assertMessage(compileFails("1", "max"), "1", "'max'", "function");
        assertMessage(compileFails("1", "d", "abs"), "1", "'abs'", "function");
        assertMessage(compileFails("1", "pi"), "1", "'pi'", "constant");
        assertMessage(compileFails("1", "e"), "1", "'e'", "constant");
        for (Function f : REFERENCE.getFunctions()) {
            assertMessage(compileFails("1", f.getName()), "1", "'" + f.getName() + "'");
        }
        REFERENCE.getConstants().forEach(c -> assertMessage(compileFails("1", c.getName()), "1", "'" + c.getName() + "'"));
    }

    @Test
    void unusableVariableNamesAreRejected() {
        assertMessage(compileFails("1", ""), "1", "empty");
        assertMessage(compileFails("1", " "), "1", "empty");
        assertMessage(compileFails("1", " d"), "1", "whitespace");
        assertMessage(compileFails("1", "a b"), "1", "'a b'");
        assertMessage(compileFails("1", "a+b"), "1", "'a+b'", "operator");
        assertMessage(compileFails("1", "-"), "1", "operator");
        assertMessage(compileFails("1", "a(b"), "1", "bracket");
        assertMessage(compileFails("1", "a,b"), "1", "'a,b'");
        assertMessage(compileFails("1", "5"), "1", "'5'", "number");
        assertMessage(compileFails("1", "1.5"), "1", "number");
        assertMessage(compileFails("1", (String) null), "1", "null");
        assertMessage(compileFails("1", "d", "x", "d"), "1", "'d'", "twice");
    }

    @Test
    void nullInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Evaluator.compile(null, "d"));
        assertThrows(IllegalArgumentException.class, () -> Evaluator.compile("1", (String[]) null));
    }

    @Test
    void wrongNumberOfValuesIsRejected() {
        CompiledExpression ce = Evaluator.compile("d + x", "d", "x");
        IllegalArgumentException few = assertThrows(IllegalArgumentException.class, () -> ce.eval(1));
        assertTrue(few.getMessage().contains("d + x") && few.getMessage().contains("2") && few.getMessage().contains("got 1"),
                few.getMessage());
        IllegalArgumentException many = assertThrows(IllegalArgumentException.class, () -> ce.eval(1, 2, 3));
        assertTrue(many.getMessage().contains("got 3"), many.getMessage());
        assertThrows(IllegalArgumentException.class, ce::eval);
        assertThrows(IllegalArgumentException.class, () -> Evaluator.compile("1").eval(1));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // NaN, infinity, getters, immutability
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void nanAndInfinityAreReturnedAsTheyAre() {
        assertTrue(Double.isNaN(Evaluator.compile("sqrt(d)", "d").eval(-1)));
        assertTrue(Double.isNaN(Evaluator.compile("0 / d", "d").eval(0)));
        assertTrue(Double.isNaN(Evaluator.compile("ln(d)", "d").eval(-1)));
        assertTrue(Double.isNaN(Evaluator.compile("max(d, 1)", "d").eval(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, Evaluator.compile("1 / d", "d").eval(0));
        assertEquals(Double.NEGATIVE_INFINITY, Evaluator.compile("-1 / d", "d").eval(0));
        assertEquals(Double.POSITIVE_INFINITY, Evaluator.compile("d ^ 400", "d").eval(10));
        // DoubleEvaluator refuses the first one, which is why the compiled form is documented to differ here.
        assertNull(reference("sqrt(d)", new String[]{"d"}, new double[]{-1}));
    }

    @Test
    void gettersReturnSourceAndNames() {
        CompiledExpression ce = Evaluator.compile(FORMULA, "d", "x", "t");
        assertEquals(FORMULA, ce.getSource());
        assertEquals(Arrays.asList("d", "x", "t"), ce.getVariableNames());
        assertThrows(UnsupportedOperationException.class, () -> ce.getVariableNames().add("y"));
        assertTrue(ce.toString().contains(FORMULA), ce.toString());
    }

    @Test
    void changingTheNamesArrayAfterCompileChangesNothing() {
        String[] names = {"d", "x"};
        CompiledExpression ce = Evaluator.compile("d - x", names);
        names[0] = "x";
        names[1] = "d";
        assertEquals(Arrays.asList("d", "x"), ce.getVariableNames());
        assertEquals(5, ce.eval(8, 3));
    }

    @Test
    void evalDoesNotModifyTheValues() {
        CompiledExpression ce = Evaluator.compile(FORMULA, "d", "x", "t");
        double[] values = {10, 2, 3};
        ce.eval(values);
        assertArrayEquals(new double[]{10, 2, 3}, values);
    }

    @Test
    void evaluatorEvalIsUnchanged() {
        assertEquals(14, Evaluator.eval("2 + 3 * 4", 1));
        assertEquals(14, Evaluator.eval("2 + 3 * 4", 0));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Concurrency
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void sameInstanceCanBeEvaluatedFromManyThreads() throws Exception {
        CompiledExpression ce      = Evaluator.compile(FORMULA, "d", "x", "t");
        int                threads = 8;
        int                rounds  = 100_000;
        ExecutorService    pool    = Executors.newFixedThreadPool(threads);
        CountDownLatch     start   = new CountDownLatch(1);
        AtomicInteger      wrong   = new AtomicInteger();
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int th = 0; th < threads; th++) {
                final int id = th;
                futures.add(pool.submit(() -> {
                    Random r = new Random(id * 7919L);
                    start.await();
                    for (int i = 0; i < rounds; i++) {
                        double d = r.nextInt(1000) + id, x = r.nextInt(50) - 10 * id, t = r.nextInt(200) - 20;
                        double expected = Math.max(d / 5, d - 4 * x / Math.max(1, t + 8));
                        if (Double.doubleToLongBits(expected) != Double.doubleToLongBits(ce.eval(d, x, t))) {
                            wrong.incrementAndGet();
                        }
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> f : futures) {
                f.get(60, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
        assertEquals(0, wrong.get(), "evaluations that gave a wrong value under concurrency");
    }

    @Test
    void compilingFromManyThreadsAtOnce() throws Exception {
        int             threads = 8;
        ExecutorService pool    = Executors.newFixedThreadPool(threads);
        CountDownLatch  start   = new CountDownLatch(1);
        try {
            List<Future<Double>> futures = new ArrayList<>();
            for (int th = 0; th < threads; th++) {
                final int id = th;
                futures.add(pool.submit(() -> {
                    start.await();
                    double sum = 0;
                    for (int i = 0; i < 2000; i++) {
                        sum += Evaluator.compile("d * " + id + " + max(d, " + i + ")", "d").eval(2);
                    }
                    return sum;
                }));
            }
            start.countDown();
            for (int th = 0; th < threads; th++) {
                double expected = 0;
                for (int i = 0; i < 2000; i++) {
                    expected += 2 * th + Math.max(2, i);
                }
                assertEquals(expected, futures.get(th).get(60, TimeUnit.SECONDS));
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
