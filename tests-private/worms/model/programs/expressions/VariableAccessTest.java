package worms.model.programs.expressions;

import static org.junit.Assert.*;
import static worms.util.AssertUtil.assertFuzzyEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import worms.model.Food;
import worms.model.Program;
import worms.model.World;
import worms.model.Worm;
import worms.model.programs.WormsRuntimeException;
import worms.model.programs.types.BooleanType;
import worms.model.programs.types.DoubleType;
import worms.model.programs.types.EntityType;
import worms.model.programs.types.Type;

public class VariableAccessTest {
	
	VariableAccess<DoubleType> expressionDoubleA,expressionDoubleB;
	VariableAccess<EntityType> expressionEntity;
	VariableAccess<BooleanType> expressionBoolean;
	Program program;
	Worm willy;
	Food pizza;
	
	@Before
	public void setUp() throws Exception {
		World world = new World(20,30,new boolean[][]{{true,true},{false,true},{true,true}},new Random());
		pizza = new Food(world);
		
		expressionDoubleA = new VariableAccess<>("a");
		expressionDoubleB = new VariableAccess<>("b");
		expressionEntity = new VariableAccess<>("entity");
		expressionBoolean = new VariableAccess<>("bool");
		
		Map<String, Type<?>>globals = new HashMap<String, Type<?>>();
		globals.put("a", new DoubleType(3.14));
		globals.put("entity", new EntityType(pizza));
		globals.put("bool", new BooleanType(true));
		program = new Program(null, globals, null);
		

		willy  = new Worm(world, 112, 358, 1.321, 34.55, "Willy Wonka", null, program);
		//program is cloned so get correct program
		program = willy.getProgram();
	}

	@Test
	public void testCalculate_NormalCase() {
		assertFuzzyEquals(3.14, expressionDoubleA.calculate(program).getValue());
		assertEquals(pizza, expressionEntity.calculate(program).getValue());
		assertTrue(expressionBoolean.calculate(program).getValue());
	}
	
	@Test(expected=WormsRuntimeException.class)
	public void testCalculate_DoesntHaveVariableCase() {
		expressionDoubleB.calculate(program);
	}
	
	@Test(expected=WormsRuntimeException.class)
	public void testCalculate_NullProgramCase() {
		expressionDoubleA.calculate(null);
	}

}
