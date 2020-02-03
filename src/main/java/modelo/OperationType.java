/**
 * 
 */
package modelo;


/**
 * @author Werney Ayala
 *
 */
public enum OperationType {
	
	ADD('A'), MOD('M'), DEL('D'), REN('R');
	
	private char operationType;
	
	private OperationType(char operationType){
		this.operationType = operationType;
	}

	/**
	 * @return the operationType
	 */
	public char getOperationType() {
		return operationType;
	}
	
	public void setOperationType(char operationType){
		this.operationType = operationType;
	}
	
}
