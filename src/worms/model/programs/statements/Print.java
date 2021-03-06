package worms.model.programs.statements;

import worms.model.Program;
import worms.model.programs.OneArgumentExecutable;
import worms.model.programs.WormsRuntimeException;
import worms.model.programs.expressions.Expression;
import worms.model.programs.types.Type;

public class Print
		extends OneArgumentExecutable<Expression<? extends Type<?>>>
		implements Statement {

	public Print(Expression<? extends Type<?>> e) throws IllegalArgumentException {
		super(e);
	}

	@Override
	public void execute(Program program) throws WormsRuntimeException {
		if (program == null)
			throw new WormsRuntimeException();
		
		String message = getFirstArgument().calculate(program).getValue().toString();
		program.getActionHandler().print(message);
	}
}
