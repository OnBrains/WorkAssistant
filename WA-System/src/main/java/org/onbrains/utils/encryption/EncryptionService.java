package org.onbrains.utils.encryption;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Сервис для шифрования и проверки пароля.
 * 
 * @author Naumov Oleg on 22.04.2016.
 */
public class EncryptionService {

	private static final String ENCTYPT_SIMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz,-/+=?.`~";
	private static final String DEFAULT_SALT = "J2-e`5_=";

	public static String hash(String password, String salt) {
		return DigestUtils.sha256Hex(password.concat(salt).concat(DEFAULT_SALT));
	}

	public static String salt() {
		Random random = new SecureRandom();
		StringBuilder salt = new StringBuilder(16);
		for (int i = 0; i < 16; i++) {
			salt.append(ENCTYPT_SIMBOLS.charAt(random.nextInt(ENCTYPT_SIMBOLS.length())));
		}
		return salt.toString();
	}

}