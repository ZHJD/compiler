package com.zhj.scan;



import static com.zhj.scan.Data.*;


public class Scanner extends Lookahead {
    private int line;//表示行位置
    private int col;//表示列位置
    private int pCol;//保存上一个字符的位置
    private char ch;//当前分析的字符
    private int bp;//字符指针
    private char [] buffer;
    private int bufLen;

    public Scanner(){
        String temp = FileUtils.readFile("aaa.txt");
        bufLen = temp.length()+1;
        buffer = new char[bufLen];
        for(int i=0;i<temp.length();i++){
            buffer[i]=temp.charAt(i);
        }
        buffer[bufLen-1]=EOI;
  //      System.out.println(buffer);
        bp=-1;
        col=0;
        line=1;
       // getToken();
    }

    private void scanChar(){
        bp++;
        ch = buffer[bp];
        switch (ch){
            case '\r':
                pCol=col;
                col=0;
                line++;
                break;
            case '\n':
                if(bp==0||buffer[bp-1]!='\r'){
                    pCol=col;
                    col=0;
                    line++;
                }
                break;
            case '\t':
                pCol=col;
                col = col + 4;
                break;
            default:
                pCol=col;
                col++;
                break;
        }
    }

    public void clearLookahead(){
        lookahead.setName("");
        lookahead.setDigit(0);
        lookahead.setMsg("");
        lookahead.setDecimal(0);
    }

    public void getToken(){
        //System.out.println(1);
        clearLookahead();
        scanChar();
        while (ch=='\r'||ch=='\t'||ch=='\n'||ch==' '){
            scanChar();
        }
        //如果首字符是字母，则可能是标识符或者关键字
        int preCol=col;//记录单词首字母所在位置,
        int preLine=line;//如果多都入的字符是换行符，那么位置会发生变化，与实际不符合
        if(isLetter(ch)||ch=='_'){
            String temp = "";
            temp += ch;
            scanChar();
            while (isLetter(ch)||ch=='_'||isDigit(ch)){
                temp+=ch;
                scanChar();
            }
            //保留字不区分大小写
            if(isKeyWord(temp.toLowerCase())){
                lookahead.setCol(preCol);
                lookahead.setLine(preLine);
                lookahead.setName(temp);
                lookahead.setType(returnType(temp.toLowerCase()));
            }
            else {//如果是标识符
                if(temp.length()>20){
                    lookahead.setType(ERR);
                    lookahead.setName(temp);
                    lookahead.setLine(preLine);
                    lookahead.setCol(preCol);
                    lookahead.setMsg("'"+temp+"'超过20个字符");
                }else{
                    lookahead.setCol(preCol);
                    lookahead.setLine(preLine);
                    lookahead.setName(temp);
                    lookahead.setType(IDENTIFIER);
                }
            }
            rollback();
        }
        /*识别无符号整数和无符号小数*/
        else if(isDigit(ch)){
           String temp = "";
           temp += ch;
           scanChar();
           int length=0;//保存整数部分的位数
           while (isDigit(ch)){
               temp+=ch;
               scanChar();
           }
           length = temp.length();
           //如果当前字符是小数点
            if(ch == '.'){
               temp += ch;
               scanChar();
               int i=0;//计算小数点后面位数
               while (isDigit(ch)){
                   temp+=ch;
                   i++;
                   scanChar();
               }
               if(i==0){
                   temp += '0';//小数点后面补上一位数
               }
               lookahead.setLine(preLine);
               lookahead.setCol(preCol);
               lookahead.setType(DECIMAL);//类型设为小数
               lookahead.setDecimal(Double.parseDouble(temp));
               lookahead.setName(temp);
               if(length!=1&&temp.charAt(0)=='0'){
                   lookahead.setType(ERR);
                   lookahead.setMsg("'"+temp+"'首位数字不能为0");
               }
            }
           // else if(!isOperator(ch))
            else {
                //如果第一位数是零，则出现错误
                lookahead.setLine(preLine);
                lookahead.setCol(preCol);
                if (temp.charAt(0)=='0'&&temp.length()!=1){
                    lookahead.setType(ERR);
                    lookahead.setDigit(Integer.parseInt(temp));
                    lookahead.setName(temp);
                    lookahead.setMsg("'"+temp+"'首位数字不能为0");
                }
                else {
                    lookahead.setType(INTEGER);
                    lookahead.setDigit(Integer.parseInt(temp));
                    lookahead.setName(temp);
                }
            }
            rollback();
        }
        /*识别运算符，除号和注释一起处理*/
        else if(isOperator(ch)){
            switch (ch){
                case '+':
                case '-':
                case '*':
                    lookahead.setLine(preLine);
                    lookahead.setCol(preCol);
                    break;
                case '=':
                    lookahead.setLine(preLine);
                    lookahead.setCol(preCol);
                    scanChar();//获取下一个符号，判断是否是等号
                    if(ch == '='){
                        lookahead.setType(EQ);//类型是逻辑判断语句
                        lookahead.setName("==");
                    }
                    else{
                        lookahead.setType(ASSIGN);
                        lookahead.setName("=");
                        rollback();//回退多读入的字符
                    }
                    break;
                case '<':
                    lookahead.setLine(preLine);
                    lookahead.setCol(preCol);
                    scanChar();//获取下一个符号，判断是否是等号
                    if(ch == '>'){
                        lookahead.setType(NQ);//类型是逻辑判断语句
                        lookahead.setName("!=");
                    }
                    else if(ch == '='){
                        lookahead.setType(LE);//类型是逻辑判断语句
                        lookahead.setName("<=");
                    }
                    else{
                        lookahead.setType(LT);
                        lookahead.setName("<");
                        rollback();//回退多读入的字符
                    }
                    break;
                case '>':
                    lookahead.setLine(preLine);
                    lookahead.setCol(preCol);
                    scanChar();//获取下一个符号，判断是否是等号
                    if(ch == '='){
                        lookahead.setType(GE);//类型是逻辑判断语句
                        lookahead.setName(">+");
                    }
                    else{
                        lookahead.setType(GT);
                        lookahead.setName(">");
                        rollback();//回退多读入的字符
                    }
                    break;
            }
        }
        /*处理注释*/
        else if(ch == '/'){
            scanChar();
            if(ch=='/'){//如果是单行注释
                while (ch!='\n'&&ch!='\r'){
                    scanChar();
                }
                getToken();
            }//如果是多行注释
            else if(ch=='*'){
                char pCh;
                scanChar();
                do{
                    pCh = ch;
                    scanChar();
                }while ((pCh!='*'||ch!='/')&&ch!=EOI);
                if (ch==EOI){

                    lookahead.setType(EOF);
                }
                else {
                    getToken();
                }
            }//如果是除号
            else{
                lookahead.setLine(preLine);
                lookahead.setCol(preCol);
                lookahead.setType(DIV);
                lookahead.setName("/");
                rollback();
            }

        }
        //识别分隔符
        else if(isDelimiter(ch)){
            lookahead.setLine(preLine);
            lookahead.setCol(preCol);
        }
        else {
            if(ch==EOI){
                lookahead.setType(EOF);
            }//出现错误
            else {
                lookahead.setName(ch + "");
                lookahead.setType(ERR);
                lookahead.setLine(preLine);
                lookahead.setCol(preCol);
                lookahead.setMsg("未识别的符号");
            }
        }
    }

    //回退一个字符
    private void rollback(){
        //如果多度的字符是换行符
        if(buffer[bp]=='\n'){
            line--;
            col=pCol;
        }
        else {
            col=pCol;
        }
        bp--;
    }

    /*判断是否是分隔符*/
    public boolean isDelimiter(char ch){
        switch (ch){
            case '(':
                lookahead.setType(LP);
                lookahead.setName("<");
                return true;
            case ')':
                lookahead.setType(RP);
                lookahead.setName(">");
                return true;
            case ',':
                lookahead.setType(CA);
                lookahead.setName(",");
                return true;
            case ';':
                lookahead.setType(SN);
                lookahead.setName(";");
                return true;
            default:
                return false;
        }
    }

    /*判断是否是操作符*/
    private boolean isOperator(char ch){
        switch (ch){
            case '=':
                return true;
            case '<':
                return true;
            case '>':
                return true;
            case '+':
                lookahead.setName("+");
                lookahead.setType(PLUS);
                return true;
            case '-':
                lookahead.setName("-");
                lookahead.setType(MINUS);
                return true;
            case '*':
                lookahead.setName("*");
                lookahead.setType(MUL);
                return true;
            default:
                return false;
        }


    }

    private boolean isLetter(char ch){
        return Character.isLetter(ch);
    }

    private boolean isKeyWord(String key){
        return KeyWord.keyWord.contains(key);
    }

    private boolean isDigit(char ch){
        return Character.isDigit(ch);
    }
    //返回保留字的种别码
    int returnType(String s)
    {
        if (s.equals("program"))
            return PROGRAM;
        else if (s.equals("var"))
            return VAR;
        else if (s.equals("procedure"))
            return PROCEDURE;
        else if (s.equals("begin"))
            return BEGIN;
        else if (s.equals("end"))
            return END;
        else if (s.equals("if"))
            return IF;
        else if (s.equals("then"))
            return THEN;
        else if (s.equals("else"))
            return ELSE;
        else if (s.equals("read"))
            return READ;
        else if (s.equals("write"))
            return WRITE;
        else if (s.equals("integer"))
            return INTEGER;
        else if (s.equals("fi"))
            return FI;
        else if (s.equals("while"))
            return WHILE;
        else if (s.equals("do"))
            return DO;
        else if(s.equals("float"))
            return FLOAT;
        else if(s.equals("and"))
            return AND;
        else if(s.equals("or"))
            return OR;
        else
            return ENDWH;
    }



    public static void main(String[] args) {
        Scanner scanner = new Scanner();
        do{
            scanner.getToken();
            System.out.println(scanner.lookahead.toString());
        }while (scanner.lookahead.getType()!=EOF);
    }
}
