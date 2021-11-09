import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LineCalculatorTests {
    @ParameterizedTest
    @MethodSource("provideParameters")
    void testCalculator(String in, String out) {
        Expression expression = new Expression(in);
        assertEquals(out, LineCalculator.getExpressionResult(expression));
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("1+1", "2"),
                Arguments.of("1+1+1", "3"),
                Arguments.of("1 + 2", "3"),
                Arguments.of("1 + -1", "0"),
                Arguments.of("-1 - -1", "0"),
                Arguments.of("-1 ---1", LineCalculator.ERR), // more than two operators next to one another is not worth supporting
                Arguments.of("-1 ----1", LineCalculator.ERR),
                Arguments.of("-1 + -1", "-2"),
                Arguments.of("5-4", "1"),
                Arguments.of("5*2", "10"),
                Arguments.of("(2+5)*3", "21"),
                Arguments.of("10/2", "5"),
                Arguments.of("2+2*5+5", "17"),
                Arguments.of("2.8*3-1", "7.4"),
                Arguments.of("2^8", "256"),
                Arguments.of("2^8*5-1", "1279"),
                Arguments.of("sqrt(4)", "2"),
                Arguments.of("sqrt(2*2)", "2"),
                Arguments.of("sqrt(1+3)", "2"),
                Arguments.of("3*sqrt(1+3)", "6"),
                Arguments.of("3+sqrt(1+3)", "5"),
                Arguments.of("1/0", LineCalculator.ERR),
                Arguments.of("1+1-1", "1"),
                Arguments.of("1.3+1", "2.3"),
                Arguments.of("1 + 1", "2"),
                Arguments.of("1 - 1", "0"),
                Arguments.of("-1", "-1"),
                Arguments.of("1 - 1.2", "-0.2"),
                Arguments.of("0", "0"),
                Arguments.of("342", "342"),
                Arguments.of("foo", LineCalculator.ERR),
                Arguments.of("", LineCalculator.ERR),
                Arguments.of("8*3+1", "25"),
                Arguments.of("8+3*2", "14"),
                Arguments.of("8+3*-2", "2"),
                Arguments.of("8+-3*2", "2"),
                Arguments.of("()", LineCalculator.ERR),
                Arguments.of("(2+3", LineCalculator.ERR),
                Arguments.of("2+3)", "5"), // ARGUABLE
                Arguments.of("2/", LineCalculator.ERR),
                Arguments.of("2*", LineCalculator.ERR),
                Arguments.of("*3", LineCalculator.ERR),
                Arguments.of("/3", LineCalculator.ERR),
                Arguments.of("+3", LineCalculator.ERR), // ARGUABLE
                Arguments.of("-3", "-3"),
                Arguments.of("2^10", "1024"),
                Arguments.of("sqrt(16)*340.5", "1362"),
                Arguments.of("(((3+2)*3)+24/2-3)*3", "72"),
                Arguments.of("3+sqrt(4)*3-2/(3+4)*0", "9")
        );
    }
}
