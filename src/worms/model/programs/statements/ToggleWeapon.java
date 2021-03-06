package worms.model.programs.statements;

import worms.model.Program;
import worms.model.programs.WormsRuntimeException;

public class ToggleWeapon
		implements ActionStatement {

	public ToggleWeapon() throws IllegalArgumentException {
		super();
	}

	@Override
	public void execute(Program program) throws WormsRuntimeException {
		if (program == null || program.getWorm() == null)
			throw new WormsRuntimeException();
		
		program.getActionHandler().toggleWeapon(program.getWorm());
	}

	@Override
	public int getCost(Program program) {
		return 0;
	}
}
