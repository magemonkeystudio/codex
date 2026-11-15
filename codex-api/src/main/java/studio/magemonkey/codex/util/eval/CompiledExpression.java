package studio.magemonkey.codex.util.eval;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A math expression that was parsed once and can be evaluated many times with different variable values.
 * <p>
 * Get one from {@link Evaluator#compile(String, String...)}:
 * <pre>{@code
 * CompiledExpression ce = Evaluator.compile("max(d / 5, d - 4 * x / max(1, t + 8))", "d", "x", "t");
 * double v = ce.eval(def, dmg, toughness);
 * }</pre>
 * The syntax, operators, functions and constants are those of the bundled Javaluator
 * {@link studio.magemonkey.codex.util.eval.javaluator.DoubleEvaluator}, and for any expression both accept the
 * result is the same double, bit for bit. The differences:
 * <ul>
 *     <li>{@code NaN} and infinities are returned as they are. {@code DoubleEvaluator} throws when a <i>function</i>
 *     returns NaN (for example {@code sqrt(-1)}); here that is the caller's check.</li>
 *     <li>{@code random()} gives a new random number on every call, as it does there.</li>
 * </ul>
 * An instance is immutable and may be shared and evaluated from any number of threads at once.
 */
@ToString(onlyExplicitlyIncluded = true)
public final class CompiledExpression {
    private static final ExpressionCompiler COMPILER = new ExpressionCompiler();

    /** The expression as it was given to {@link Evaluator#compile(String, String...)}. */
    @Getter
    @ToString.Include
    private final String         source;
    /** The variable names, in the order {@link #eval(double...)} expects their values. Unmodifiable. */
    @Getter
    @ToString.Include
    private final List<String>   variableNames;
    private final ExpressionNode root;

    private CompiledExpression(String source, List<String> variableNames, ExpressionNode root) {
        this.source = source;
        this.variableNames = variableNames;
        this.root = root;
    }

    /**
     * @see Evaluator#compile(String, String...)
     */
    static CompiledExpression compile(String expression, String... variables) {
        if (expression == null) {
            throw new IllegalArgumentException("Cannot compile a null expression");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Cannot compile '" + expression + "': variable names are null");
        }
        List<String> names = new ArrayList<>(variables.length);
        Set<String>  seen  = new HashSet<>();
        for (String name : variables) {
            String problem = COMPILER.checkVariableName(name);
            if (problem == null && !seen.add(name)) {
                problem = "variable name '" + name + "' is given twice";
            }
            if (problem != null) {
                throw new IllegalArgumentException("Cannot compile '" + expression + "': " + problem);
            }
            names.add(name);
        }
        return new CompiledExpression(expression,
                Collections.unmodifiableList(names),
                COMPILER.compile(expression, names));
    }

    /**
     * Evaluates the expression. Does not parse, does not allocate, and does not modify {@code values}.
     *
     * @param values one value for each variable name, in the order the names were given to {@code compile}
     * @return the result. May be NaN or infinite; checking is up to the caller.
     * @throws IllegalArgumentException if the number of values differs from the number of variable names
     */
    public double eval(double... values) {
        if (values.length != variableNames.size()) {
            throw new IllegalArgumentException("Expression '" + source + "' takes " + variableNames.size()
                    + " value(s) " + variableNames + " but got " + values.length);
        }
        return root.eval(values);
    }
}
