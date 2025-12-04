package MX.unison;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Clase utilitaria para encriptar y desencriptar mensajes usando AES.
 */
public class EncryptionUtil {

    // Clave secreta fija para el cifrado AES. DEBE SER IGUAL EN CLIENTE Y SERVIDOR.
    private static final String SECRET_KEY = "UNISON_MONITOR_KEY";

    private static SecretKeySpec secretKey;

    /**
     * Prepara la clave AES.
     * Crea una clave de 128 bits (16 bytes) a partir de la clave secreta usando SHA-1.
     */
    private static void setKey() {
        MessageDigest sha = null;
        try {
            byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // Usar solo los primeros 16 bytes para AES-128
            secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            System.err.println("Error al configurar la clave de encriptación: " + e.getMessage());
        }
    }

    /**
     * Encripta una cadena de texto.
     * @param strToEncrypt Cadena a encriptar.
     * @return Cadena encriptada en formato Base64.
     */
    public static String encrypt(String strToEncrypt) {
        try {
            setKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // ECB es simple, PKCS5Padding para relleno
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.err.println("Error al encriptar: " + e.getMessage());
        }
        return null;
    }

    /**
     * Desencripta una cadena de texto.
     * @param strToDecrypt Cadena encriptada en formato Base64.
     * @return Cadena desencriptada.
     */
    public static String decrypt(String strToDecrypt) {
        try {
            setKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.err.println("Error al desencriptar: " + e.getMessage());
        }
        return null;
    }
}