package com.tms.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.crypto.tink.subtle.EcdsaVerifyJce;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.google.crypto.tink.subtle.EllipticCurves.EcdsaEncoding;
import com.google.crypto.tink.subtle.Enums.HashType;
import com.tms.controller.model.RestResponse;
import com.tms.exception.ValidationException;
import com.tms.properties.SetUpProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.interfaces.ECPublicKey;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ApplicationHelper {

	private static SetUpProperties setup = SetUpProperties.getInstance();

	private ApplicationHelper() {
	}

	public static Boolean isBlocked(final Calendar date) {
		Boolean v = date != null && date.after(Calendar.getInstance());
		return v;
	}

	public static Calendar wrongAttemptsLimit() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return calendar;
	}

	public static Calendar blockPasswordUntil(int minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, minutes);
		return calendar;
	}

	public static void sendError(final String msg, int errorCode) {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		HttpServletResponse response = requestAttributes.getResponse();
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(NON_EMPTY);
		try {
			response.setContentType(APPLICATION_JSON_VALUE);
			response.setStatus(errorCode);
			RestResponse restResponse = new RestResponse();
			restResponse.addError(msg);
			restResponse.setStatus(false);
			mapper.writeValue(response.getWriter(), restResponse);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean checkPasswordPolicy(String password) throws ValidationException {

		// for checking if password length
		// is between 8 and 15
		if (!((password.length() >= 8) && (password.length() <= 15))) {
			throw new ValidationException(setup.getProperty("settings.message.rest.password.policy.length.error"));
		}

		// to check space
		if (password.contains(" ")) {
			throw new ValidationException(setup.getProperty("settings.message.rest.password.policy.space.error"));
		}
		if (true) {
			int count = 0;

			// check digits from 0 to 9
			for (int i = 0; i <= 9; i++) {

				// to convert int to string
				String str1 = Integer.toString(i);

				if (password.contains(str1)) {
					count = 1;
				}
			}
			if (count == 0) {
				throw new ValidationException(setup.getProperty("settings.message.rest.password.policy.digit.error"));
			}
		}

		// for special characters
		if (!(password.contains("@") || password.contains("#") || password.contains("!") || password.contains("~")
				|| password.contains("$") || password.contains("%") || password.contains("^") || password.contains("&")
				|| password.contains("*") || password.contains("(") || password.contains(")") || password.contains("-")
				|| password.contains("+") || password.contains("/") || password.contains(":") || password.contains(".")
				|| password.contains(", ") || password.contains("<") || password.contains(">") || password.contains("?")
				|| password.contains("|"))) {
			throw new ValidationException(setup.getProperty("settings.message.rest.password.policy.special.error"));
		}

		if (true) {
			int count = 0;

			// checking capital letters
			for (int i = 65; i <= 90; i++) {

				// type casting
				char c = (char) i;

				String str1 = Character.toString(c);
				if (password.contains(str1)) {
					count = 1;
				}
			}
			if (count == 0) {
				throw new ValidationException(
						setup.getProperty("settings.message.rest.password.policy.uppercase.error"));
			}
		}

		if (true) {
			int count = 0;

			// checking small letters
			for (int i = 90; i <= 122; i++) {

				// type casting
				char c = (char) i;
				String str1 = Character.toString(c);

				if (password.contains(str1)) {
					count = 1;
				}
			}
			if (count == 0) {
				throw new ValidationException(
						setup.getProperty("settings.message.rest.password.policy.lowercase.error"));
			}
		}

		// The password is valid

		return true;

	}

	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int len;

		// read bytes from the input stream and store them in buffer
		while ((len = in.read(buffer)) != -1) {
			// write bytes from the buffer into output stream
			os.write(buffer, 0, len);
		}
		return os.toByteArray();
	}

	public static Boolean isCouponExpired(final Calendar date) {
		Boolean v = date != null && Calendar.getInstance().after(date);
		return v;
	}

	public static String getRandomNumberString() {
		Random rnd = new Random();
		long drand = (long) (rnd.nextDouble() * 10000000000L);
		return String.format("%010d", drand);
	}

	private static Map<Long, ECPublicKey> parsePublicKeysJson() throws GeneralSecurityException, IOException {
		URL url = new URL("https://www.gstatic.com/admob/reward/verifier-keys.json");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = reader.readLine()) != null) {
			content.append(inputLine);
		}
		reader.close();
		connection.disconnect();
		String publicKeysJson = content.toString();
		JSONArray keys = new JSONObject(publicKeysJson).getJSONArray("keys");
		Map<Long, ECPublicKey> publicKeys = new HashMap<>();
		for (int i = 0; i < keys.length(); i++) {
			JSONObject key = keys.getJSONObject(i);
			publicKeys.put(key.getLong("keyId"), EllipticCurves.getEcPublicKey(Base64.getDecoder().decode(key.getString("base64"))));
		}
		if (publicKeys.isEmpty()) {
			throw new GeneralSecurityException("No trusted keys are available for this protocol version");
		}
		return publicKeys;
	}

	public static void verify(final byte[] dataToVerify, Long keyId, final byte[] signature)
			throws GeneralSecurityException {
		try {
			Map<Long, ECPublicKey> publicKeys = parsePublicKeysJson();
			if (publicKeys.containsKey(keyId)) {
				ECPublicKey publicKey = publicKeys.get(keyId);
				EcdsaVerifyJce verifier = new EcdsaVerifyJce(publicKey, HashType.SHA256, EcdsaEncoding.DER);
				verifier.verify(signature, dataToVerify);
			} else {
				throw new GeneralSecurityException(String.format("Cannot find verifying key with key id: %s.", keyId));
			}
		} catch (JSONException exception) {
			throw new GeneralSecurityException(exception);
		} catch (IOException exception) {
			throw new GeneralSecurityException(exception);
		}
	}

	public static String getRandomHexColorCode() {
		Random obj = new Random();
		int rand_num = obj.nextInt(0xffffff + 1);
		return String.format("#%06x", rand_num);
	}
	
	public static boolean isCurrentDateBetweenEventDates(final LocalDateTime startDate, final LocalDateTime endDate){
		   // return !(LocalDateTime.now().isBefore(startDate) || LocalDateTime.now().isAfter(endDate));
		   boolean result = (LocalDateTime.now().isAfter(startDate) && LocalDateTime.now().isBefore(endDate));
		   return !result;
		}
	
	public static boolean isAuthenticated() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication == null || AnonymousAuthenticationToken.class.
	      isAssignableFrom(authentication.getClass())) {
	        return false;
	    }
	    log.info(""+authentication.isAuthenticated());
	    return authentication.isAuthenticated();
	}
	
	public static String generateRandomPassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
            .concat(numbers)
            .concat(specialChar)
            .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        String password = pwdChars.stream()
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
        return password;
    }
}
