package org.bouncycastle.crypto.params;

import j2me.security.SecureRandom;

import org.bouncycastle.crypto.KeyGenerationParameters;

public class X448KeyGenerationParameters
    extends KeyGenerationParameters
{
    public X448KeyGenerationParameters(SecureRandom random)
    {
        super(random, 448);
    }
}
