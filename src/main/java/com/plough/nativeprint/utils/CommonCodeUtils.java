package com.plough.nativeprint.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommonCodeUtils {
    public static final int ENCODE_LEN = 4;
    public static final int HEX = 16;
    public static final double COEFFICIENT = 1.5D;
    public static final int ASC_CODE_A = 65;
    public static final int ASC_CODE_F = 70;
    public static final int ASC_CODE_Z = 90;
    public static final int ASC_CODEA = 97;
    public static final int ASC_CODEF = 102;
    public static final int ASC_CODEZ = 122;
    public static final int ASC_CODE_0 = 48;
    public static final int ASC_CODE_9 = 57;
    public static final int ASC_CODE_DEL = 127;
    public static final int ASC_CODE_LEFT_BRACKET = 91;
    public static final int ASC_CODE_RIGHT_BRACKET = 93;
    public static final int HEX_ARR_LENGTH = 128;
    public static final int EIGHTTIMES2 = 256;
    private static final String[] HEXDIGITS = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    private static Pattern bracketPattern = Pattern.compile("\\[[^\\]]*\\]");
    private static Pattern textPattern = Pattern.compile("[0-9a-f]{2,4}", 2);
    private static Pattern blankPattern = Pattern.compile("\\s+");
    private static int JUDGE_HELPER_LEN = 100;
    private static final boolean[] JUDGE_HELPER;
    private static final boolean[] HEX_ARR;
    private static final char[] APPEND_HELPER;
    private static final int COPYRIGHT_HELPER_LEN = 9000;
    private static final boolean[] COPYRIGHT_HELPER;
    private static final int[] PASSWORD_MASK_ARRAY;

    private CommonCodeUtils() {
    }

    public static String javascriptEncode(String var0) {
        CommonCodeUtils.StringBuilderHelper var1 = new CommonCodeUtils.StringBuilderHelper(var0);
        if (!StringUtils.isNotEmpty(var0)) {
            return "";
        } else {
            char var3 = 0;
            int var5 = var0.length();

            for(int var4 = 0; var4 < var5; ++var4) {
                char var2 = var3;
                var3 = var0.charAt(var4);
                if (var3 < JUDGE_HELPER_LEN && var3 > -1) {
                    if (JUDGE_HELPER[var3]) {
                        var1.append('\\', var4);
                        var1.append(APPEND_HELPER[var3], var4);
                        var1.move();
                        continue;
                    }

                    if (var3 == '/') {
                        if (var2 == '<') {
                            var1.append('\\', var4);
                        }

                        var1.append(var3, var4);
                        var1.move();
                        continue;
                    }
                }

                if (isCopyRightSymbol(var3)) {
                    String var6 = "000" + Integer.toHexString(var3);
                    var1.append('\\', var4);
                    var1.append('u', var4).append(var6.substring(var6.length() - 4), var4);
                    var1.move();
                }
            }

            return var1.toString();
        }
    }

    public static final boolean isCopyRightSymbol(char var0) {
        return var0 < 9000 && var0 > -1 && COPYRIGHT_HELPER[var0] || var0 < 0;
    }

    public static String javascriptDecode(String var0) {
        return encodeString(var0, new String[][]{{"\\", "'", "\""}, {"\\\\", "\\'", "\\\""}});
    }

    public static String encodeString(String var0, String[][] var1) {
        if (var0 == null) {
            return "";
        } else {
            StringBuilder var2 = new StringBuilder();

            label28:
            for(int var3 = 0; var3 < var0.length(); ++var3) {
                char var4 = var0.charAt(var3);

                for(int var5 = 0; var5 < var1[1].length; ++var5) {
                    if (var4 == var1[1][var5].charAt(0)) {
                        var2.append(var1[0][var5]);
                        continue label28;
                    }
                }

                var2.append(var4);
            }

            return var2.toString();
        }
    }

    public static String encodeURIComponent(String var0) {
        try {
            return URLEncoder.encode(var0, "UTF-8");
        } catch (UnsupportedEncodingException var2) {
            return encodeString(var0, new String[][]{{"%20", "%23", "%24", "%26", "%2B", "%2C", "%2F", "%3A", "%3B", "%3D", "%3F", "%40", "%25"}, {" ", "#", "$", "&", "+", ",", "/", ":", ";", "=", "?", "@", "%"}});
        }
    }

    public static String attributeHtmlEncode(CharSequence var0) {
        if (var0 == null) {
            return "";
        } else {
            int var1 = var0.length();
            StringBuffer var2 = new StringBuffer((int)((double)var1 * 1.5D));

            for(int var3 = 0; var3 < var1; ++var3) {
                char var4 = var0.charAt(var3);
                if (isLetterOrNumber(var4)) {
                    var2.append(var4);
                } else {
                    var2.append("&#").append(var4).append(';');
                }
            }

            return var2.toString();
        }
    }

    public static String attributeHtmlDecode(CharSequence var0) {
        if (var0 == null) {
            return "";
        } else {
            int var1 = var0.length();
            StringBuilder var2 = new StringBuilder(var1);

            for(int var3 = 0; var3 < var1; ++var3) {
                char var4 = var0.charAt(var3);
                boolean var5 = false;
                if (var4 == '&' && var3 + 1 < var1 && var0.charAt(var3 + 1) == '#') {
                    int var6 = var0.toString().indexOf(";", var3 + 1);
                    if (var6 != -1 && var6 < var3 + 3 + 3) {
                        String var7 = var0.subSequence(var3 + 2, var6).toString();
                        char var8 = (char)Integer.parseInt(var7);
                        var2.append(var8);
                        var3 = var6;
                        var5 = true;
                    }
                }

                if (!var5) {
                    var2.append(var4);
                }
            }

            return var2.toString();
        }
    }

    private static boolean isLetterOrNumber(char var0) {
        return var0 >= 'a' && var0 <= 'z' || var0 >= 'A' && var0 <= 'Z' || var0 >= '0' && var0 <= '9';
    }

    public static String htmlEncode(CharSequence var0) {
        if (var0 == null) {
            return "";
        } else {
            int var1 = var0.length();
            StringBuilder var2 = new StringBuilder(var1);

            for(int var3 = 0; var3 < var1; ++var3) {
                char var4 = var0.charAt(var3);
                var3 = dealWithChar(var4, var2, var1, var3, var0);
            }

            return replaceBlankToHtmlBlank(var2.toString());
        }
    }

    private static int dealWithChar(char var0, StringBuilder var1, int var2, int var3, CharSequence var4) {
        switch(var0) {
            case '\n':
                var1.append("<br>");
                break;
            case '\r':
                if (var3 + 1 < var2 && var4.charAt(var3 + 1) == '\n') {
                    ++var3;
                }

                var1.append("<br>");
                break;
            case '"':
                var1.append("&quot;");
                break;
            case '&':
                var1.append("&amp;");
                break;
            case '<':
                var1.append("&lt;");
                break;
            case '>':
                var1.append("&gt;");
                break;
            case '\\':
                if (var3 + 1 < var2 && var4.charAt(var3 + 1) == 'n') {
                    ++var3;
                    var1.append("<br>");
                } else {
                    if (var3 + 1 < var2 && var4.charAt(var3 + 1) == '\\') {
                        ++var3;
                    }

                    var1.append(var0);
                }
                break;
            default:
                var1.append(var0);
        }

        return var3;
    }

    private static String replaceBlankToHtmlBlank(String var0) {
        Matcher var1 = blankPattern.matcher(var0);
        int var2 = 0;
        boolean var3 = false;
        StringBuffer var4 = new StringBuffer(var0.length());

        while(true) {
            int var6;
            do {
                if (!var1.find()) {
                    if (var2 == 0) {
                        return var0;
                    }

                    var4.append(var0.substring(var2));
                    return var4.toString();
                }

                int var8 = var1.start();
                var4.append(var0.substring(var2, var8));
                var2 = var1.end();
                String var5 = var1.group();
                var4.append(" ");
                var6 = var5.length();
            } while(var6 <= 1);

            for(int var7 = var6; var7 > 1; --var7) {
                var4.append("&nbsp;");
            }

            var4.append(" ");
        }
    }

    public static String cjkEncode(String var0) {
        if (var0 == null) {
            return "";
        } else {
            StringBuilder var1 = new StringBuilder();
            int var2 = 0;

            for(int var3 = var0.length(); var2 < var3; ++var2) {
                char var4 = var0.charAt(var2);
                if (needToEncode(var4)) {
                    var1.append('[');
                    var1.append(Integer.toString(var4, 16));
                    var1.append(']');
                } else {
                    var1.append(var4);
                }
            }

            return var1.toString();
        }
    }

    private static boolean needToEncode(char var0) {
        return var0 > 127 || var0 == '[' || var0 == ']';
    }

    public static String cjkDecode(String var0) throws Exception {
        if (var0 == null) {
            return "";
        } else if (!isCJKEncoded(var0)) {
            return var0;
        } else {
            StringBuilder var1 = new StringBuilder();

            for(int var2 = 0; var2 < var0.length(); ++var2) {
                char var3 = var0.charAt(var2);
                if (var3 == '[') {
                    int var4 = var0.indexOf(93, var2 + 1);
                    if (var4 > var2 + 1) {
                        String var5 = var0.substring(var2 + 1, var4);
                        if (var5.length() > 0) {
                            var3 = (char)Integer.parseInt(var5, 16);
                        }

                        var2 = var4;
                    }
                }

                var1.append(var3);
            }

            return var1.toString();
        }
    }

    public static boolean isCJKEncoded(String var0) {
        if (var0 == null) {
            return false;
        } else {
            int var1 = -1;
            boolean var2 = true;
            boolean var3 = false;

            for(int var4 = 0; var4 < var0.length(); ++var4) {
                char var5 = var0.charAt(var4);
                if (var5 == '[') {
                    var1 = var4 + 1;
                } else if (var1 != -1 && var5 == ']') {
                    var3 = true;
                    int var6 = var4 - var1 - 1;
                    if (var6 == 0 || var6 >>> 2 != 0) {
                        return false;
                    }

                    if (!isHex(var0, var1, var4)) {
                        return false;
                    }

                    var1 = -1;
                    var2 = true;
                }
            }

            return var3;
        }
    }

    private static boolean isHex(String var0, int var1, int var2) {
        for(int var3 = var1; var3 < var2; ++var3) {
            if (!isHex(var0.charAt(var3))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isHex(char var0) {
        return var0 >>> 7 == 0 && HEX_ARR[var0];
    }

    public static String passwordEncode(String var0) {
        StringBuilder var1 = new StringBuilder();
        var1.append("___");
        if (var0 == null) {
            return var1.toString();
        } else {
            int var2 = 0;

            for(int var3 = 0; var3 < var0.length(); ++var3) {
                if (var2 == PASSWORD_MASK_ARRAY.length) {
                    var2 = 0;
                }

                int var4 = var0.charAt(var3) ^ PASSWORD_MASK_ARRAY[var2];
                String var5 = Integer.toHexString(var4);
                int var6 = var5.length();

                for(int var7 = 0; var7 < 4 - var6; ++var7) {
                    var5 = "0" + var5;
                }

                var1.append(var5);
                ++var2;
            }

            return var1.toString();
        }
    }

    public static String passwordDecode(String var0) {
        if (var0 != null && var0.startsWith("___")) {
            var0 = var0.substring(3);
            StringBuilder var1 = new StringBuilder();
            int var2 = 0;

            for(int var3 = 0; var3 <= var0.length() - 4; var3 += 4) {
                if (var2 == PASSWORD_MASK_ARRAY.length) {
                    var2 = 0;
                }

                String var4 = var0.substring(var3, var3 + 4);
                int var5 = Integer.parseInt(var4, 16) ^ PASSWORD_MASK_ARRAY[var2];
                var1.append((char)var5);
                ++var2;
            }

            var0 = var1.toString();
        }

        return var0;
    }

    public static String decodeText(String var0) {
        if (var0 == null) {
            return null;
        } else {
            try {
                return cjkDecode(var0);
            } catch (Exception var2) {
                return var0;
            }
        }
    }

    public static String md5Encode(String var0, Object var1, String var2) {
        try {
            MessageDigest var3 = MessageDigest.getInstance(var2);
            String var4 = var0 == null ? "" : var0;
            if (var1 != null && StringUtils.isNotEmpty(var1.toString())) {
                var4 = var4 + "{" + var1.toString() + "}";
            }

            byte[] var5 = var3.digest(var4.getBytes("UTF-8"));
            StringBuilder var6 = new StringBuilder();

            for(int var7 = 0; var7 < var5.length; ++var7) {
                var6.append(byteToHexString(var5[var7]));
            }

            return var6.toString();
        } catch (Exception var8) {
            return null;
        }
    }

    public static String byteToHexString(byte var0) {
        int var1 = var0;
        if (var0 < 0) {
            var1 = 256 + var0;
        }

        int var2 = var1 / 16;
        int var3 = var1 % 16;
        return HEXDIGITS[var2] + HEXDIGITS[var3];
    }

    public static String getNewCharSetString(String var0, String var1, String var2) {
        if (StringUtils.isEmpty(var2)) {
            return var2;
        } else {
            boolean var3 = StringUtils.isBlank(var0);
            boolean var4 = StringUtils.isBlank(var1);
            if (var3 && var4) {
                return var2;
            } else {
                try {
                    byte[] var5 = var3 ? var2.getBytes() : var2.getBytes(var0);
                    return var4 ? new String(var5) : new String(var5, var1);
                } catch (UnsupportedEncodingException var6) {
                    var6.printStackTrace();
                    return var2;
                }
            }
        }
    }

    static {
        JUDGE_HELPER = new boolean[JUDGE_HELPER_LEN];
        HEX_ARR = new boolean[128];
        APPEND_HELPER = new char[JUDGE_HELPER_LEN];
        JUDGE_HELPER[92] = true;
        APPEND_HELPER[92] = '\\';
        JUDGE_HELPER[34] = true;
        APPEND_HELPER[34] = '"';
        JUDGE_HELPER[9] = true;
        APPEND_HELPER[9] = 't';
        JUDGE_HELPER[10] = true;
        APPEND_HELPER[10] = 'n';
        JUDGE_HELPER[13] = true;
        APPEND_HELPER[13] = 'r';
        JUDGE_HELPER[12] = true;
        APPEND_HELPER[12] = 'f';
        JUDGE_HELPER[8] = true;
        APPEND_HELPER[8] = 'b';

        int var0;
        for(var0 = 48; var0 < 58; ++var0) {
            HEX_ARR[var0] = true;
        }

        for(var0 = 65; var0 < 71; ++var0) {
            HEX_ARR[var0] = true;
        }

        for(var0 = 97; var0 < 103; ++var0) {
            HEX_ARR[var0] = true;
        }

        COPYRIGHT_HELPER = new boolean[9000];

        for(var0 = 0; var0 < 32; ++var0) {
            COPYRIGHT_HELPER[var0] = true;
        }

        for(var0 = 128; var0 < 160; ++var0) {
            COPYRIGHT_HELPER[var0] = true;
        }

        for(var0 = 8192; var0 < 8448; ++var0) {
            COPYRIGHT_HELPER[var0] = true;
        }

        COPYRIGHT_HELPER[169] = true;
        COPYRIGHT_HELPER[174] = true;
        PASSWORD_MASK_ARRAY = new int[]{19, 78, 10, 15, 100, 213, 43, 23};
    }

    private static final class StringBuilderHelper {
        private StringBuilder sb;
        private String baseString;
        private int start;

        private StringBuilderHelper(String var1) {
            this.start = 0;
            this.baseString = var1;
        }

        private void initSB(int var1) {
            if (this.sb == null) {
                this.sb = new StringBuilder(this.baseString.length());
            }

            if (this.start != var1) {
                this.sb.append(this.baseString, this.start, var1);
                this.start = var1;
            }

        }

        private CommonCodeUtils.StringBuilderHelper append(char var1, int var2) {
            this.initSB(var2);
            this.sb.append(var1);
            return this;
        }

        private void move() {
            ++this.start;
        }

        private CommonCodeUtils.StringBuilderHelper append(String var1, int var2) {
            this.initSB(var2);
            this.sb.append(var1);
            return this;
        }

        public String toString() {
            if (this.sb == null) {
                return this.baseString;
            } else {
                this.initSB(this.baseString.length());
                return this.sb.toString();
            }
        }
    }
}
