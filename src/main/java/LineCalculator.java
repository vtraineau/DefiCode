import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This calculator takes as input a string and returns its result or an error.
 * Source and details: https://github.com/EzoQC/DefiCode
 */
public class LineCalculator {
    final static String ERR = "Erreur*";
    final static Pattern PATTERN_NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?");
    final static Pattern PATTERN_OPERATOR = Pattern.compile("\\+|-|\\*|/|\\^|sqrt");
    final static Pattern PATTERN_GROUP_START = Pattern.compile("\\(");
    final static Pattern PATTERN_GROUP_END = Pattern.compile("\\)");

    public static void main(String[] args) {
        String inputExpression = String.join("", args);
        Expression expression = new Expression(inputExpression);
        System.out.println(getExpressionResult(expression));
    }

    static String getExpressionResult(Expression expression) {
        expression.setExpression(expression.getExpression().replace(" ", ""));
        buildExpressionTree(expression);
        calculateExpression(expression);
        if (!expression.isFailed() && expression.hasResult()) {
            return expression.getResult().stripTrailingZeros().toPlainString();
        } else {
            return ERR;
        }
    }

    /**
     * To build the "tree" form of an expression, consider 3 types of elements:
     * Numbers (including negative and decimal), Operators (@see Operator class), and Groups (parentheses, including the sqrt radicand)
     * The expression string is translated into a list of children of the above types.
     * Groups contain children themselves, their tree form is built recursively.
     * Example: "3*(2+4)"
     * In this expression, there is a Number 3 followed by the operator * and a group "2+4"
     * The group can itself be represented as 3 elements: 2, +, 4
     */
    static void buildExpressionTree(Expression expression) {
        while (expression.getCurrentIndex() != expression.getEnd()) {
            int currentIndexAtStart = expression.getCurrentIndex();
            Expression number = findAndGetNumber(expression.getExpression(), expression.getCurrentIndex());
            if (number != null) {
                expression.addChild(number);
                expression.addToCurrentIndex( number.getEnd() - number.getStart());
                if (expression.getCurrentIndex() == expression.getEnd()) {
                    return;
                }
            }
            Operator operator = findAndGetOperator(expression.getExpression(), expression.getCurrentIndex());
            if (operator != null) {
                Expression operatorAsExpression = new Expression(operator);
                expression.addChild(operatorAsExpression);
                expression.addToCurrentIndex(operatorAsExpression.getEnd());
            }
            if (findIfGroupIsStarting(expression)) {
                expression.addToCurrentIndex(1);
                Expression group = new Expression(expression.getExpression().substring(expression.getCurrentIndex()));
                buildExpressionTree(group);
                expression.addChild(group);
                expression.addToCurrentIndex(group.getEnd() - group.getStart());
            }
            if (findIfGroupIsEnding(expression)) {
                expression.addToCurrentIndex(1);
                expression.setEnd(expression.getCurrentIndex());
                return;
            }
            if (expression.getCurrentIndex() == expression.getEnd()) {
                return;
            } else if (expression.getCurrentIndex() == currentIndexAtStart) {
                expression.setFailed(true);
                return;
            }
        }
    }

    static Expression findAndGetNumber(String seq, int start) {
        Matcher m = PATTERN_NUMBER.matcher(seq.substring(start));
        if (m.find() && m.start() == 0) {
            BigDecimal b = new BigDecimal(m.group(0));
            return new Expression(b, start, start + m.end());
        }
        return null;
    }

    static Operator findAndGetOperator(String seq, int start) {
        Matcher m = PATTERN_OPERATOR.matcher(seq.substring(start));
        if (m.find() && m.start() == 0) {
            return switch (m.group(0)) {
                case "+" -> Operator.SUM;
                case "-" -> Operator.SUBTRACTION;
                case "*" -> Operator.MULTIPLICATION;
                case "/" -> Operator.DIVISION;
                case "^" -> Operator.EXPO;
                case "sqrt" -> Operator.SQRT;
                default -> null;
            };
        }
        return null;
    }

    static boolean findIfGroupIsStarting(Expression expression) {
        Matcher m = PATTERN_GROUP_START.matcher(expression.getExpression().substring(expression.getCurrentIndex()));
        return m.find() && m.start() == 0;
    }

    static boolean findIfGroupIsEnding(Expression expression) {
        Matcher m = PATTERN_GROUP_END.matcher(expression.getExpression().substring(expression.getCurrentIndex()));
        return m.find() && m.start() == 0;
    }

    static void calculateExpression(Expression expression) {
        if (expression.hasResult() || expression.isFailed()) {
            return;
        }
        if (expression.getChildren().size() > 1) {
            executeOperators(expression, Operator.SQRT, null);
            executeOperators(expression, Operator.EXPO, null);
            executeOperators(expression, Operator.MULTIPLICATION, Operator.DIVISION); // 2 operators executed from left to right
            executeOperators(expression, Operator.SUM, Operator.SUBTRACTION);
        }
        if (expression.getChildren().size() == 1) {
            expression.setResult(expression.getChildren().get(0).getResult());
            expression.setOperator(Operator.NONE);
        }
    }

    /**
     * To execute an operator, we find the operator in the list of children, and replace it and its operands with the result
     * Recursion is used when necessary.
     */
    static void executeOperators(Expression expression, Operator operator, Operator operator2) {
        boolean singleOperand = operator.equals(Operator.SQRT);
        ArrayList<Expression> newChildren = new ArrayList<>();
        boolean skipNext = false;
        for (Expression child : expression.getChildren()) {
            if (child.getOperator().equals(operator) || child.getOperator().equals(operator2)) {
                Expression res = getOperationResult(singleOperand ? null : child.getPrevious(), child.getOperator(), child.getNext());
                if (res.isFailed()) {
                    expression.setFailed(true);
                    return;
                }
                res.setPrevious(singleOperand ? child.getPrevious() : child.getPrevious() == null ? null : child.getPrevious().getPrevious());
                res.setNext(child.getNext() == null ? null : child.getNext().getNext());
                if (res.getNext() != null) {
                    res.getNext().setPrevious(res);
                }
                if (res.getPrevious() != null) {
                    res.getPrevious().setNext(res);
                }
                if (!singleOperand && newChildren.size() > 0) {
                    newChildren.remove(newChildren.size() - 1);
                }
                newChildren.add(res);
                skipNext = true;
            } else {
                if (!skipNext) {
                    newChildren.add(child);
                }
                skipNext = false;
            }
        }
        expression.setChildren(newChildren);
    }

    static Expression getOperationResult(Expression left, Operator operator, Expression right) {
        try {
            if (left != null && !left.hasResult()) {
                calculateExpression(left);
            } else if (right != null && !right.hasResult()) {
                calculateExpression(right);
            }
            BigDecimal res = switch (operator) {
                case NONE -> null;
                case SUM -> sum(left.getResult(), right.getResult());
                case SUBTRACTION -> subtract(left.getResult(), right.getResult());
                case MULTIPLICATION -> multiply(left.getResult(), right.getResult());
                case DIVISION -> divide(left.getResult(), right.getResult());
                case EXPO -> expo(left.getResult(), right.getResult());
                case SQRT -> sqrt(right.getResult());
            };
            return new Expression(res);
        } catch (Exception e) {
            Expression failedExpression = new Expression("");
            failedExpression.setFailed(true);
            return failedExpression;
        }
    }

    static BigDecimal multiply(BigDecimal left, BigDecimal right) {
        return left.multiply(right);
    }

    static BigDecimal divide(BigDecimal left, BigDecimal right) {
        return left.divide(right, RoundingMode.HALF_EVEN);
    }

    static BigDecimal expo(BigDecimal left, BigDecimal right) {
        return left.pow(right.intValue());
    }

    static BigDecimal sqrt(BigDecimal right) {
        return right.sqrt(MathContext.DECIMAL64);
    }

    static BigDecimal sum(BigDecimal left, BigDecimal right) {
        return left.add(right);
    }

    static BigDecimal subtract(BigDecimal left, BigDecimal right) {
        return left.subtract(right);
    }

}
