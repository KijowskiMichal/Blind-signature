public class SignedPackage
{
    byte[]signature=null;
    byte[]id=null;
    public void setup(byte[]signature,byte[]id)
    {
        this.signature = signature;
        this.id = id;
    }
    public byte[] getSignature() {
        return signature;
    }
    public byte[] getId() {
        return id;
    }
}
