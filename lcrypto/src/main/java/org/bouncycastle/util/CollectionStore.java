package org.bouncycastle.util;

import j2me.util.ArrayList;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.List;

/**
 * A simple collection backed store.
 */
public class CollectionStore
    implements Store, Iterable
{
    private Collection _local;

    /**
     * Basic constructor.
     *
     * @param collection - initial contents for the store, this is copied.
     */
    public CollectionStore(
        Collection collection)
    {
        _local = new ArrayList(collection);
    }

    /**
     * Return the matches in the collection for the passed in selector.
     *
     * @param selector the selector to match against.
     * @return a possibly empty collection of matching objects.
     */
    public Collection getMatches(Selector selector)
    {
        if (selector == null)
        {
            return new ArrayList(_local);
        }
        else
        {
            List col = new ArrayList();
            Iterator iter = _local.iterator();

            while (iter.hasNext())
            {
                Object obj = iter.next();

                if (selector.match(obj))
                {
                    col.add(obj);
                }
            }

            return col;
        }
    }

    /**
     * Support method for Iterable where available.
     */
    public Iterator iterator()
    {
        return getMatches(null).iterator();
    }
}
