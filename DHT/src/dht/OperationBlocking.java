package dht;


public class OperationBlocking {

	private java.util.logging.Logger LOGGER = MainUI.LOGGER;
	
	private boolean        waiting = false;
	private OperationsDHT  operation;
	
	public OperationBlocking() {
		
	}
	
	/**
	 * Wait when a remote operation has been requested. 
	 * The calling thread is continued when the operation is
	 * received
	 * @return The received operation
	 */
	public synchronized OperationsDHT sendOperation() {

		if (waiting) {
			LOGGER.severe("Invoke sendOperation while it is waiting");
			System.out.println("Invoke sendOperation while it is waiting");
			return null;
		}
		waiting = true;	
		try {
			while (waiting) {
				LOGGER.fine("-----Waiting for response...-----");
				wait();
			}			
		} catch (Exception e) {
			LOGGER.severe("Exception: sendOperation()");
			return null;
		}
		LOGGER.fine("Operation: " +  operation.getOperation() + 
				     ". Value: " + operation.getValue() +
				     ". Status: " + operation.getStatus());
		return operation;
	}

	/**
	 * This method is invoked when a requested operation is received.
	 * Then, the waiting thread is unblocked
	 * @param operation The received operation
	 */
	public synchronized void receiveOperation(OperationsDHT  operation) {
		
		if (!waiting) {
			LOGGER.severe("Invoke sendOperation while no waiting");
			System.out.println("Invoke sendOperation while no waiting");
			return;
		}
		
		waiting = false;
		this.operation = operation;
		notifyAll();
	}
}
