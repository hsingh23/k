package org.kframework.backend.java.kil;

import org.kframework.backend.java.symbolic.Unifier;
import org.kframework.backend.java.symbolic.Transformer;
import org.kframework.backend.java.symbolic.Visitor;
import org.kframework.backend.java.util.KSorts;
import org.kframework.kil.ASTNode;


/**
 * A hole (a term of the form "HOLE").
 *
 * @author AndreiS
 */
public class Hole extends Term implements Sorted {

    public static final Hole HOLE = new Hole();

    private Hole() {
        super(Kind.KITEM);
    }

    @Override
    public boolean isSymbolic() {
        return false;
    }

    /**
     * Returns a {@code String} representation of the sort of this object.
     */
    @Override
    public String sort() {
        return KSorts.KITEM;
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "HOLE";
    }

    @Override
    public void accept(Unifier unifier, Term patten) {
        unifier.unify(this, patten);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(Transformer transformer) {
        return transformer.transform(this);
    }

    /**
     * Returns the HOLE constant in this session rather than the de-serialized constant.
     */
    private Object readResolve() {
        return HOLE;
    }

}
