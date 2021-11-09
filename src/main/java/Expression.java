import java.math.BigDecimal;
import java.util.ArrayList;

public class Expression {
    private String expression;
    private BigDecimal result;
    private boolean failed = false;
    private Operator operator = Operator.NONE;
    private ArrayList<Expression> children = new ArrayList<>();
    private Expression previous;
    private Expression next;
    private int start;
    private int currentIndex;
    private int end;

    public Expression(String expression) {
        setExpression(expression);
    }

    public Expression(Operator operator) {
        this.operator = operator;
        this.start = 0;
        this.end = operator.equals(Operator.SQRT) ? 4 : 1;
    }

    public Expression(BigDecimal result, int start, int end) {
        this.result = result;
        this.start = start;
        this.end = end;
    }

    public Expression(BigDecimal result) {
        this.result = result;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
        this.start = 0;
        this.end = expression.length();
    }

    public BigDecimal getResult() {
        return result;
    }

    public void setResult(BigDecimal result) {
        this.result = result;
    }

    public boolean hasResult() {
        return this.result != null;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public void addChild(Expression expression) {
        if (this.children.size() > 0) {
            Expression previous = this.children.get(this.children.size() - 1);
            previous.setNext(expression);
            expression.setPrevious(previous);
        }
        this.children.add(expression);
    }

    public void setChildren(ArrayList<Expression> newChildren) {
        this.children = newChildren;
    }

    public ArrayList<Expression> getChildren() {
        return this.children;
    }

    public Expression getPrevious() {
        return previous;
    }

    public void setPrevious(Expression previous) {
        this.previous = previous;
    }

    public Expression getNext() {
        return next;
    }

    public void setNext(Expression next) {
        this.next = next;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void addToCurrentIndex(int numberToAdd) {
        this.currentIndex += numberToAdd;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
