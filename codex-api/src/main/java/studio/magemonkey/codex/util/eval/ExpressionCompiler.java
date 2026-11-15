package studio.magemonkey.codex.util.eval;

import studio.magemonkey.codex.util.eval.javaluator.*;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns an expression into an {@link ExpressionNode} tree using the grammar of {@link DoubleEvaluator}.
 * <p>
 * This is an {@link AbstractEvaluator} whose values are tree nodes instead of numbers, so the tokenizer, the
 * operator precedence and associativity, the unary minus handling, the bracket and argument-count checks are all
 * the ones {@link DoubleEvaluator} runs. Only the meaning given to each operator and function lives here.
 */
final class ExpressionCompiler extends AbstractEvaluator<ExpressionNode> {
    private static final Pattern POSITION = Pattern.compile("At position (\\d+)");

    private static final ThreadLocal<NumberFormat> FORMATTER =
            ThreadLocal.withInitial(() -> NumberFormat.getNumberInstance(Locale.US));

    private static final Map<Function, DoubleUnaryOperator> UNARY = new IdentityHashMap<>();

    static {
        UNARY.put(DoubleEvaluator.ABS, Math::abs);
        UNARY.put(DoubleEvaluator.CEIL, Math::ceil);
        UNARY.put(DoubleEvaluator.FLOOR, Math::floor);
        UNARY.put(DoubleEvaluator.ROUND, d -> d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY
                ? d
                : (double) Math.round(d));
        UNARY.put(DoubleEvaluator.SINEH, Math::sinh);
        UNARY.put(DoubleEvaluator.COSINEH, Math::cosh);
        UNARY.put(DoubleEvaluator.TANGENTH, Math::tanh);
        UNARY.put(DoubleEvaluator.SINE, Math::sin);
        UNARY.put(DoubleEvaluator.COSINE, Math::cos);
        UNARY.put(DoubleEvaluator.TANGENT, Math::tan);
        UNARY.put(DoubleEvaluator.ACOSINE, Math::acos);
        UNARY.put(DoubleEvaluator.ASINE, Math::asin);
        UNARY.put(DoubleEvaluator.ATAN, Math::atan);
        UNARY.put(DoubleEvaluator.LN, Math::log);
        UNARY.put(DoubleEvaluator.LOG, Math::log10);
        UNARY.put(DoubleEvaluator.SQRT, Math::sqrt);
    }

    /** The variables of one compilation, as the evaluation context the shared parser asks for values. */
    private static final class Variables implements AbstractVariableSet<ExpressionNode> {
        private final Map<String, ExpressionNode> byName = new HashMap<>();

        @Override
        public ExpressionNode get(String variableName) {
            return byName.get(variableName);
        }
    }

    ExpressionCompiler() {
        super(DoubleEvaluator.getDefaultParameters());
    }

    /**
     * @param expression the expression to compile
     * @param names      the variable names, already checked for duplicates
     * @return the tree
     * @throws IllegalArgumentException if the expression is not valid
     */
    ExpressionNode compile(String expression, List<String> names) {
        Variables variables = new Variables();
        for (int i = 0; i < names.size(); i++) {
            variables.byName.put(names.get(i), new ExpressionNode.Variable(i));
        }
        try {
            return evaluate(expression, variables);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(describe(expression, e.getMessage()), e);
        } catch (RuntimeException e) {
            // The shared parser reports some malformed input (a lone ",", a missing operand) as an NPE or similar.
            throw new IllegalArgumentException(describe(expression, "malformed expression"), e);
        }
    }

    /**
     * Checks that a variable name would be read back as a single variable: not blank, no whitespace, no operator or
     * bracket in it, not a function, a constant or a number.
     *
     * @return why the name is not usable, or null if it is
     */
    String checkVariableName(String name) {
        if (name == null) {
            return "variable name is null";
        }
        if (name.isEmpty() || name.chars().anyMatch(Character::isWhitespace)) {
            return "variable name '" + name + "' is empty or contains whitespace";
        }
        if (functions.containsKey(name)) {
            return "variable name '" + name + "' is already a function";
        }
        if (constants.containsKey(name)) {
            return "variable name '" + name + "' is already a constant";
        }
        Collection<Token> tokens = tokenizer.tokenize(name);
        if (tokens.size() != 1 || !tokens.iterator().next().isLiteral()) {
            return "variable name '" + name + "' contains an operator, bracket, separator or whitespace";
        }
        if (parseNumber(name) != null) {
            return "variable name '" + name + "' is a number";
        }
        return null;
    }

    private static String describe(String expression, String cause) {
        String message = cause == null || cause.isEmpty() ? "malformed expression" : cause;
        // The shared parser adds a three-line "At position N of line L" excerpt; keep the position, on one line.
        int newline = message.indexOf('\n');
        String first = newline < 0 ? message : message.substring(0, newline);
        Matcher m = POSITION.matcher(message);
        String where = m.find() ? " (at position " + m.group(1) + ")" : "";
        return "Cannot compile '" + expression + "': " + first + where;
    }

    private static Double parseNumber(String literal) {
        ParsePosition p      = new ParsePosition(0);
        Number        result = FORMATTER.get().parse(literal, p);
        if (p.getIndex() == 0 || p.getIndex() != literal.length()) {
            return null;
        }
        return result.doubleValue();
    }

    @Override
    protected ExpressionNode toValue(Token literalTok, EvaluationContext context) {
        String literal = literalTok.getString();
        // Same parse as DoubleEvaluator.toValue, so a number compiles to the exact double the string evaluator gets.
        Double number = parseNumber(literal);
        if (number != null) {
            return new ExpressionNode.Constant(number);
        }
        char first = literal.charAt(0);
        if ((first >= '0' && first <= '9') || first == '.') {
            throw context.getError("invalid number '" + literal + "'", literalTok);
        }
        throw context.getError("unknown variable or function '" + literal + "'", literalTok);
    }

    @Override
    protected ExpressionNode evaluate(Constant constant, EvaluationContext context) {
        if (DoubleEvaluator.PI == constant) {
            return new ExpressionNode.Constant(Math.PI);
        } else if (DoubleEvaluator.E == constant) {
            return new ExpressionNode.Constant(Math.E);
        }
        throw new IllegalArgumentException("unsupported constant '" + constant.getName() + "'");
    }

    @Override
    protected ExpressionNode evaluate(Operator operator, Iterator<ExpressionNode> operands, EvaluationContext context) {
        if (DoubleEvaluator.NEGATE == operator || DoubleEvaluator.NEGATE_HIGH == operator) {
            return new ExpressionNode.Unary(d -> -d, operands.next());
        }
        ExpressionNode left  = operands.next();
        ExpressionNode right = operands.next();
        if (DoubleEvaluator.MINUS == operator) {
            return new ExpressionNode.Binary((a, b) -> a - b, left, right);
        } else if (DoubleEvaluator.PLUS == operator) {
            return new ExpressionNode.Binary((a, b) -> a + b, left, right);
        } else if (DoubleEvaluator.MULTIPLY == operator) {
            return new ExpressionNode.Binary((a, b) -> a * b, left, right);
        } else if (DoubleEvaluator.DIVIDE == operator) {
            return new ExpressionNode.Binary((a, b) -> a / b, left, right);
        } else if (DoubleEvaluator.EXPONENT == operator) {
            return new ExpressionNode.Binary(Math::pow, left, right);
        } else if (DoubleEvaluator.MODULO == operator) {
            return new ExpressionNode.Binary((a, b) -> a % b, left, right);
        }
        throw new IllegalArgumentException("unsupported operator '" + operator.getSymbol() + "'");
    }

    @Override
    protected void doFunction(Deque<ExpressionNode> values, Token functionTok, int argCount, EvaluationContext context) {
        Function f = functionTok.getFunction();
        if (argCount < f.getMinimumArgumentCount() || argCount > f.getMaximumArgumentCount()) {
            String expected = f.getMaximumArgumentCount() == Integer.MAX_VALUE
                    ? "at least " + f.getMinimumArgumentCount()
                    : f.getMinimumArgumentCount() == f.getMaximumArgumentCount()
                      ? String.valueOf(f.getMinimumArgumentCount())
                      : f.getMinimumArgumentCount() + " to " + f.getMaximumArgumentCount();
            throw context.getError(
                    "wrong number of arguments for " + f.getName() + ": expected " + expected + " but got " + argCount,
                    functionTok);
        }
        super.doFunction(values, functionTok, argCount, context);
    }

    @Override
    protected ExpressionNode evaluate(Function function, Iterator<ExpressionNode> arguments, EvaluationContext context) {
        DoubleUnaryOperator unary = UNARY.get(function);
        if (unary != null) {
            return new ExpressionNode.Unary(unary, arguments.next());
        }
        if (DoubleEvaluator.RANDOM == function) {
            return new ExpressionNode.Random();
        }
        List<ExpressionNode> list = new ArrayList<>();
        while (arguments.hasNext()) {
            list.add(arguments.next());
        }
        ExpressionNode[] args = list.toArray(new ExpressionNode[0]);
        if (DoubleEvaluator.MIN == function) {
            return new ExpressionNode.Fold(Math::min, false, false, args);
        } else if (DoubleEvaluator.MAX == function) {
            return new ExpressionNode.Fold(Math::max, false, false, args);
        } else if (DoubleEvaluator.SUM == function) {
            return new ExpressionNode.Fold((a, b) -> a + b, true, false, args);
        } else if (DoubleEvaluator.AVERAGE == function) {
            return new ExpressionNode.Fold((a, b) -> a + b, true, true, args);
        }
        throw new IllegalArgumentException("unsupported function '" + function.getName() + "'");
    }
}
