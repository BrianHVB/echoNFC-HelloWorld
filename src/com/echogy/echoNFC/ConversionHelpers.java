package com.echogy.echoNFC;


public class ConversionHelpers {
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();


	/**
	 * Fast utility function from StackOverflow for converting a byte array to a hex string
	 * @param bytes an array of bytes
	 * @return a string of hexadecimal values
	 */
	public static String bytesToHex(byte[] bytes) {
		// https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String bytesToPrettyHex(byte[] bytes) {
		return hexPrettyPrint(bytesToHex(bytes));
	}

	public static String hexPrettyPrint(String hexString) {
		StringBuilder sb = new StringBuilder();
		char[] chars = hexString.toCharArray();
		for (int i = 0; i < hexString.length(); i++) {
			sb.append(chars[i]);
			if (i % 2 == 1 && i != hexString.length() - 1) {
				sb.append(':');
			}
		}

		return sb.toString();
	}

	public static byte xorByteArray(byte[] bytes) {
		int result = 0;
		for (byte b : bytes) {
			result = result ^ b;
		}

		return (byte)result;
	}
}
