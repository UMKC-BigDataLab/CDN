package net.sf.saxon.instruct;
import net.sf.saxon.trans.Err;
import net.sf.saxon.event.ReceiverOptions;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.expr.*;
import net.sf.saxon.functions.StringFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Orphan;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.*;
import net.sf.saxon.value.Value;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.value.UntypedAtomicValue;

/**
* An xsl:value-of element in the stylesheet. <br>
* The xsl:value-of element takes attributes:<ul>
* <li>a mandatory attribute select="expression".
* This must be a valid String expression</li>
* <li>an optional disable-output-escaping attribute, value "yes" or "no"</li>
* <li>an optional separator attribute. This is handled at compile-time: if the separator attribute
* is present, the select expression passed in here will be a call to the string-join() function.</li>
* </ul>
*/

public final class ValueOf extends SimpleNodeConstructor {

    private int options;
    private boolean isNumberingInstruction = false;  // set to true if generated by xsl:number
    private boolean noNodeIfEmpty;

    /**
     * Create a new ValueOf expression
     * @param select the select expression
     * @param disable true if disable-output-escaping is in force
     * @param noNodeIfEmpty true if the instruction is to return () if the select expression is (),
     * false if it is to return an empty text node
     */

    public ValueOf(Expression select, boolean disable, boolean noNodeIfEmpty) {
        this.select = select;
        options = (disable ? ReceiverOptions.DISABLE_ESCAPING : 0);
        this.noNodeIfEmpty = noNodeIfEmpty;
        adoptChildExpression(select);

        // If value is fixed, test whether there are any special characters that might need to be
        // escaped when the time comes for serialization
        if (select instanceof StringLiteral) {
            boolean special = false;
            CharSequence val = ((StringLiteral)select).getStringValue();
            for (int k=0; k<val.length(); k++) {
                char c = val.charAt(k);
                if ((int)c<33 || (int)c>126 ||
                         c=='<' || c=='>' || c=='&') {
                    special = true;
                    break;
                 }
            }
            if (!special) {
                options |= ReceiverOptions.NO_SPECIAL_CHARS;
            }
        }
    }

    /**
     * Indicate that this is really an xsl:nunber instruction
     */

    public void setIsNumberingInstruction() {
        isNumberingInstruction = true;
    }

    /**
     * Determine whether this is really an xsl:number instruction
     * @return true if this derives from xsl:number
     */

    public boolean isNumberingInstruction() {
        return isNumberingInstruction;
    }

    /**
     * Get the name of this instruction for diagnostic and tracing purposes
     * @return the namecode of the instruction name
    */

    public int getInstructionNameCode() {
        if (isNumberingInstruction) {
            return StandardNames.XSL_NUMBER;
        } else if (select instanceof StringLiteral) {
            return StandardNames.XSL_TEXT;
        } else {
            return StandardNames.XSL_VALUE_OF;
        }
    }

    /**
    * Offer promotion for subexpressions. The offer will be accepted if the subexpression
    * is not dependent on the factors (e.g. the context item) identified in the PromotionOffer.
    * By default the offer is not accepted - this is appropriate in the case of simple expressions
    * such as constant values and variable references where promotion would give no performance
    * advantage. This method is always called at compile time.
    *
    * @param offer details of the offer, for example the offer to move
    *     expressions that don't depend on the context to an outer level in
    *     the containing expression
    * @exception XPathException if any error is detected
    */

    protected void promoteInst(PromotionOffer offer) throws XPathException {
        super.promoteInst(offer);
    }

    /**
     * Test for any special options such as disable-output-escaping
     * @return any special options
     */

    public int getOptions() {
        return options;
    }

    /**
     * Test whether disable-output-escaping was requested
     * @return  true if disable-output-escaping was requested
     */

    public boolean isDisableOutputEscaping() {
        return (options & ReceiverOptions.DISABLE_ESCAPING) != 0;
    }

    public ItemType getItemType(TypeHierarchy th) {
        return NodeKindTest.TEXT;
    }

    public int computeCardinality() {
        if (noNodeIfEmpty) {
            return StaticProperty.ALLOWS_ZERO_OR_ONE;
        } else {
            return StaticProperty.EXACTLY_ONE;
        }
    }

    public void localTypeCheck(ExpressionVisitor visitor, ItemType contextItemType) {
       //
    }

    /**
     * Copy an expression. This makes a deep copy.
     *
     * @return the copy of the original expression
     */

    public Expression copy() {
        ValueOf exp = new ValueOf(select.copy(), (options&ReceiverOptions.DISABLE_ESCAPING) != 0 , noNodeIfEmpty);
        if (isNumberingInstruction) {
            exp.setIsNumberingInstruction();
        }
        return exp;
    }

    /**
      * Check statically that the results of the expression are capable of constructing the content
      * of a given schema type.
      *
      * @param parentType The schema type
      * @param env        the static context
      * @param whole true if this expression is to account for the whole value of the type
      * @throws net.sf.saxon.trans.XPathException
      *          if the expression doesn't match the required content type
      */

     public void checkPermittedContents(SchemaType parentType, StaticContext env, boolean whole) throws XPathException {
         // if the expression is a constant value, check that it is valid for the type
         if (select instanceof Literal) {
             Value selectValue = ((Literal)select).getValue();
             SimpleType stype = null;
             if (parentType instanceof SimpleType && whole) {
                 stype = (SimpleType)parentType;
             } else if (parentType instanceof ComplexType && ((ComplexType)parentType).isSimpleContent()) {
                 stype = ((ComplexType)parentType).getSimpleContentType();
             }
             if (whole && stype != null && !stype.isNamespaceSensitive()) {
                        // Can't validate namespace-sensitive content statically
                 ValidationFailure err = stype.validateContent(
                         selectValue.getStringValue(), null, env.getConfiguration().getNameChecker());
                 if (err != null) {
                     err.setLocator(this);
                     throw err.makeException();
                 }
                 return;
             }
             if (parentType instanceof ComplexType &&
                     !((ComplexType)parentType).isSimpleContent() &&
                     !((ComplexType)parentType).isMixedContent() &&
                     !Whitespace.isWhite(selectValue.getStringValue())) {
                 XPathException err = new XPathException("Complex type " + parentType.getDescription() +
                         " does not allow text content " +
                         Err.wrap(selectValue.getStringValue()));
                 err.setLocator(this);
                 err.setIsTypeError(true);
                 throw err;
             }
         }
    }

    /**
     * Convert this value-of instruction to an expression that delivers the string-value of the resulting
     * text node. This will often be a call on the string-join function.
     * @param env the static evaluation context
     * @return the converted expression
     */

    public Expression convertToStringJoin(StaticContext env) {
        if (select.getItemType(env.getConfiguration().getTypeHierarchy()).equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            return select;
        } else if (select instanceof StringLiteral) {
            try {
                return new Literal(new UntypedAtomicValue(((StringLiteral)select).getValue().getStringValueCS()));
            } catch (XPathException err) {
                throw new AssertionError(err);
            }
        } else {
            StringFn fn = (StringFn) SystemFunction.makeSystemFunction("string", new Expression[]{select});
            return new CastExpression(fn, BuiltInAtomicType.UNTYPED_ATOMIC, false);
        }
    }


    /**
     * Process this instruction, sending the resulting text node to the current output destination
     * @param context the dynamic evaluation context
     * @return Always returns null
     * @throws XPathException
     */

    public TailCall processLeavingTail(XPathContext context) throws XPathException {
        SequenceReceiver out = context.getReceiver();
        if (select instanceof SimpleContentConstructor) {
            ((SimpleContentConstructor)select).process(context, locationId, options);
        } else {
            Item item = select.evaluateItem(context);
            if (item != null) {
                out.characters(item.getStringValueCS(), locationId, options);
            }
        }
        return null;
    }

    /**
     * Process the value of the node, to create the new node.
     * @param value   the string value of the new node
     * @param context the dynamic evaluation context
     * @throws net.sf.saxon.trans.XPathException
     *
     */

    protected void processValue(CharSequence value, XPathContext context) throws XPathException {
        SequenceReceiver out = context.getReceiver();
        out.characters(value, locationId, options);
    }

    /**
     * Evaluate this expression, returning the resulting text node to the caller
     * @param context the dynamic evaluation context
     * @return the parentless text node that results from evaluating this instruction, or null to
     * represent an empty sequence
     * @throws XPathException
     */

    public Item evaluateItem(XPathContext context) throws XPathException {
        try {
            CharSequence val;
            Item item = select.evaluateItem(context);
            if (item == null) {
                if (noNodeIfEmpty) {
                    return null;
                } else {
                    val = "";
                }
            } else {
                val = item.getStringValueCS();
            }
            Orphan o = new Orphan(context.getController().getConfiguration());
            o.setNodeKind(Type.TEXT);
            o.setStringValue(val);
            return o;
        } catch (XPathException err) {
            err.maybeSetLocation(this);
            throw err;
        }
    }

    /**
     * Diagnostic print of expression structure. The abstract expression tree
     * is written to the supplied output destination.
     */

    public void explain(ExpressionPresenter out) {
        out.startElement("valueOf");
        getSelect().explain(out);
        out.endElement();
    }
}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
