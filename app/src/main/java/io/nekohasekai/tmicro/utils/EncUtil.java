package io.nekohasekai.tmicro.utils;

import j2me.security.SecureRandom;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.util.encoders.Base64;

public class EncUtil {

    public static SecureRandom secureRandom = new SecureRandom();

    public static byte[] processSM2(CipherParameters key, boolean forEncryption, byte[] content) {

        SM2Engine engine = new SM2Engine();
        engine.init(forEncryption, forEncryption ? new ParametersWithRandom(key, secureRandom) : key);
        try {
            return engine.processBlock(content, 0, content.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }

    }

    public static ECDomainParameters getSM2DomainParams() {
        //noinspection ConstantConditions
        return EncUtil.toDomainParams(CustomNamedCurves.getByName("sm2p256v1"));
    }

    public static ECDomainParameters toDomainParams(X9ECParameters x9ECParameters) {
        return new ECDomainParameters(
                x9ECParameters.getCurve(),
                x9ECParameters.getG(),
                x9ECParameters.getN(),
                x9ECParameters.getH()
        );
    }

    private static ECPublicKeyParameters pubKey;

    public static void loadPubKey() {
        try {
            ECDomainParameters params = getSM2DomainParams();
            pubKey = new ECPublicKeyParameters(params.getCurve().decodePoint(IoUtil.readResBytes("public.key")), params);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    public static String publicEncode(byte[] content) {
        loadPubKey();
        return Base64.toBase64String(processSM2(pubKey, true, content));
    }

    public static byte[] generateChaCha20Poly1305Key() {
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return key;
    }

    public static byte[] processChaCha20Poly1305(byte[] key, boolean forEncryption, byte[] content) {
        byte[] nonce = new byte[12];
        secureRandom.nextBytes(nonce);

        AEADParameters parameters = new AEADParameters(new KeyParameter(key), 128, nonce);
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        cipher.init(forEncryption, parameters);
        cipher.processBytes(content, 0, content.length, content, 0);
        return content;
    }

}


















