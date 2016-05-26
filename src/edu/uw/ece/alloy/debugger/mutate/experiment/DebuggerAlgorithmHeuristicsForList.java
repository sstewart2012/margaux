package edu.uw.ece.alloy.debugger.mutate.experiment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.uw.ece.alloy.debugger.mutate.Approximator;
import edu.uw.ece.alloy.debugger.mutate.DebuggerAlgorithm;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.mutate.Oracle;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class DebuggerAlgorithmHeuristicsForList extends DebuggerAlgorithm {

	final public static DebuggerAlgorithmHeuristicsForList EMPTY_ALGORITHM = new DebuggerAlgorithmHeuristicsForList();

	// Whether an expression is inconsistent by itself.
	boolean inconsistentExpressions = false;
	// A map from an expression and weakest inconsistent properties.
	final Map<Expr, Pair<String, String>> weakestInconsistentProps;
	final Map<Expr, Pair<String, String>> allInconsistentProps;

	protected DebuggerAlgorithmHeuristicsForList(File sourceFile,
			File destinationDir, Approximator approximator, Oracle oracle,
			ExampleFinder exampleFinder) {
		super(sourceFile, destinationDir, approximator, oracle, exampleFinder);
		weakestInconsistentProps = new HashMap<>();
		allInconsistentProps = new HashMap<>();
	}

	protected DebuggerAlgorithmHeuristicsForList() {
		super();
		weakestInconsistentProps = null;
		allInconsistentProps = null;
	}

	@Override
	protected void afterInquiryOracle() {
	}

	@Override
	protected void beforeInquiryOracle() {
	}

	@Override
	protected void afterCallingExampleFinder() {
	}

	@Override
	protected void beforeCallingExampleFinder() {
	}

	@Override
	protected void afterMutating() {
	}

	@Override
	protected void beforeMutating() {
	}

	@Override
	protected void beforePickWeakenOrStrengthenedApprox() {
	}

	@Override
	protected void afterPickWeakenOrStrengthened() {
	}

	@Override
	protected void beforePickWeakenOrStrengthened() {

		// RULE: if an expression is inconsistent by itself, then do not Strengthen
		// it.
		if (inconsistentExpressions) {
			// emptying the strongerApproxQueue prevents any strengthening
			strongerApproxQueue.clear();
		}

	}

	@Override
	protected void afterPickApproximation() {
	}

	@Override
	protected void beforePickApproximation() {
	}

	@Override
	protected void afterPickModelPart() {
	}

	@Override
	protected void beforePickModelPart() {
	}

	@Override
	protected void afterPickField() {
		// find out whether an expression is inconsistent by itself
		try {

			inconsistentExpressions = super.approximator.isInconsistent(constraint,
					toBeingAnalyzedField, scope);
		} catch (Err e) {
			e.printStackTrace();
			logger.severe(Utils.threadName() + constraint
					+ " cannot be converted to an inorder form.");
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void beforePickField() {
	}

	@Override
	protected void onStartLoop() {
	}

	@Override
	public DebuggerAlgorithmHeuristicsForList createIt(File sourceFile,
			File destinationDir, Approximator approximator, Oracle oracle,
			ExampleFinder exampleFinder) {
		return new DebuggerAlgorithmHeuristicsForList(sourceFile, destinationDir,
				approximator, oracle, exampleFinder);
	}

}
