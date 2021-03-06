package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SingletonIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceType;

/**
 * This class implements the saxon:is-whole-number() extension function,
 * which is specially-recognized by the system because calls are generated by the optimizer.
 *
 * <p>The function signature is <code>saxon:is-whole-number($arg as numeric?) as boolean</code></p>
 *
 * <p>The result is true if $arg is not empty and is equal to some integer.</p>
*/

public class IsWholeNumber extends ExtensionFunctionDefinition {

    private static final StructuredQName qName =
            new StructuredQName("", NamespaceConstant.SAXON, "is-whole-number");

    /**
     * Get the function name, as a QName
     * @return the QName of the function
     */

    public StructuredQName getFunctionQName() {
        return qName;
    }

    /**
     * Get the minimum number of arguments required by the function
     * @return the minimum number of arguments that must be supplied in a call to this function
     */

    public int getMinimumNumberOfArguments() {
        return 1;
    }

    /**
     * Get the maximum number of arguments allowed by the function
     * @return the maximum number of arguments that may be supplied in a call to this function
     */

    public int getMaximumNumberOfArguments() {
        return 1;
    }

    /**
     * Get the required types for the arguments of this function, counting from zero
     * @return the required types of the argument, as defined by the function signature. Normally
     *         this should be an array of size {@link #getMaximumNumberOfArguments()}; however for functions
     *         that allow a variable number of arguments, the array can be smaller than this, with the last
     *         entry in the array providing the required type for all the remaining arguments.
     */

    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.OPTIONAL_NUMERIC};
    }

    /**
     * Get the type of the result of the function
     * @param suppliedArgumentTypes the static types of the arguments to the function.
     *    This is provided so that a more precise result type can be returned in the common
     *    case where the type of the result depends on the type of the first argument. The value
     *    will be null if the function call has no arguments.
     * @return the return type of the function, as defined by its function signature
     */

    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_BOOLEAN;
    }

    /**
     * Create a call on this function. This method is called by the compiler when it identifies
     * a function call that calls this function.
     */

    public ExtensionFunctionCall makeCallExpression() {
        return new IsWholeNumberCall();
    }

    /**
     * Return an object capable of compiling this IntegratedFunction call to Java source code. The returned
     * object may be null, in which case Java code generation is not supported for this IntegratedFunction.
     * If the returned object is not null, it must implement the interface
     * com.saxonica.codegen.IntegratedFunctionCompiler. The default implementation returns null
     * @return an instance of com.saxonica.codegen.IntegratedFunctionCompiler that generates Java code
     *         to implement the call on this extension function
     */

    public Object getCompilerForJava() {
        try {
            return Class.forName("com.saxonica.codegen.IsWholeNumberCompiler").newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static class IsWholeNumberCall extends ExtensionFunctionCall {

        /**
         * Evaluate this function call at run-time
         * @param arguments The values of the arguments to the function call. Each argument value (which is in general
         *                  a sequence) is supplied in the form of an iterator over the items in the sequence. If required, the
         *                  supplied sequence can be materialized by calling, for example, <code>new SequenceExtent(arguments[i])</code>.
         *                  If the argument is always a singleton, then the single item may be obtained by calling
         *                  <code>arguments[i].next()</code>. The implementation is not obliged to read all the items in each
         *                  <code>SequenceIterator</code> if they are not required to compute the result; but if any SequenceIterator is not read
         *                  to completion, it is good practice to call its close() method.
         * @param context   The XPath dynamic evaluation context
         * @return an iterator over the results of the function. If the result is a single item, it can be
         *         returned in the form of a {@link net.sf.saxon.om.SingletonIterator}. If the result is an empty sequence,
         *         the method should return <code>EmptyIterator.getInstance()</code>
         * @throws net.sf.saxon.trans.XPathException
         *          if a dynamic error occurs during evaluation of the function. The Saxon run-time
         *          code will add information about the error location.
         */

        public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
            return SingletonIterator.makeIterator(
                    BooleanValue.get(effectiveBooleanValue(arguments, context)));
        }

        /**
         * Get the effective boolean value of the expression. This returns false if the value
         * is the empty sequence, a zero-length string, a number equal to zero, or the boolean
         * false. Otherwise it returns true.
         *
         * @param context The context in which the expression is to be evaluated
         * @return the effective boolean value
         * @throws net.sf.saxon.trans.XPathException
         *          if any dynamic error occurs evaluating the
         *          expression
         */

        public boolean effectiveBooleanValue(SequenceIterator[] arguments, XPathContext context) throws XPathException {
            NumericValue val = (NumericValue)arguments[0].next();
            return val != null && val.isWholeNumber();
        }


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
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Contributor(s):
//
