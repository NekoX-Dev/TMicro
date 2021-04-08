package org.bouncycastle.crypto.params;

import j2me.security.SecureRandom;

import org.bouncycastle.crypto.KeyGenerationParameters;

public class Ed448KeyGenerationParameters
    extends KeyGenerationParameters
{
    public Ed448KeyGenerationParameters(SecureRandom random)
    {
        super(random, 448);
    }
}
