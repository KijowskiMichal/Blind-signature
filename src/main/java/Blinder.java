import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RSABlindingEngine;
import org.bouncycastle.crypto.generators.RSABlindingFactorGenerator;
import org.bouncycastle.crypto.params.RSABlindingParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.PSSSigner;

import java.math.BigInteger;

public class Blinder
{
    byte[]id;
    RSABlindingParameters blindingParams;
    public void setup(byte[]id, RSAKeyParameters pubkey)
    {
        this.id = id;
        RSABlindingFactorGenerator blindingFactorGenerator = new RSABlindingFactorGenerator();
        blindingFactorGenerator.init(pubkey);
        BigInteger blindingFactor = blindingFactorGenerator.generateBlindingFactor();
        this.blindingParams = new RSABlindingParameters(pubkey, blindingFactor);
    }
    public RequestForSign generateRequest(){
        try
        {
            PSSSigner signer = new PSSSigner(new RSABlindingEngine(), new SHA1Digest(), 20);
            signer.init(true, blindingParams);
            signer.update(id, 0, id.length);
            byte[] signature = signer.generateSignature();
            RequestForSign requestForSign = new RequestForSign();
            requestForSign.setup(signature);
            return requestForSign;
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        return null;
    }
    public SignedPackage createSignedPackage(byte[] signature) {
        RSABlindingEngine blindingEngine = new RSABlindingEngine();
        blindingEngine.init(false, blindingParams);
        byte[] s = blindingEngine.processBlock(signature, 0, signature.length);
        SignedPackage signedPackage = new SignedPackage();
        signedPackage.setup(s, id);
        return signedPackage;
    }
}
