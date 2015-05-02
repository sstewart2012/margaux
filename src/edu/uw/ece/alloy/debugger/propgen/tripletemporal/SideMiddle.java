package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SideMiddle extends Side implements SideOrdered {
	
	
	
	public SideMiddle(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, String endNext,
			String endFirst, String middleNext, String middleFirst,
			String endConcreteNext, String endConcreteFirst,
			String middleConcreteNext, String middleConcreteFirst) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, endNext, endFirst, middleNext, middleFirst,
				endConcreteNext, endConcreteFirst, middleConcreteNext,
				middleConcreteFirst);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String genBody() {
		return MiddleName;
	}

	@Override
	protected String getOther() {
		return EndName;
	}

	@Override
	public String getFirst() {
		return this.MiddleFirst;
	}

	@Override
	public String getNext() {
		return this.MiddleNext;
	}

	@Override
	public String genLetforLocality(String letVar, String quantifiedVar, final String quanitifiedOrderedVar) {
		final String letExpression = "let %1$s = %2$s(%3$s.%4$s) |";
		return String.format(letExpression, letVar, quantifiedVar+".", quanitifiedOrderedVar, RName );
	}

	@Override
	public String getConcreteFirst() {
		// TODO Auto-generated method stub
		return MiddleConcreteFirst;
	}

	@Override
	public String getConcreteNext() {
		// TODO Auto-generated method stub
		return MiddleConcreteNext;
	}


	

}
