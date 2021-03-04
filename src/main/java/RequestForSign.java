public class RequestForSign
{
    byte[]message = null;
    public void setup(byte[]message)
    {
        this.message = message;
    }
    public byte[] getMessage() {
        return message;
    }
}
