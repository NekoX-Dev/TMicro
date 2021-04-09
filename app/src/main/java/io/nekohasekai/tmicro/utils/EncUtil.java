package io.nekohasekai.tmicro.utils;

import j2me.math.BigInteger;
import j2me.security.SecureRandom;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.Arrays;
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

    private static ECDomainParameters sm2Params;

    public static ECDomainParameters getSM2DomainParams() {
        if (sm2Params == null) {
            sm2Params = EncUtil.toDomainParams(CustomNamedCurves.getByName("sm2p256v1"));
        }
        return sm2Params;
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
        getSM2DomainParams();
        try {
            pubKey = new ECPublicKeyParameters(sm2Params.getCurve().decodePoint(IoUtil.readResBytes("public.key")), sm2Params);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    public static String publicEncode(byte[] content) {
        loadPubKey();
        return Base64.toBase64String(processSM2(pubKey, true, content));
    }

    public static ECPrivateKeyParameters generateSM2PrivateKey() {
        getSM2DomainParams();
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(new ECKeyGenerationParameters(getSM2DomainParams(), secureRandom));
        AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
        return (ECPrivateKeyParameters) keyPair.getPrivate();
    }

    public static ECPrivateKeyParameters loadSM2PrivateKey(byte[] key) {
        getSM2DomainParams();
        return new ECPrivateKeyParameters(new BigInteger(key), sm2Params);
    }

    public static ECPublicKeyParameters generateSM2PublicKey(ECPrivateKeyParameters privateKey) {
        ECPoint Q = new FixedPointCombMultiplier().multiply(sm2Params.getG(), privateKey.getD());
        return new ECPublicKeyParameters(Q, sm2Params);
    }

    public static byte[] mkChaChaKey() {
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return key;
    }

    public static class ChaChaSession {

        private final byte[] key;
        private final SecureRandom nonceIn;
        private final SecureRandom nonceOut;
        public boolean isServer = false;
        private final ChaCha20Poly1305 cipher = new ChaCha20Poly1305();

        public ChaChaSession(byte[] key) {
            this.key = key;
            this.nonceIn = new SecureRandom(genSeed(false));
            this.nonceOut = new SecureRandom(genSeed(true));
        }

        /**
         * @noinspection SameParameterValue
         */
        private byte[] genSeed(boolean output) {
            if (output ^ isServer) {
                return Arrays.append(key, (byte) 0);
            } else {
                return Arrays.append(key, (byte) 1);
            }
        }

        private byte[] process(boolean forEncryption, byte[] content, byte[] nonce) throws CryptoException {
            AEADParameters parameters = new AEADParameters(new KeyParameter(key), 128, nonce);
            cipher.init(forEncryption, parameters);
            byte[] result = new byte[content.length * 2];
            int offset = cipher.processBytes(content, 0, content.length, result, 0);
            return Arrays.copyOfRange(result, 0, offset + cipher.doFinal(result, offset));
        }

        public byte[] mkMessage(byte[] content) throws CryptoException {
            byte[] nonce = new byte[12];
            nonceOut.nextBytes(nonce);
            return process(true, content, nonce);
        }

        public byte[] readMessage(byte[] message) throws CryptoException {
            byte[] nonce = new byte[12];
            nonceIn.nextBytes(nonce);
            return process(false, message, nonce);
        }

    }


}


















