package org.bouncycastle.crypto.params;

import j2me.math.BigInteger;

public class GOST3410PublicKeyParameters
        extends GOST3410KeyParameters
{
    private BigInteger      y;

    public GOST3410PublicKeyParameters(
        BigInteger      y,
        GOST3410Parameters   params)
    {
        super(false, params);

        this.y = y;
    }

    public BigInteger getY()
    {
        return y;
    }
}
