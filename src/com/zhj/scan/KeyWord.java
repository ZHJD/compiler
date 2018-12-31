package com.zhj.scan;

import java.util.Arrays;
import java.util.HashSet;

public class KeyWord {
    private static String []key = {
            "program",
            "var",
            "integer",
            "float",
            "procedure",
            "begin",
            "end",
            "read",
            "write",
            "if",
            "then",
            "else",
            "fi",
            "while",
            "do",
            "endwh",
            "or",
            "and"};
    //使用哈希表保存关键字，偏于判断
    public static HashSet<String> keyWord = new HashSet<>(Arrays.asList(key));
}
