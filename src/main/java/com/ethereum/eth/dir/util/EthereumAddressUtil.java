package com.ethereum.eth.dir.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class EthereumAddressUtil {

	public static List<String> getAddressList() throws IOException {
		return Files.readAllLines(Paths.get("D:\\Projects\\eth-dir\\address\\address.txt"));
	}

}
