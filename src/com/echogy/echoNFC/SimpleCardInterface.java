package com.echogy.echoNFC;

import javax.smartcardio.*;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

import static com.echogy.echoNFC.ConversionHelpers.*;

@SuppressWarnings("WeakerAccess")
public class SimpleCardInterface {

	private CardTerminal terminal;
	private Card card;

	public SimpleCardInterface() throws CardException {
		terminal = getTerminal();
	}

	public static void main(String[] args) throws CardException, UnsupportedEncodingException {
		System.out.println("I rock da NFCs\n");

		CommandAPDU command;
		ResponseAPDU response;
		String commandHexString;
		byte[] commandByteArray;

		SimpleCardInterface sci = new SimpleCardInterface();
		sci.connectToCard();

		sci.printCardDetails();
		print("");

		// get the firmware of the reader
		print("Get Firmware:");
		commandHexString = "FF00480000";
		commandByteArray = DatatypeConverter.parseHexBinary(commandHexString);
		command = new CommandAPDU(commandByteArray);
		response = sci.sendCommand(command);
		byte[] responseBytes = response.getBytes();
		print(response);
		print("byte array:\t" + Arrays.toString(responseBytes));
		print("hex string:\t" + bytesToPrettyHex(responseBytes));
		print("ascii string:\t" + new String(responseBytes, StandardCharsets.UTF_8));
		print("");

		// make the reader beep
		print("Making the reader beep:");
		commandHexString = "FF 00 40 00 04 01 00 03 03".replace(" ", "");
		commandByteArray = DatatypeConverter.parseHexBinary(commandHexString);
		command = new CommandAPDU(commandByteArray);
		response = sci.sendCommand(command);
		print(response);
		print(bytesToPrettyHex(response.getBytes()));
		print("");

		// get the UID of the card
		// the UID of a MiFare Ultralight is 7-bytes long;
		// two extra bytes are always returned that show the status of the command
		// 90 00 = success    63 00 = error      6A 81 = not supported
		print("Getting UID:");
		command = makeCommand("FF CA 00 00 00");
		response = sci.sendCommand(command);
		print(response);
		print(bytesToPrettyHex(response.getBytes()));
		print("");

		// get the ATS of the card
		print("Getting ATS:");
		command = makeCommand("FF CA 01 00 00");
		var res = sci.sendCommand(command);
		print(res);
		print(bytesToPrettyHex(res.getBytes()));
		print("");


	}

	public static void print(Object input) {
		System.out.println(input);
	}

	public void listTerminals() {
		TerminalFactory factory = TerminalFactory.getDefault();
		try {
			List<CardTerminal> terminals = factory.terminals().list();
			print(terminals);
		} catch (CardException e) {
			e.printStackTrace();
		}
	}

	public CardTerminal getTerminal() throws CardException {
		TerminalFactory factory = TerminalFactory.getDefault();
		List<CardTerminal> terminals = factory.terminals().list();
		return terminals.get(0);
	}

	public void connectToCard() throws CardException {
		// connect("<x>") where <x> is the protocol to use. 'T=0', 'T=1', ...., or '*' for any available protocol
		if (isCardPresent()) {
			card =  terminal.connect("*");
		}
		else {
			print("ERROR: Card not present");
		}

	}

	public void printCardDetails() throws CardException {
		ATR atr = card.getATR();
		byte[] atrBytes = atr.getBytes();

		// check bit is calculated by XORing all bytes from T0 (inclusive) to TCK (exclusive)
		int length = atrBytes.length;
		int start = 1;
		int stop = length - 1;
		byte[] toCheck = Arrays.copyOfRange(atrBytes, start, stop);
		byte calcTck = xorByteArray(toCheck);
		String calcTckHex = Integer.toHexString(calcTck);
		String reportedTckHex = Integer.toHexString(atrBytes[length - 1]);

		print(card);
		print("Protocol: " + card.getProtocol());
		print("ATR:\t" + Arrays.toString(atr.getBytes()));
		print("\t\t" + bytesToPrettyHex(atrBytes));
		print(String.format("check-bit:\tcalculated = %1$s\treported = %2$s", calcTckHex, reportedTckHex));
	}


	public boolean isCardPresent() throws CardException {
		return terminal.isCardPresent();
	}

	public ResponseAPDU sendCommand(CommandAPDU command) throws CardException {

		if (isCardPresent()) {
			CardChannel channel = card.getBasicChannel();
			return channel.transmit(command);
		}
		else {
			print("ERROR: Card not present");
			return new ResponseAPDU(new byte[0]);
		}

	}

	public static CommandAPDU makeCommand(String hexString) {
		String hex = hexString.replaceAll("[ :]", "");
		byte[] bytes = DatatypeConverter.parseHexBinary(hex);
		return new CommandAPDU(bytes);
	}

}
