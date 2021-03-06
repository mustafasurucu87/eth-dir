package com.ethereum.eth.dir.service.impl;

import java.math.BigInteger;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.ethereum.eth.dir.service.PrivateKeyService;

@Service
public class PrivateKeyServiceImpl implements PrivateKeyService {

	@Override
	public String getRandomPrivateKey() {
		Random r = new Random();
		StringBuffer sb = new StringBuffer();
		while (sb.length() < 64) {
			sb.append(Integer.toHexString(r.nextInt()));
		}
		//sb.setCharAt(0, '7');

		return sb.toString().substring(0, 64);
	}

	@Override
	public String incrementPrivateKeyByIndex(String privateKey, int index) {
		StringBuilder stringBuilder = new StringBuilder(privateKey);
		String c = String.valueOf(stringBuilder.charAt(index));
		int i = Integer.parseInt(c, 16);
		i++;
		String s = Integer.toHexString(i);
		stringBuilder.setCharAt(index, s.charAt(0));
		return stringBuilder.toString().substring(0, 64);
	}

	@Override
	public String incrementPrivateKey(String privateKey, BigInteger amount) {
		BigInteger decimal = new BigInteger(privateKey, 16);
		decimal = decimal.add(amount);
		return decimal.toString(16);
	}

	@Override
	public String decrementPrivateKey(String privateKey, BigInteger amount) {
		BigInteger decimal = new BigInteger(privateKey, 16);
		decimal = decimal.subtract(amount);
		return decimal.toString(16);
	}

}
