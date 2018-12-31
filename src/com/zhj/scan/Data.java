package com.zhj.scan;

public class Data {
    public static int EOF = -1;

    /*单词的种别码定义部分*/
    //关键字
    public static int PROGRAM = 1;
    public static int VAR = 2;
    public static int INTEGER = 3;
    public static int FLOAT = 4;
    public static int PROCEDURE = 5;
    public static int BEGIN = 6;
    public static int END = 7;
    public static int READ = 8;
    public static int WRITE = 9;
    public static int IF = 10;
    public static int THEN = 11;
    public static int ELSE = 12;
    public static int FI = 13;
    public static int WHILE = 14;
    public static int DO = 15;
    public static int ENDWH = 16;
    public static int AND = 17;
    public static int OR = 18;

    //标识符
    public static int IDENTIFIER = 19;
    //无符号整数
    public static int DIGIT = 20;
    //小数
    public static int DECIMAL = 21;

    //运算符
    public static int PLUS = 22; //'+'
    public static int MINUS =23; //'-'
    public static int MUL = 24;  //'*'
    public static int DIV = 25;  //'/'
    public static int ASSIGN = 26;   //'='
    public static int LT = 27;   //'<'
    public static int GT = 28;   //'>'
    public static int LE = 29;  //‘《=’
    public static int GE = 30;  //'>='
    public static int NQ = 31;  //'<>'
    public static int EQ = 32;  //'=='

    //界符
    public static int LP = 33;
    public static int RP = 34;
    public static int CA = 35;//逗号
    public static int SN = 36;//分号

    //标识符长度限制
    public static int LIMIT = 37;

    //输入结束符
    public static char EOI = '\0';

    //出错
    public static int ERR = 38;

}
