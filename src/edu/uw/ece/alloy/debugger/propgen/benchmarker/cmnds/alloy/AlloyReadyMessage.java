package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import java.util.Map;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;

public class AlloyReadyMessage extends ReadyMessage {

	private static final long serialVersionUID = -6479019132107993293L;

	public AlloyReadyMessage(RemoteProcess process) {
		super(process);
	}
	
	public AlloyReadyMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}	
	
	@Override
	public void onAction(Map<String, Object> context)
			throws InvalidParameterException {
		RemoteProcessLogger manager = (RemoteProcessLogger) context
				.get("RemoteProcessLogger");
		manager.changeStatusToIDLE(process);
	}
	
}
