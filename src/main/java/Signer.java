import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Signer
{
    AsymmetricCipherKeyPair keys;
    public void setup()
    {
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16), new SecureRandom(), 2048, 80));
        this.keys = generator.generateKeyPair();
    }
    public byte[]sign(RequestForSign requestForSign) {
        byte[] message = requestForSign.getMessage();
        RSAEngine engine = new RSAEngine();
        engine.init(true, keys.getPrivate());
        return engine.processBlock(message, 0, message.length);
    }
    public boolean verify(SignedPackage signedPackage) {
        byte[]id = signedPackage.getId();
        byte[]signature = signedPackage.getSignature();
        PSSSigner signer = new PSSSigner(new RSAEngine(), new SHA1Digest(), 20);
        signer.init(false, keys.getPublic());
        signer.update(id, 0, id.length);
        return signer.verifySignature(signature);
    }
    public KeyPair convertBcToJceKeyPair() throws Exception {
        byte[] pkcs8Encoded = PrivateKeyInfoFactory.createPrivateKeyInfo(keys.getPrivate()).getEncoded();
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(pkcs8Encoded);
        byte[] spkiEncoded = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(keys.getPublic()).getEncoded();
        X509EncodedKeySpec spkiKeySpec = new X509EncodedKeySpec(spkiEncoded);
        KeyFactory keyFac = KeyFactory.getInstance("RSA");
        return new KeyPair(keyFac.generatePublic(spkiKeySpec), keyFac.generatePrivate(pkcs8KeySpec));
    }
    public AsymmetricCipherKeyPair getKeys() {
        return keys;
    }
}
