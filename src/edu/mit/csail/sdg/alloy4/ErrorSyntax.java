package edu.mit.csail.sdg.alloy4;

/**
 * Immutable;
 * represents a syntax error that should be reported to the user.
 *
 * <br/>
 * <br/> Invariant: msg!=null
 *
 * @author Felix Chang
 */

@SuppressWarnings("serial")
public final class ErrorSyntax extends ErrorWithPos {

    /**
     * Constructs a new exception object.
     * @param pos - the filename/line/row information (null if unknown)
     * @param msg - the actual error message
     */
    public ErrorSyntax(Pos pos, String msg) {
    	super(pos,null,msg);
    }

    /** Returns a human-readable description of the error */
    @Override public String toString() {
        if (pos==null) return "Syntax error: "+msg;
        if (pos.filename.length()>0)
            return "Syntax error in "+pos.filename
            +" at line "+pos.y+" column "+pos.x+": "+msg;
        return "Syntax error at line "+pos.y+" column "+pos.x+": "+msg;
    }
}
