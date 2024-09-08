package ua.glek.notes.Service;

import Utils.CryptoUtils;

public class EncryptedDataService {
    public static final String secretKey = "mySuperSecretKey";

    public String saveEncryptedData(String data)throws Exception {
        String encryptedData = CryptoUtils.encrypt(data,secretKey);
        return encryptedData;
    }


    public String getDecryptedData(String encryptedData)throws Exception {
        String decryptedData = CryptoUtils.decrypt(encryptedData,secretKey);
        return decryptedData;
    }

}
