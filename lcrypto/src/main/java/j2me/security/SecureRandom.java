package j2me.security;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import org.bouncycastle.crypto.prng.RandomGenerator;

/**
 * An implementation of SecureRandom specifically for the light-weight API, JDK
 * 1.0, and the J2ME. Random generation is based on the traditional SHA1 with
 * counter. Calling setSeed will always increase the entropy of the hash.
 * <p>
 * <b>Do not use this class without calling setSeed at least once</b>! There
 * are some example seed generators in the org.bouncycastle.prng package.
 */
public class SecureRandom extends java.util.Random {
    public static final SecureRandom INSTANCE = new SecureRandom();

    protected RandomGenerator generator;

    protected SecureRandom(
            RandomGenerator generator) {
        super(0);
        this.generator = generator;
    }

    public SecureRandom() {
        this(new DigestRandomGenerator(new SM3Digest()));
        setSeed(System.currentTimeMillis());
    }

    public SecureRandom(byte[] inSeed) {
        this();
        setSeed(inSeed);
    }

    public String getAlgorithm() {
        return "unknown";
    }

    // public instance methods
    public byte[] generateSeed(int numBytes) {
        byte[] rv = new byte[numBytes];

        nextBytes(rv);

        return rv;
    }

    // public final Provider getProvider();
    public void setSeed(byte[] inSeed) {
        generator.addSeedMaterial(inSeed);
    }

    // public methods overriding random
    public void nextBytes(byte[] bytes) {
        generator.nextBytes(bytes);
    }

    public void setSeed(long rSeed) {
        if (rSeed != 0)    // to avoid problems with Random calling setSeed in construction
        {
            generator.addSeedMaterial(rSeed);
        }
    }

    public int nextInt() {
        byte[] intBytes = new byte[4];

        nextBytes(intBytes);

        int result = 0;

        for (int i = 0; i < 4; i++) {
            result = (result << 8) + (intBytes[i] & 0xff);
        }

        return result;
    }

    protected final int next(int numBits) {
        int size = (numBits + 7) / 8;
        byte[] bytes = new byte[size];

        nextBytes(bytes);

        int result = 0;

        for (int i = 0; i < size; i++) {
            result = (result << 8) + (bytes[i] & 0xff);
        }

        return result & ((1 << numBits) - 1);
    }
}
