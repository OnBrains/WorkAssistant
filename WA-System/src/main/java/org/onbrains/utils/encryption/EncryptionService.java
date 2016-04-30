package org.onbrains.utils.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Сервис для шифрования и проверки пароля.
 * 
 * @author Naumov Oleg on 22.04.2016.
 */
public class EncryptionService {

	private static final String DEFAULT_SALT = "J2-e`5_=";

	public static String hash(String password, String salt) {
		return DigestUtils.sha256Hex(password.concat(salt).concat(DEFAULT_SALT));
	}

	public static String salt() {
		Random random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return new String(salt);
	}

}