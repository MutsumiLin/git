package edu.green.trip;

import edu.green.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CarbonCalculatorTest {
    private final CarbonCalculator calculator=new CarbonCalculator();
    private final CarbonRule rule=new CarbonRule(1,"simulation-v1",new BigDecimal("0.200000"),new BigDecimal("0.080000"),
            new BigDecimal("0.050000"),BigDecimal.ZERO,100);
    @ParameterizedTest
    @CsvSource({"BUS,10.00,1.200000,120","METRO,10.00,1.500000,150","BIKE,10.00,2.000000,200",
            "BUS,0.01,0.001200,0","BUS,0.09,0.010800,1","BUS,0.25,0.030000,3","BIKE,100.00,20.000000,2000"})
    void decimalMathAndFloorAreExact(String mode,String distance,String saved,long points) {
        var result=calculator.calculate(new BigDecimal(distance),TransportMode.valueOf(mode),rule);
        assertEquals(new BigDecimal(saved),result.savedCarbonKg());assertEquals(points,result.earnedPoints());
    }
    @ParameterizedTest @CsvSource({"0","-1","100.01","0.001","10.000"})
    void invalidDistanceIsRejected(String distance) {
        assertThrows(BusinessException.class,()->calculator.calculate(new BigDecimal(distance),TransportMode.BUS,rule));
    }
    @Test void unsupportedNullInputsAreRejected() {
        assertThrows(BusinessException.class,()->calculator.calculate(null,TransportMode.BUS,rule));
        assertThrows(BusinessException.class,()->calculator.calculate(BigDecimal.ONE,null,rule));
    }
    @Test void higherEmissionThanBaselineProducesZero() {
        var high=new CarbonRule(2,"higher",new BigDecimal("0.1"),new BigDecimal("0.2"),BigDecimal.ZERO,BigDecimal.ZERO,100);
        var result=calculator.calculate(BigDecimal.TEN,TransportMode.BUS,high);
        assertEquals(0,result.earnedPoints());assertEquals(new BigDecimal("0.000000"),result.savedCarbonKg());
    }
}
