package org.bouncycastle.util;

import j2me.util.Collection;

public interface Store
{
    Collection getMatches(Selector selector)
        throws StoreException;
}
