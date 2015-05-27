package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public abstract class Locality extends Property {

	final public Side side;
	
	final public String quantifiedFirstForLet;
	final public String quantifiedNextForLet;
	
	
	
	
	public Locality(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, Side side,
			String quantifiedFirstForLet, String quantifiedNextForLet) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName);
		this.side = side;
		this.quantifiedFirstForLet = quantifiedFirstForLet;
		this.quantifiedNextForLet = quantifiedNextForLet;
	}


	@Override
	protected String getPredecessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSuccessor() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected  boolean isConsistent(){
		if(! side.isConsistent()) return false;
		if(side instanceof SideBoth) return false;
		return true;		
	}

	@Override
	protected String genPredName() {		
		return super.genPredName() + side.genPredName();
	}

	@Override
	protected String genBody() {		
		//Quantifier is like a body for Locality part of the predicate
		return genBody(quantifiedFirstForLet, quantifiedNextForLet);
	}
	
	
	protected String genBody(String q1, String q2) {
		//Quantifier is like a body for Locality part of the predicate
		return genQuantifier() + side.genLetforLocality(getLetVariable1(), getQunatifiedVar(), q1)  
								+ side.genLetforLocality(getLetVariable2(), getQunatifiedVar(), q2) ;
	}

	public abstract String genQuantifier();
	public abstract String getQunatifiedVar();
	
//	public abstract String getLetExprssion1(final String quantifier);
//	public abstract String getLetExprssion2(final String quantifier);

	public String getLetVariable1() {return "c";}
	public String getLetVariable2() {return getLetVariable1()+"'";}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((quantifiedFirstForLet == null) ? 0 : quantifiedFirstForLet
						.hashCode());
		result = prime
				* result
				+ ((quantifiedNextForLet == null) ? 0 : quantifiedNextForLet
						.hashCode());
		result = prime * result + ((side == null) ? 0 : side.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Locality)) {
			return false;
		}
		Locality other = (Locality) obj;
		if (quantifiedFirstForLet == null) {
			if (other.quantifiedFirstForLet != null) {
				return false;
			}
		} else if (!quantifiedFirstForLet.equals(other.quantifiedFirstForLet)) {
			return false;
		}
		if (quantifiedNextForLet == null) {
			if (other.quantifiedNextForLet != null) {
				return false;
			}
		} else if (!quantifiedNextForLet.equals(other.quantifiedNextForLet)) {
			return false;
		}
		if (side == null) {
			if (other.side != null) {
				return false;
			}
		} else if (!side.equals(other.side)) {
			return false;
		}
		return true;
	}

	
	
	
}
