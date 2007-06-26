package edu.mit.csail.sdg.alloy4compiler.parser;

import java.util.List;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorAPI;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;

/**
 * Immutable; represents a field/variable/parameter declaration such as "a,b,c: X".
 *
 * <p> <b>Invariant:</b>  names.size()>0
 * <p> <b>Invariant:</b>  all n:names | n is not "", and doesn't contain '/' or '@'
 */

final class EDecl {

    /** The filename, line, and column position in the original Alloy model file; never null. */
    final Pos pos;

    /** Caches the span() result. */
    private Pos span=null;

    /** Whether this is a "disjoint" declaration; NOTE: if names.size()==1, then this flag is always false. */
    final boolean disjoint;

    /** If disjoint is true, then this records the location of the "disjoint" keyword in the original source. */
    final Pos disjointPosition;

    /** The unmodifiable list of names. */
    final ConstList<String> names;

    /** The expression that these names are quantified over. */
    final Expr value;

    /** Returns a human-readable String representation. */
    @Override public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append("EDecl{");
        for(int i=0; i<names.size(); i++) {
            if (i!=0) sb.append(',');
            sb.append(names.get(i));
        }
        sb.append("}\n");
        value.toString(sb,1);
        return sb.toString();
    }

    /**
     * Constructs a new EDecl object with x as the list of names.
     *
     * @param pos - the original position in the file (can be null if unknown)
     * @param names - the list of names
     * @param disjoint - if nonnull, it means the names should be bound to disjoint values
     * @param value - the expression that the names are quantified over
     *
     * @throws ErrorAPI    if x.size()==0
     * @throws ErrorSyntax if any of the name is ""
     * @throws ErrorSyntax if any of the name contains '/' or '@'
     */
    EDecl (Pos pos, List<String> names, Pos disjoint, Expr value) throws Err {
        if (pos==null) pos=Pos.UNKNOWN;
        if (disjoint!=null) pos=pos.merge(disjoint);
        this.pos=pos;
        this.names=ConstList.make(names);
        this.disjoint=(disjoint!=null) && (this.names.size()>1);
        this.disjointPosition=(this.disjoint ? disjoint : Pos.UNKNOWN);
        this.value=value;
        if (this.names.size()==0) throw new ErrorAPI(this.pos, "The list of declarations cannot be empty.");
        for(int i=0; i<this.names.size(); i++) {
            String n=this.names.get(i);
            if (n.length()==0)     throw new ErrorSyntax(span(), "Variable name cannot be empty.");
            if (n.indexOf('/')>=0) throw new ErrorSyntax(span(), "Variable name cannot contain \'/\'");
            if (n.indexOf('@')>=0) throw new ErrorSyntax(span(), "Variable name cannot contain \'@\'");
        }
    }

    /**
     * Constructs a new EDecl object with name as the only name.
     *
     * @param pos - the original position in the file (can be null if unknown)
     * @param name - the only name
     * @param value - the expression that the name is quantified over
     *
     * @throws ErrorSyntax if name is equal to ""
     * @throws ErrorSyntax if name contains '/' or '@'
     */
    EDecl (Pos pos, String name, Expr value) throws Err {
        this.pos=(pos==null ? Pos.UNKNOWN : pos);
        this.names=ConstList.make(1,name);
        this.disjoint=false;
        this.disjointPosition=Pos.UNKNOWN;
        this.value=value;
        if (name.length()==0)     throw new ErrorSyntax(span(), "Variable name must not be empty.");
        if (name.indexOf('/')>=0) throw new ErrorSyntax(span(), "Variable name cannot contain \'/\'");
        if (name.indexOf('@')>=0) throw new ErrorSyntax(span(), "Variable name cannot contain \'@\'");
    }

    /** Returns a Pos object representing the entire span of this EDecl and all its subexpressions. */
    Pos span() {
        Pos p=span;
        if (p==null) {
            p=pos.merge(value.span());
            if (disjointPosition!=null) p=p.merge(disjointPosition);
            span=p;
        }
        return p;
    }

    /**
     * Convenience method that checks if there are duplicate names in a EDecl list.
     * @return the first duplicate name if duplicates exist, and returns null otherwise
     */
    static String findDuplicateName (List<EDecl> list) {
        for(int i=0; i<list.size(); i++) {
            EDecl d=list.get(i);
            for(int j=0; j<d.names.size(); j++) {
                String n=d.names.get(j);
                for(int k=j+1; k<d.names.size(); k++) if (d.names.get(k).equals(n)) return n;
                for(int k=i+1; k<list.size(); k++) if (list.get(k).names.contains(n)) return n;
            }
        }
        return null;
    }
}