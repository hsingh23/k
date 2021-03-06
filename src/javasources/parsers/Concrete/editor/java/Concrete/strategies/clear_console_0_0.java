package Concrete.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.eclipse.ui.console.*;

/**
 * Example Java strategy implementation.
 * 
 * This strategy can be used by editor services and can be called in Stratego modules by declaring it as an external strategy as follows:
 * 
 * <code>
 *  external string-trim-last-one(|)
 * </code>
 * 
 * @see InteropRegisterer This class registers string_trim_last_one_0_0 for use.
 */
public class clear_console_0_0 extends Strategy {

	public static clear_console_0_0 instance = new clear_console_0_0();

	public final static String conName = "Spoofax Console";

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm current) {
		IOConsole cons = getSpoofaxConsole();
		if (cons != null)
			cons.clearConsole();
		return current;
	}

	private IOConsole getSpoofaxConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (IConsole cons : existing) {
			if (cons.getName().equals(conName))
				return (IOConsole) cons;
		}
		return null;
	}
}