package com.zhj.scan;

import static com.zhj.scan.Data.ERR;

public class Token {
    private int line;//行号
    private int col;//列号
    private int digit;//如果单词是整数，保存整数的值
    private double decimal;//如果单词是小数报错数值
    private String name = "";//如果单词是变量，保存变量值
    private int type;//保存单词类型
    private String msg = "";//保存出错信息
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getDigit() {
        return digit;
    }

    public void setDigit(int digit) {
        this.digit = digit;
    }

    public double getDecimal() {
        return decimal;
    }

    public void setDecimal(double decimal) {
        this.decimal = decimal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if(type==ERR){
            return line + " " + col + " " + type+ " " + name + " " + msg;
        }
        else{
            return line + " " + col + " " + type+ " " + name;
        }
    }
}
