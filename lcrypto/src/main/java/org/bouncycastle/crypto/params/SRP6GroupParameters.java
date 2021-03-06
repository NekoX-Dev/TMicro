package org.bouncycastle.crypto.params;

import j2me.math.BigInteger;

public class SRP6GroupParameters
{
    private BigInteger N, g;

    public SRP6GroupParameters(BigInteger N, BigInteger g)
    {
        this.N = N;
        this.g = g;
    }

    public BigInteger getG()
    {
        return g;
    }

    public BigInteger getN()
    {
        return N;
    }
}
