/*
 * Alloy Analyzer 4 -- Copyright (c) 2006-2008, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4compiler.sim;

/**
 * Immutable; represents a tuple.
 *
 * <p><b>Thread Safety:</b>  Safe.
 */

public final class SimTuple {

    /** Stores the tuple. */
    private SimAtom[] array;

    /** If nonzero, it caches the hash code. */
    private int hashCode = 0;

    /** Construct a tuple backed by the given array as-is; thus the caller must not modify it any more. */
    private SimTuple(SimAtom[] array) {
        if (array.length==0) throw new IllegalArgumentException();
        this.array=array;
    }

    /** Construct the binary tuple (a,b) */
    public static SimTuple make(SimAtom a, SimAtom b) {
        return new SimTuple(new SimAtom[]{a, b});
    }

    /** Construct the unary tuple containing the given atom. */
    public static SimTuple make(SimAtom atom) {
        return new SimTuple(new SimAtom[]{atom});
    }

    /** Construct the unary tuple containing the given atom. */
    public static SimTuple make(String atom) {
        return new SimTuple(new SimAtom[]{SimAtom.make(atom)});
    }

    /** Construct the tuple containing the given list of atoms; the list must not be empty. */
    public static SimTuple make(String[] atoms) {
        SimAtom[] ans = new SimAtom[atoms.length];
        for(int i=0; i<atoms.length; i++) ans[i] = SimAtom.make(atoms[i]);
        return new SimTuple(ans);
    }

    /** Returns the arity of this tuple. */
    public int arity() { return array.length; }

    /** Return the i-th atom from this tuple. */
    public SimAtom get(int i) { return array[i]; }

    /** Return the product of this tuple and that tuple. */
    public SimTuple product(SimTuple that) {
        SimAtom[] c = new SimAtom[array.length + that.array.length]; // If integer overflows, we'll get an exception here, which is good
        for(int i=0; i<this.array.length; i++) c[i] = array[i];
        for(int i=0; i<that.array.length; i++) c[i+array.length] = that.array[i];
        return new SimTuple(c);
    }

    /** Return the relational join of this tuple and that tuple; throws an exception if the join point doesn't match or if both sides are unary. */
    public SimTuple join(SimTuple that) {
        if (array.length+that.array.length==2 || array[array.length-1]!=that.array[0]) throw new IllegalArgumentException();
        SimAtom[] c = new SimAtom[array.length + that.array.length - 2]; // If integer overflows, we'll get an exception here, which is good
        for(int i=0; i<this.array.length-1; i++) c[i] = array[i];
        for(int i=0; i<that.array.length-1; i++) c[i+array.length-1] = that.array[i+1];
        return new SimTuple(c);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        int ans = hashCode;
        if (ans == 0) {
            // We already know each SimAtom has been canonicalized, so just computing its IdentityHashCode is faster
            for(int i=array.length-1; i>=0; i--) ans = ans*31 + System.identityHashCode(array[i]);
            hashCode = ans;
        }
        return ans;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object that) {
        if (this==that) return true; else if (!(that instanceof SimTuple)) return false;
        SimAtom[] other = ((SimTuple)that).array;
        if (array==other) return true; else if (array.length != other.length) return false;
        if (hashCode() != that.hashCode()) return false;
        for(int i=array.length-1; i>=0; i--) if (array[i]!=other[i]) return false;
        array=other; // Change it so we share the same array; this is thread safe since these array contents are never mutated, so it doesn't matter if some thread sees the old array and some sees the new array
        return true;
    }

    /** Returns the first atom of this tuple. */
    public SimAtom head() { return array[0]; }

    /** Returns the last atom of this tuple. */
    public SimAtom tail() { return array[array.length-1]; }

    /** Returns the subtuple containing the first n atoms (n must be between 1 and arity) */
    public SimTuple head(int n) {
        if (n<=0 || n>array.length) throw new IllegalArgumentException(); else if (n==array.length) return this;
        SimAtom newtuple[] = new SimAtom[n];
        for(int c=0; c<newtuple.length; c++) newtuple[c] = array[c];
        return new SimTuple(newtuple);
    }

    /** Returns the subtuple containing the last n atoms (n must be between 1 and arity) */
    public SimTuple tail(int n) {
        if (n<=0 || n>array.length) throw new IllegalArgumentException(); else if (n==array.length) return this;
        SimAtom newtuple[] = new SimAtom[n];
        for(int a=array.length-n, c=0; c<newtuple.length; c++) newtuple[c] = array[c+a];
        return new SimTuple(newtuple);
    }

    /** Write a human-readable representation of this tuple into the given StringBuilder object. */
    public void toString(StringBuilder sb) {
        for(int i=0; i<array.length; i++) {
            if (i>0) sb.append("->");
            sb.append(array[i]);
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }
}