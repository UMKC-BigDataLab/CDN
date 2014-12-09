package net.sf.saxon.style;

import net.sf.saxon.sort.StringCollator;

/**
 * Interface implemented by SaxonCollation, which is not available in Saxon-HE
 */
public interface CollationDeclaration {

    public String getCollationName();

    /**
     * Get the collator defined by this collation declaration
     * @return the StringCollator
     */

    public StringCollator getCollator();
    
}

//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//

