package com.ethereum.eth.dir.service.impl;

import com.ethereum.eth.dir.model.EthereumWallet;
import com.ethereum.eth.dir.model.request.TransferEtherRequest;
import com.ethereum.eth.dir.service.EthereumClientService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

@Service
public class EthereumClientServiceImpl implements EthereumClientService {

	static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
	static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

	private Web3j web3j;

	@PostConstruct
	public void init() {
		web3j = Web3j.build(new HttpService("https://mainnet.infura.io/<your-token>"));
	}
	@Override
	public EthereumWallet getWalletByAddress(String privateKey, String address) {

		if (StringUtils.isEmpty(address)) {
			return null;
		}

		EthGetBalance ethGetBalance = null;
		try {
			ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Error on query PK : " + privateKey + " Address : " + address);
			return new EthereumWallet(address, null, BigDecimal.ZERO);
		}

		return new EthereumWallet(address, null, convertToEther(ethGetBalance));
	}

	@Override
	public EthereumWallet getWalletByPrivateKey(String privateKey) {
		String address = getAddressByPrivateKey(privateKey);
		if (address == null) {
			return null;
		}
		EthereumWallet wallet = getWalletByAddress(privateKey, address);
		wallet.setPrivateKey(privateKey);
		return wallet;
	}

	public String getAddressByPrivateKey(String privateKey) {
		String address = null;
		if (WalletUtils.isValidPrivateKey(privateKey)) {
			BigInteger key = new BigInteger(privateKey, 16);
			ECKeyPair ecKeyPair = ECKeyPair.create(key);
			Credentials credentials = Credentials.create(ecKeyPair);
			address = credentials.getAddress();
		} else {
			System.out.println("Not a valid PK");
			return null;
		}
		return address;
	}

	@Override
	public void transferEther(TransferEtherRequest request) {
		String fromAddress = null;
		Credentials credentials;
		if (WalletUtils.isValidPrivateKey(request.getPrivateKey())) {
			BigInteger key = new BigInteger(request.getPrivateKey(), 16);
			ECKeyPair ecKeyPair = ECKeyPair.create(key);
			credentials = Credentials.create(ecKeyPair);
			fromAddress = credentials.getAddress();
		} else {
			System.out.println("Not a valid PK");
			return;
		}

		try {
			EthGetTransactionCount ethGetTransactionCount = web3j
					.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST).sendAsync().get();

			BigInteger nonce = ethGetTransactionCount.getTransactionCount();
			System.out.println(nonce);

			RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, GAS_PRICE, GAS_LIMIT,
					request.getToAddress(), request.getAmount());

			byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
			String hexValue = Numeric.toHexString(signedMessage);

			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
			String transactionHash = ethSendTransaction.getTransactionHash();
			System.out.println("Transaction Hash : " + transactionHash);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private BigDecimal convertToEther(EthGetBalance ethGetBalance) {
		return Convert.fromWei(ethGetBalance.getBalance().toString(), Unit.ETHER);
	}
}
