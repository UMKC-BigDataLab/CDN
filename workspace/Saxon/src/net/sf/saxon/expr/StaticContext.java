package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.instruct.LocationMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.sort.StringCollator;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;

import javax.xml.transform.SourceLocator;
import java.util.Set;

/**
* A StaticContext contains the information needed while an expression or pattern
* is being parsed. The information is also sometimes needed at run-time.
*/

public interface StaticContext {

    /**
     * Get the system configuration
     * @return the Saxon configuration
     */

    public Configuration getConfiguration();

    /**
     * Get the executable associated with this static context. The Executable generally holds details
     * of function libraries and collations. For freestanding XPath expressions, there will generally
     * be a single executable corresponding one-to-one with the static context object, and which can be
     * created as soon as the Configuration is known.
     * @return the Executable
     */

    public Executable getExecutable();


    /**
     * Construct a dynamic context for early evaluation of constant subexpressions.
     * @return a newly constructed dynamic context
     */

    public XPathContext makeEarlyEvaluationContext();

    /**
     * Get the location map. This is a mapping from short location ids held with each expression or
     * subexpression, to a fully-resolved location in a source stylesheet or query.
     * @return the location map
     */

    public LocationMap getLocationMap();

    /**
     * Issue a compile-time warning.
     * @param message The warning message. This should not contain any prefix such as "Warning".
     * @param locator the location of the construct in question. May be null.
    */

    public void issueWarning(String message, SourceLocator locator);

    /**
     * Get the System ID of the container of the expression. This is the containing
     * entity (file) and is therefore useful for diagnostics. Use getBaseURI() to get
     * the base URI, which may be different.
     * @return the system ID
     */

    public String getSystemId();

    /**
     * Get the line number of the expression within its containing entity
     * Returns -1 if no line number is available
     * @return the line number, or -1 if not available
    */

    public int getLineNumber();

    /**
     * Get the Base URI of the stylesheet element, for resolving any relative URI's used
     * in the expression.
     * Used by the document(), doc(), resolve-uri(), and base-uri() functions.
     * May return null if the base URI is not known.
     * @return the static base URI, or null if not known
    */

    public String getBaseURI();

    /**
     * Get the URI for a namespace prefix. The default namespace is NOT used
     * when the prefix is empty.
     * @param prefix The namespace prefix.
     * @return the corresponding namespace URI
     * @throws XPathException if the prefix is not declared
    */

    public String getURIForPrefix(String prefix) throws XPathException;

    /**
     * Get the NamePool used for compiling expressions
     * @return the name pool
     */

    public NamePool getNamePool();

    /**
     * Bind a variable used in this element to the XSLVariable element in which it is declared
     * @param qName The name of the variable
     * @return an expression representing the variable reference, This will often be
     * a {@link VariableReference}, suitably initialized to refer to the corresponding variable declaration,
     * but in general it can be any expression.
    */

    public Expression bindVariable(StructuredQName qName) throws XPathException;

    /**
     * Get the function library containing all the in-scope functions available in this static
     * context
     * @return the function library
     */

    public FunctionLibrary getFunctionLibrary();

    /**
    * Get a named collation.
    * @param name The name of the required collation. Supply null to get the default collation.
    * @return the collation; or null if the required collation is not found.
    */

    public StringCollator getCollation(String name);

    /**
    * Get the name of the default collation.
    * @return the name of the default collation; or the name of the codepoint collation
    * if no default collation has been defined
    */

    public String getDefaultCollationName();

    /**
     * Get the default XPath namespace for elements and types
     * @return the default namespace, or NamespaceConstant.NULL for the non-namespace
     */

    public String getDefaultElementNamespace();

    /**
     * Get the default function namespace
     * @return the default namespace for function names
     */

    public String getDefaultFunctionNamespace();

    /**
     * Ask whether Backwards Compatible Mode is used
     * @return true if running in XPath 1.0 compatibility mode
    */

    public boolean isInBackwardsCompatibleMode();

    /**
     * Ask whether a Schema for a given target namespace has been imported. Note that the
     * in-scope element declarations, attribute declarations and schema types are the types registered
     * with the (schema-aware) configuration, provided that their namespace URI is registered
     * in the static context as being an imported schema namespace. (A consequence of this is that
     * within a Configuration, there can only be one schema for any given namespace, including the
     * null namespace).
     * @param namespace the target namespace in question
     * @return true if the given namespace has been imported
     */

    public boolean isImportedSchema(String namespace);

    /**
     * Get the set of imported schemas
     * @return a Set, the set of URIs representing the target namespaces of imported schemas,
     * using the zero-length string to denote the "null" namespace.
     */

    public Set getImportedSchemaNamespaces();

    /**
     * Ask whether a built-in type is available in this context. This method caters for differences
     * between host languages as to which set of types are built in.
     * @param type the supposedly built-in type. This will always be a type in the
     * XS namespace.
     * @return true if this type can be used in this static context
     */

    public boolean isAllowedBuiltInType(BuiltInAtomicType type);

    /**
     * Get a namespace resolver to resolve the namespaces declared in this static context.
     * @return a namespace resolver.
     */

    NamespaceResolver getNamespaceResolver();

    /**
     * Get a DecimalFormatManager to resolve the names of decimal formats used in calls
     * to the format-number() function.
     * @return the decimal format manager for this static context, or null if named decimal
     * formats are not supported in this environment.
     */

    DecimalFormatManager getDecimalFormatManager();

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
