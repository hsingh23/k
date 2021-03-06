package org.kframework.kcheck.utils;

import org.kframework.kcheck.RLBackend;
import org.kframework.kil.ASTNode;
import org.kframework.kil.BoolBuiltin;
import org.kframework.kil.KApp;
import org.kframework.kil.KList;
import org.kframework.kil.Term;
import org.kframework.kil.loader.Context;
import org.kframework.kil.visitors.CopyOnWriteTransformer;
import org.kframework.kil.visitors.exceptions.TransformerException;

public class ExtractPatternless extends CopyOnWriteTransformer {

	private Term phi = BoolBuiltin.TRUE, phiPrime = BoolBuiltin.TRUE;
	private boolean remove = true;
	
	public ExtractPatternless(Context context, boolean remove) {
		super("Extract encoded patternless formula from term", context);
		this.remove = remove;
	}

	@Override
	public ASTNode transform(KApp node) throws TransformerException {
		if (node.getLabel().toString().equals(RLBackend.INTERNAL_KLABEL)) {
			phi = ((KList) node.getChild()).getContents().get(0);
			phiPrime = ((KList) node.getChild()).getContents().get(1);
			if (remove)
				return BoolBuiltin.TRUE;
			return node;
		}
		
		return super.transform(node);
	}

	public Term getPhi() {
		return phi;
	}

	public Term getPhiPrime() {
		return phiPrime;
	}
}
