package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;

public class RemoteProcessManagerTest {

	private static class RemoteProcessManagerMainMock{
		public static void main(String[] args){
			System.out.println("RemoteProcessManagerMainMock");
			for (String arg: args)
				System.out.println(arg);
			System.exit(10);
		}
	}
	
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	RemoteProcessManager manager;

	@Before
	public void setUp() throws Exception {
		InetSocketAddress localSocket = ProcessorUtil.findEmptyLocalSocket();
		int maxActiveProcessNumbers = 3;
		int maxDoingTasks = 2;
		manager = new RemoteProcessManager(localSocket, maxActiveProcessNumbers, maxDoingTasks, RemoteProcessManagerMainMock.class);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testBoot() {
		assertNotNull(manager);
		InetSocketAddress remoteSocket = ProcessorUtil.findEmptyLocalSocket();
		RemoteProcess remoteProcess = new RemoteProcess(remoteSocket);
		Process process = null;
		try {
			process = manager.bootProcess(remoteProcess);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(process);
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals( process.exitValue(), 10);
	}
	
	@Test
	public void testAddProcess() {
		assertNotNull(manager);
		InetSocketAddress remoteSocket = ProcessorUtil.findEmptyLocalSocket();
		RemoteProcess remoteProcess = new RemoteProcess(remoteSocket);
		try {
			manager.addProcess(remoteProcess);
			manager.getRemoteProcessRecord(remoteProcess).process.waitFor();
			manager.changeStatusToIDLE(remoteProcess);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		assertEquals(remoteProcess, manager.getActiveRandomeProcess());
	}
	

}
