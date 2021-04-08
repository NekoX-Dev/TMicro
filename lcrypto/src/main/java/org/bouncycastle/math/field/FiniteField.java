package org.bouncycastle.math.field;

import j2me.math.BigInteger;

public interface FiniteField
{
    BigInteger getCharacteristic();

    int getDimension();
}
