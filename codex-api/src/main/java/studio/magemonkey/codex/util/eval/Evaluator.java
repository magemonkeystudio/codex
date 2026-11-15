package studio.magemonkey.codex.util.eval;

import studio.magemonkey.codex.Codex;
import studio.magemonkey.codex.util.eval.javaluator.DoubleEvaluator;

public class Evaluator {

    private static DoubleEvaluator jl = new DoubleEvaluator();

    /**
     * @param exp    String math expression.
     * @param method Evaluation method. Allowed: 0 - internal, 1 - javaluator.
     * @return Evaluated expression.
     */
    public static double eval(String exp, int method) {
        if (method == 1 || method == 2) {
            return javaluator(exp);
        } else {
            return internal(exp);
        }
    }

    /**
     * Parses a math expression once, so it can be evaluated many times with different values for its variables.
     * Use this instead of {@link #eval(String, int)} when the same formula runs repeatedly.
     * <p>
     * The syntax is that of the Javaluator evaluator ({@code + - * / ^ %}, unary minus, brackets, {@code min},
     * {@code max}, {@code abs}, {@code sqrt}, the constants {@code pi} and {@code e} and so on).
     *
     * @param expression the expression, such as {@code max(d / 5, d - 4 * x / max(1, t + 8))}
     * @param variables  the names of the variables the expression may use. Their values are later given to
     *                   {@link CompiledExpression#eval(double...)} in this order. A name cannot be a function, a
     *                   constant or a number, cannot contain an operator, bracket, comma or whitespace, and cannot
     *                   be repeated.
     * @return the compiled expression, which is immutable and thread safe
     * @throws IllegalArgumentException with a message naming the expression, if it has a syntax error, mismatched
     *                                  brackets, an unknown variable or function, a function with the wrong number
     *                                  of arguments, or if a variable name is not usable
     */
    public static CompiledExpression compile(String expression, String... variables) {
        return CompiledExpression.compile(expression, variables);
    }

    private static double javaluator(String exp) {
        try {
            return jl.evaluate(exp);
        } catch (IllegalArgumentException e) {
            Codex.error("[E1] Unable to evaluate '" + exp + "'. Trying with E0...");
            return eval(exp, 0);
        }
    }

    private static double internal(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int    startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    x = switch (func) {
                        case "sqrt" -> Math.sqrt(x);
                        case "sin" -> Math.sin(Math.toRadians(x));
                        case "cos" -> Math.cos(Math.toRadians(x));
                        case "tan" -> Math.tan(Math.toRadians(x));
                        default -> throw new RuntimeException("Unknown function: " + func);
                    };
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}
