package worms.model.programs.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import worms.model.programs.types.DoubleType;

public class GreaterThanTest {
	
	Expression<DoubleType> literal3, literal5, literal17, literalNegative20;
	
	@Before
	public void setup() {
		literal3 = new DoubleLiteral(3);
		literal5 = new DoubleLiteral(5);
		literal17 = new DoubleLiteral(17);
		literalNegative20 = new DoubleLiteral(-20);
	}

	@Test
	public void testCalculate() {
		GreaterThan expr = new GreaterThan(literal3, literal5);
		assertFalse(expr.calculate(null).getValue());
		
		expr = new GreaterThan(literal17, literalNegative20);
		assertTrue(expr.calculate(null).getValue());
		
		expr = new GreaterThan(literalNegative20, literal5);
		assertFalse(expr.calculate(null).getValue());
		
		expr = new GreaterThan(literalNegative20, literalNegative20);
		assertFalse(expr.calculate(null).getValue());
	}

}
