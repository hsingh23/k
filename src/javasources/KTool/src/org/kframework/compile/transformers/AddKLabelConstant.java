package org.kframework.compile.transformers;

import org.kframework.kil.*;
import org.kframework.kil.visitors.CopyOnWriteTransformer;
import org.kframework.kil.visitors.exceptions.TransformerException;

import java.util.ArrayList;

/** Define isKLabelConstant predicate */
public class AddKLabelConstant extends CopyOnWriteTransformer {

    private static final KLabelConstant KLabelConstantPredicate =
            KLabelConstant.of(AddPredicates.predicate("KLabelConstant"));

    public AddKLabelConstant(org.kframework.kil.loader.Context context) {
        super("Define isKLabelConstant predicate for KLabel constants", context);
    }

    @Override
    public ASTNode transform(Module node) throws TransformerException {
        Module retNode = node.shallowCopy();
        retNode.setItems(new ArrayList<ModuleItem>(node.getItems()));

        // declare the isKLabelConstant predicate as KLabel
        retNode.addConstant(KLabelConstantPredicate);

        for (String klbl : node.getModuleKLabels()) {
            Term kapp = KApp.of(new KInjectedLabel(KLabelConstant.of(klbl, context)));
            Term lhs = KApp.of(KLabelConstantPredicate, kapp);
            Term rhs = BoolBuiltin.TRUE;
            Rule rule = new Rule(lhs, rhs, context);
            rule.addAttribute(Attribute.PREDICATE);
            retNode.appendModuleItem(rule);
        }

        if (retNode.getItems().size() != node.getItems().size())
            return retNode;
        else
            return node;
    }

}

