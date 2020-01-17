package com.chavessumer.fileserver.encryption;

public class Util {

    public static byte[] compress(byte[] block) throws Exception {

        if (block.length % 2 != 0) {
            throw new Exception("Parâmetro inválido");
        }

        byte[] ret = new byte[block.length / 2];

        byte input;
        for (int x = 0; x < ret.length; x++) {
            input = block[x * 2];
            if ((input >= 'A' || input >= 'a') && (input <= 'F' || input <= 'f')) {
                ret[x] = (byte) ((input - 'A' + 10) << 4);
            } else if (input >= '0' && input <= '9') {
                ret[x] = (byte) ((input - '0') << 4);
            } else {
                throw new Exception("Caracter inválido");
            }
            input = block[x * 2 + 1];
            if ((input >= 'A' || input >= 'a') && (input <= 'F' || input <= 'f')) {
                ret[x] |= ((input - 'A' + 10) & 0x0F);
            } else if (input >= '0' && input <= '9') {
                ret[x] |= ((input - '0') & 0x0F);
            } else {
                throw new Exception("Caracter inválido");
            }
        }
        return ret;
    }


    public static String byteToString(byte[] informacao){
        StringBuilder builder = new StringBuilder();
        for (byte b : informacao) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    public static byte[] hexStringToByteArray(String info) {
        int len = info.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(info.charAt(i), 16) << 4)
                                 + Character.digit(info.charAt(i+1), 16));
        }
        return data;
    }

}
