package org.bouncycastle.crypto.params;

import j2me.security.SecureRandom;

import org.bouncycastle.crypto.KeyGenerationParameters;

public class X25519KeyGenerationParameters
    extends KeyGenerationParameters
{
    public X25519KeyGenerationParameters(SecureRandom random)
    {
        super(random, 255);
    }
}
