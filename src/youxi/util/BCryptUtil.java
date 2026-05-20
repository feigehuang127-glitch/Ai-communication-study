package youxi.util;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtil {
    private static final int ROUNDS = 10;

    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(ROUNDS));
    }

    public static boolean check(String plainText, String hash) {
        try {
            return BCrypt.checkpw(plainText, hash);
        } catch (Exception e) {
            return false;
        }
    }
}
