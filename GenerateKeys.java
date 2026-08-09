import java.security.*;
import java.util.Base64;
import java.nio.file.*;

public class GenerateKeys {
    public static void main(String[] args) throws Exception {
        String certsDir = args.length > 0 ? args[0] : ".";
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair kp = gen.generateKeyPair();
        Base64.Encoder enc = Base64.getMimeEncoder(64, new byte[]{'\n'});
        String priv = "-----BEGIN PRIVATE KEY-----\n" + enc.encodeToString(kp.getPrivate().getEncoded()) + "\n-----END PRIVATE KEY-----";
        String pub  = "-----BEGIN PUBLIC KEY-----\n"  + enc.encodeToString(kp.getPublic().getEncoded())  + "\n-----END PUBLIC KEY-----";
        Files.writeString(Path.of(certsDir, "dev-private.pem"), priv);
        Files.writeString(Path.of(certsDir, "dev-public.pem"),  pub);
        System.out.println("RSA keys generated in: " + certsDir);
    }
}

