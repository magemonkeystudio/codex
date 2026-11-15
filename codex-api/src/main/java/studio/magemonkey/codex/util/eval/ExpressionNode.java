package studio.magemonkey.codex.util.eval;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * One node of a compiled expression tree. Nodes are immutable and hold no per-call state, so a tree can be
 * evaluated from any number of threads at once and evaluating it allocates nothing.
 * <p>
 * Every arithmetic rule here is a copy of the matching branch in
 * {@link studio.magemonkey.codex.util.eval.javaluator.DoubleEvaluator}; the differential tests keep the two in step.
 */
abstract class ExpressionNode {

    /**
     * @param values the values of the variables, in the order they were given to the compiler. Never modified.
     */
    abstract double eval(double[] values);

    static final class Constant extends ExpressionNode {
        private final double value;

        Constant(double value) {
            this.value = value;
        }

        @Override
        double eval(double[] values) {
            return value;
        }
    }

    static final class Variable extends ExpressionNode {
        private final int index;

        Variable(int index) {
            this.index = index;
        }

        @Override
        double eval(double[] values) {
            return values[index];
        }
    }

    static final class Unary extends ExpressionNode {
        private final DoubleUnaryOperator op;
        private final ExpressionNode      operand;

        Unary(DoubleUnaryOperator op, ExpressionNode operand) {
            this.op = op;
            this.operand = operand;
        }

        @Override
        double eval(double[] values) {
            return op.applyAsDouble(operand.eval(values));
        }
    }

    static final class Binary extends ExpressionNode {
        private final DoubleBinaryOperator op;
        private final ExpressionNode       left;
        private final ExpressionNode       right;

        Binary(DoubleBinaryOperator op, ExpressionNode left, ExpressionNode right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        double eval(double[] values) {
            // Left before right, like the operand order of the string evaluator.
            double l = left.eval(values);
            double r = right.eval(values);
            return op.applyAsDouble(l, r);
        }
    }

    /**
     * A function over any number of arguments, folded from the left. {@code seed} is used for functions that start
     * from a fixed value ({@code sum}, {@code avg}) instead of from the first argument.
     */
    static final class Fold extends ExpressionNode {
        private final DoubleBinaryOperator op;
        private final boolean              seeded;
        private final boolean              average;
        private final ExpressionNode[]     args;

        Fold(DoubleBinaryOperator op, boolean seeded, boolean average, ExpressionNode[] args) {
            this.op = op;
            this.seeded = seeded;
            this.average = average;
            this.args = args;
        }

        @Override
        double eval(double[] values) {
            double result;
            int    from;
            if (seeded) {
                result = 0.;
                from = 0;
            } else {
                result = args[0].eval(values);
                from = 1;
            }
            for (int i = from; i < args.length; i++) {
                result = op.applyAsDouble(result, args[i].eval(values));
            }
            return average ? result / args.length : result;
        }
    }

    /** {@code random()}: a fresh {@link Math#random()} on every call, like the string evaluator. */
    static final class Random extends ExpressionNode {
        @Override
        double eval(double[] values) {
            return Math.random();
        }
    }
}
