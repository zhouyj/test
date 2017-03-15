package com.feedss.contact.qcloud;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.Arrays;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.feedss.base.util.conf.ConfigureUtil;
import com.tls.base64_url.base64_url;
import com.tls.tls_sigature.tls_sigature.CheckTLSSignatureResult;
import com.tls.tls_sigature.tls_sigature.GenTLSSignatureResult;

@Component
public class GenerateTLSSignatureUtil {

	private static final Logger logger = LoggerFactory.getLogger(GenerateTLSSignatureUtil.class);

	static long ExpireTime = 3600 * 24 * 180; // 180天

	private String privStr;

	private String pubStr;

	private long sdkAppId;
	
	@Autowired
	private ConfigureUtil configureUtil;
	
	public GenerateTLSSignatureUtil(){
		
	}
	
	@PostConstruct
	private void init() {
		logger.info("init GenerateTLSSignatureUtil...");
		String sdkAppIdStr = configureUtil.getConfig(QCloudMessageUtil.QCLOUD_SDKAPPId_KEY);
		if (!StringUtils.isEmpty(sdkAppIdStr)) {
			try {
				sdkAppId = Long.parseLong(sdkAppIdStr);
			} catch (NumberFormatException e) {
				logger.error("parse sdkappid error", e);
			}
			if (sdkAppId != 0) {
				privStr = configureUtil.getConfig(QCloudMessageUtil.QCLOUD_PRIVSTR_KEY);
				pubStr = configureUtil.getConfig(QCloudMessageUtil.QCLOUD_PUBSTR_KEY);
			}
			if(sdkAppId==0 || StringUtils.isEmpty(privStr) || StringUtils.isEmpty(pubStr)){
				logger.error("GenerateTLSSignatureUtil init failure, have not get valid sdkAppId = " + sdkAppId + ", pristr = " + privStr + ", pubStr = " + pubStr);
				return;
			}
		}
	}
	
	public String getUserSig(String userId) {
		String userSig = "";
		try {
			GenTLSSignatureResult result = GenTLSSignatureEx(sdkAppId, userId, privStr);
			if (0 == result.urlSig.length()) {
				logger.debug("GenTLSSignatureEx failed: " + result.errMessage);
				return userSig;
			}
			logger.debug("generate sig: " + result.urlSig);
			userSig = result.urlSig;

		} catch (Exception e) {
			logger.error("getUserSig error", e);
		}
		return userSig;
	}

	/**
	 * 是否需要刷新，在签名过期十天内开始做
	 * @param userId
	 * @param sig
	 * @return
	 */
	public boolean needRefreshUserSig(String userId, String sig) {
		try {
			CheckTLSSignatureResult checkResult = CheckTLSSignatureEx(sig, sdkAppId, userId, pubStr);
			if (checkResult.verifyResult == false) {
				System.out.println("CheckTLSSignature failed: " + checkResult.errMessage);
				return true;
			}else{
				if (System.currentTimeMillis() / 1000 + 10*3600*24 - checkResult.initTime > checkResult.expireTime){
					return true;
				}else {
					return false;
				}
			}
		} catch (Exception e) {
			logger.error("checkUserSig error", e);
		}
		return true;
	}

	/**
	 * @brief 生成 tls 票据，精简参数列表，有效期默认为 180 天
	 * @param skdAppid
	 *            应用的 sdkappid
	 * @param identifier
	 *            用户 id
	 * @param privStr
	 *            私钥文件内容
	 * @return
	 * @throws IOException
	 */
	public static GenTLSSignatureResult GenTLSSignatureEx(long skdAppid, String identifier, String privStr)
			throws IOException {
		return GenTLSSignatureEx(skdAppid, identifier, privStr, ExpireTime);
	}

	/**
	 * @brief 生成 tls 票据，精简参数列表
	 * @param skdAppid
	 *            应用的 sdkappid
	 * @param identifier
	 *            用户 id
	 * @param privStr
	 *            私钥文件内容
	 * @param expire
	 *            有效期，以秒为单位，推荐时长一个月
	 * @return
	 * @throws IOException
	 */
	public static GenTLSSignatureResult GenTLSSignatureEx(long skdAppid, String identifier, String privStr, long expire)
			throws IOException {

		GenTLSSignatureResult result = new GenTLSSignatureResult();

		Security.addProvider(new BouncyCastleProvider());
		Reader reader = new CharArrayReader(privStr.toCharArray());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
		PEMParser parser = new PEMParser(reader);
		Object obj = parser.readObject();
		parser.close();
		PrivateKey privKeyStruct = converter.getPrivateKey((PrivateKeyInfo) obj);

		String jsonString = "{" + "\"TLS.account_type\":\"" + 0 + "\"," + "\"TLS.identifier\":\"" + identifier + "\","
				+ "\"TLS.appid_at_3rd\":\"" + 0 + "\"," + "\"TLS.sdk_appid\":\"" + skdAppid + "\","
				+ "\"TLS.expire_after\":\"" + expire + "\"," + "\"TLS.version\": \"201512300000\"" + "}";

		String time = String.valueOf(System.currentTimeMillis() / 1000);
		String SerialString = "TLS.appid_at_3rd:" + 0 + "\n" + "TLS.account_type:" + 0 + "\n" + "TLS.identifier:"
				+ identifier + "\n" + "TLS.sdk_appid:" + skdAppid + "\n" + "TLS.time:" + time + "\n"
				+ "TLS.expire_after:" + expire + "\n";

		try {
			// Create Signature by SerialString
			Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
			signature.initSign(privKeyStruct);
			signature.update(SerialString.getBytes(Charset.forName("UTF-8")));
			byte[] signatureBytes = signature.sign();

			String sigTLS = Base64.encodeBase64String(signatureBytes);

			// Add TlsSig to jsonString
			JSONObject jsonObject = new JSONObject(jsonString);
			jsonObject.put("TLS.sig", (Object) sigTLS);
			jsonObject.put("TLS.time", (Object) time);
			jsonString = jsonObject.toString();

			// compression
			Deflater compresser = new Deflater();
			compresser.setInput(jsonString.getBytes(Charset.forName("UTF-8")));

			compresser.finish();
			byte[] compressBytes = new byte[512];
			int compressBytesLength = compresser.deflate(compressBytes);
			compresser.end();
			String userSig = new String(
					base64_url.base64EncodeUrl(Arrays.copyOfRange(compressBytes, 0, compressBytesLength)));

			result.urlSig = userSig;
		} catch (Exception e) {
			e.printStackTrace();
			result.errMessage = "generate usersig failed";
		}

		return result;
	}

	public static CheckTLSSignatureResult CheckTLSSignatureEx(String urlSig, long sdkAppid, String identifier,
			String publicKey) throws DataFormatException {

		CheckTLSSignatureResult result = new CheckTLSSignatureResult();
		Security.addProvider(new BouncyCastleProvider());

		// DeBaseUrl64 urlSig to json
		Base64 decoder = new Base64();

		byte[] compressBytes = base64_url.base64DecodeUrl(urlSig.getBytes(Charset.forName("UTF-8")));

		// Decompression
		Inflater decompression = new Inflater();
		decompression.setInput(compressBytes, 0, compressBytes.length);
		byte[] decompressBytes = new byte[1024];
		int decompressLength = decompression.inflate(decompressBytes);
		decompression.end();

		String jsonString = new String(Arrays.copyOfRange(decompressBytes, 0, decompressLength));

		// Get TLS.Sig from json
		JSONObject jsonObject = new JSONObject(jsonString);
		String sigTLS = jsonObject.getString("TLS.sig");

		// debase64 TLS.Sig to get serailString
		byte[] signatureBytes = decoder.decode(sigTLS.getBytes(Charset.forName("UTF-8")));

		try {
			String strSdkAppid = jsonObject.getString("TLS.sdk_appid");
			String sigTime = jsonObject.getString("TLS.time");
			String sigExpire = jsonObject.getString("TLS.expire_after");

			if (Integer.parseInt(strSdkAppid) != sdkAppid) {
				result.errMessage = new String(
						"sdkappid " + strSdkAppid + " in tls sig not equal sdkappid " + sdkAppid + " in request");
				return result;
			}

			if (System.currentTimeMillis() / 1000 - Long.parseLong(sigTime) > Long.parseLong(sigExpire)) {
				result.errMessage = new String("TLS sig is out of date");
				return result;
			}

			// Get Serial String from json
			String SerialString = "TLS.appid_at_3rd:" + 0 + "\n" + "TLS.account_type:" + 0 + "\n" + "TLS.identifier:"
					+ identifier + "\n" + "TLS.sdk_appid:" + sdkAppid + "\n" + "TLS.time:" + sigTime + "\n"
					+ "TLS.expire_after:" + sigExpire + "\n";

			Reader reader = new CharArrayReader(publicKey.toCharArray());
			PEMParser parser = new PEMParser(reader);
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
			Object obj = parser.readObject();
			parser.close();
			PublicKey pubKeyStruct = converter.getPublicKey((SubjectPublicKeyInfo) obj);

			Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
			signature.initVerify(pubKeyStruct);
			signature.update(SerialString.getBytes(Charset.forName("UTF-8")));
			boolean bool = signature.verify(signatureBytes);
			result.expireTime = Integer.parseInt(sigExpire);
			result.initTime = Integer.parseInt(sigTime);
			result.verifyResult = bool;
		} catch (Exception e) {
			e.printStackTrace();
			result.errMessage = "Failed in checking sig";
		}

		return result;
	}
}
