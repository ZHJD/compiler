package com.zhj.parse;

import com.sun.deploy.security.ValidationState;
import com.zhj.scan.Lookahead;
import com.zhj.scan.Scanner;

import java.util.*;

import static com.zhj.scan.Data.*;
import static com.zhj.scan.Data.LP;
import static com.zhj.scan.Data.RP;

public class Yuyi extends Scanner {
    private HashMap<String,SymTable> symTables;
    private List<Quad> qList;
    private HashMap<String,Function> functionHashMap;
    private int nxq;//指向下一个四元式地址
    private boolean isInFunction;
    private static int i = 0;
    private int kind;//标记已经读入的字符的类型
    private String functionName;

    public String newTemp(){
        return "t"+i++;
    }
    /*     回填函数     */
    public void backpatch(Stack<Integer> stack,int p){
        while (!stack.empty()){
            qList.get(stack.pop()).setResult(p+"");
        }
    }
    /*构造函数*/
    public Yuyi(){
        functionHashMap = new HashMap<>();
        symTables = new HashMap<>();
        qList = new ArrayList<>();
        nxq=0;
        isInFunction=false;
        getToken();
    }
    /*  判断函数是否已经声明                            */
    public boolean isHaveFunctionName(String name){
        return functionHashMap.containsKey(name);
    }
    /*      在符号表中查找            */
    public String lookup(String name){
        if(isInFunction){
            Function function = functionHashMap.get(functionName);
            if(function.localVari.containsKey(name)){
                return function.localVari.get(name).kind;
            }else {
                return null;
            }
        }else {
            if(symTables.containsKey(name)){
                return symTables.get(name).kind;
            }else {
                return null;
            }
        }
    }

    /*                          */
    public int getParmsLength(String functionName){
        return functionHashMap.get(functionName).length;
    }

    /*   返回形参类型                                       */
    public String getParmsKind(String functionName){
        Function function = functionHashMap.get(functionName);
        return function.parms.poll().kind;
    }

    /*      首先在函数内部找标识符，如果不存在则全局搜索                              */
    public String lookup(String name,boolean isUsed){
        Function function = functionHashMap.get(functionName);
        if(function.localVari.containsKey(name)){
            if(isUsed){
                function.localVari.get(name).time++;
            }
            return function.localVari.get(name).kind;
        }else {
            if(symTables.containsKey(name)){
                if(isUsed){
                    symTables.get(name).time++;
                }
                return symTables.get(name).kind;
            }else {
                return null;
            }
        }
    }
    /*        登记函数名                         */
    public void enterFunctionName(String name){
        functionHashMap.put(name,new Function());
    }
    /*  登记符号表                       */
    public void enter(String sym,String kind,boolean isParm){
        if(!isInFunction){
            SymTable symTable = new SymTable();
            symTable.name=sym;
            symTable.kind=kind;
            symTables.put(sym,symTable);
        }
        if(functionHashMap.containsKey(functionName)){
            Function function = functionHashMap.get(functionName);
            SymTable symTable = new SymTable();
            symTable.name=sym;
            symTable.kind=kind;
            function.localVari.put(sym,symTable);
            if(isParm){
                function.parms.add(symTable);
                function.length++;
            }
        }
    }


    /*          登记四元式                            */
    public void emit(String op,String arg1,String arg2,String result){
        Quad quad = new Quad(op,arg1,arg2,result);
        qList.add(quad);
        nxq++;
    }



    /* 如果匹配成功返回true */
    public boolean match(int type){
        if(lookahead.getType()==type){
            kind=type;
            getToken();
            return true;
        }
        else{
            return false;
        }
    }



    /*       打印四元式                                              */
    public void printCode(String op,String arg1,String arg2,String result){
        System.out.println("("+op+","+arg1+","+arg2+","+result+")");
    }

    /* Program→ ProgramHead VarDecpart ProgramBody  */
    public void  program(){
        programHead();
        VarDecpart();
        ProgramBody();
        if (lookahead.getType()!=EOF){
            String msg="程序结尾包含多余的字符";
            error(msg);
        }else if (lookahead.getType()==EOF){
            System.out.println("分析结束");
        }
    }

    /* ProgramHead→ 'program' ID */
    public void programHead(){
        if(!match(PROGRAM)){
            error("缺少program关键字");
        }
        if(lookahead.getType()==IDENTIFIER){
            if(lookup(lookahead.getName())==null){
                enter(lookahead.getName(),"programName",false);
                emit("program","","",lookahead.getName());
                match(IDENTIFIER);
            }else{
                error("程序中不能出现程序名字");
            }
        }
        else {
            error("缺少标识符"+lookahead.getName());
        }
    }


    /***变量声明***/
    /*VarDecpart→ ε
        | 'var' VarDecList
*/
    public void VarDecpart(){
        if(lookahead.getType()==VAR){
            if(!match(VAR)){
                error("缺少var关键字");
            }
            VarDecList();
        }
        else if(lookahead.getType()==PROCEDURE||lookahead.getType()==EOF||lookahead.getType()==BEGIN){
            return;
        }
        else {
            String msg="错误的变量声明";
            error(msg);
            getToken();
        }
    }

    /*  VarDecList→ VarIdList {VarIdList}  */
    public void VarDecList(){
        VarIdList();
        while (lookahead.getType()==INTEGER||lookahead.getType()==FLOAT){
            VarIdList();
        }
    }

    /* VarIdList→ TypeName ID {',' ID} ';' */
    public void VarIdList(){
        String eplace = TypeName();
        if(lookahead.getType()==IDENTIFIER){
            if(lookup(lookahead.getName())!=null){
                error("该变量名已经定义"+lookahead.getName());
            }
            enter(lookahead.getName(),eplace,false);
            match(lookahead.getType());
        }
        else {
            error("错误的标识符"+lookahead.getName());
        }
        while (match(CA)){
            if(lookahead.getType()==IDENTIFIER){
                if(lookup(lookahead.getName())!=null){
                    error("该变量名已经定义"+lookahead.getName());
                }
                enter(lookahead.getName(),eplace,false);
                match(lookahead.getType());
            }
            else {
                error("错误的标识符"+lookahead.getName());
            }
        }
        if(!match(SN )){
            error("期待输入分号");
        }
    }

    /* TypeName→'integer'
        | 'float'
*/
    public String TypeName(){
        String eplace="";
        if(lookahead.getType()==INTEGER){
            eplace="INTEGER";
            match(INTEGER);
        }
        else if(lookahead.getType()==FLOAT){
            eplace="FLOAT";
            match(FLOAT);
        }
        else {
            String msg = "错误的类型名" + '"' + lookahead.getName() + '"';
            if (lookahead.getType()!=EOF){
                error(msg);
            }
            else {
                msg="缺少变量名";
                error(msg);
            }
        }
        return eplace;
    }

    /***过程声明***/
    /* ProgramBody→ε
| ProcDec {ProcDec}  */
    public void ProgramBody(){
        if(lookahead.getType()==PROCEDURE){
            ProcDec();
            while (lookahead.getType()==PROCEDURE){
                ProcDec();
            }
        }
        else if(lookahead.getType()!=EOF){
            error("错误的程序体定义" + lookahead.getName());
        }
        isInFunction=false;
    }

    /*  ProcDec→ 'procedure' ID '(' ParamList ')' ';' VarDecpart ProcBody  */
    public void ProcDec(){
        match(PROCEDURE);
        if(lookahead.getType()==IDENTIFIER){
            functionName=lookahead.getName();
            isInFunction=true;
            if (isHaveFunctionName(functionName)){
                error("函数名字已经存在，不能重复使用"+functionName);
            }
            enterFunctionName(functionName);
            //enter(lookahead.getName(),"procedure",false);
            match(IDENTIFIER);
        }
        else {
            error("获取过程名失败" + lookahead.getName());
        }

        if(!match(LP)){
            error("缺少左括号");
        }
        ParamList();
        if(!match(RP)){
            error("形参列表出缺少右括号");
        }
        if(!match(SN)){
            error("缺少分号");
        }
        VarDecpart();
        ProcBody();
    }

    /*  形参声明  */
    /*  ParamList→ ε
        | Param {';' Param}
Param→ TypeName ID {',' ID}     */
    public void ParamList(){
        if(lookahead.getType()==RP){
            return;
        }
        else if(lookahead.getType()==INTEGER||lookahead.getType()==FLOAT){
            Param();
            while (lookahead.getType()==SN){
                match(SN);
                Param();
            }
        }
        else{
            error("定义形参列表错误 "+lookahead.getName());
        }
    }

    /* Param→ TypeName ID {',' ID}  */
    public void Param(){
        String eplace = TypeName();
        if(lookahead.getType()==IDENTIFIER){
            if(lookup(lookahead.getName())!=null){
                error("变量已经存在"+lookahead.getName());
            }
            enter(lookahead.getName(),eplace,true);
            match(IDENTIFIER);
        }
        else {
            error("形参定义错误，缺少标识符"+ lookahead.getName());
        }
        while (lookahead.getType()==CA ){//匹配逗号
            match(CA);
            if(lookahead.getType()==IDENTIFIER){
                if(lookup(lookahead.getName())!=null){
                    error("变量已经存在"+lookahead.getName());
                }
                enter(lookahead.getName(),eplace,true);
                match(IDENTIFIER);
            }
            else {
                error("形参定义错误，缺少标识符"+ lookahead.getName());
            }
        }
    }

    //过程体
    /* ProcBody→ 'begin' StmList 'end' */
    public void ProcBody(){
        if (!match(BEGIN)){
            error("缺少begin关键字");
        }
        StmList();
        if(!match(END)){
            error("缺少end关键字");
        }
    }



    //语句
    /*StmList→ ε
     | Stm {';' Stm}
*/
    public void StmList(){
        if(lookahead.getType()==END||
                lookahead.getType()==ELSE
                ||lookahead.getType()==FI||
                lookahead.getType()==ENDWH){
            return;
        }
        else if(lookahead.getType()==IF||
                lookahead.getType()==WHILE||
                lookahead.getType()==READ||
                lookahead.getType()==WRITE||
                lookahead.getType()==IDENTIFIER){
            Stm();
            while (lookahead.getType()==SN ){
                match(SN);
                Stm();
            }
        }
        else {
            String msg = "不完整的语句";
            error(msg);
        }
    }


    /*Stm→ConditionalStm
     | LoopStm
     | InputStm
     | OutputStm
     | CallStm
| AssignmentStm
*/
    public void Stm(){
        if(lookahead.getType()==IF){
            ConditionalStm();
        }
        else if(lookahead.getType()==WHILE){
            LoopStm();
        }
        else if(lookahead.getType()==READ){
            InputStm();
        }
        else if(lookahead.getType()==WRITE){
            OutputStm();
        }
        else if(lookahead.getType()==IDENTIFIER){
            String eplace = lookahead.getName();
            match(IDENTIFIER);
            if(lookahead.getType()==LP){
                if(!isHaveFunctionName(eplace)){
                    error("该函数尚未定义"+" "+eplace);
                }
                if(!match(LP)){
                    error(eplace + "右侧缺少左括号");
                }
                ActParamList(eplace);
                match(RP);
                emit("call","","",eplace);
            }
            else if(lookahead.getType()==ASSIGN){//如果是赋值语句
                match(ASSIGN);
                String ep2 =Exp();
                String kind1 = lookup(ep2,true);
                String kind2 = lookup(eplace,true);
                if(kind2==null){
                    error("没有声明的标识符"+" "+eplace);
                }
                if(kind2.equals("INTEGER")&&kind1.equals("FLOAT")){
                    error("不能把float型赋值给int型");
                }
                emit("=",eplace,ep2,"");

            }
            else {
                error("错误的函数调用或赋值语句"+lookahead.getName());
            }
        }else {
            error("不合法的语句 " + lookahead.getName());
        }
    }

    /*ConditionalStm→'if' ConditionalExp 'then' StmList 'else' StmList 'fi'*/
    public void ConditionalStm(){
        Stack<Integer> tc = new Stack<>();
        Stack<Integer> fc = new Stack<>();
        match(IF);
        ConditionalExp(tc,fc);
        if(!match(THEN)){
            error("缺少then");
        }
        backpatch(tc,nxq);
        StmList();
        if(!match(ELSE)){
            error("缺少else");
        }
        int temp = nxq;
        emit("jmp","","","");
        backpatch(fc,nxq);
        StmList();
        if(!match(FI)){
            error("缺少fi关键字");
        }
        qList.get(temp).setResult(nxq+"");
    }

    /* InputStm→'read' ID */
    public void InputStm(){
        match(READ);
        if(lookahead.getType()==IDENTIFIER){
            if(lookup(lookahead.getName(),true)==null){
                error("未定义的变量" + lookahead.getName());
            }
            emit("read","","",lookahead.getName());
            match(IDENTIFIER);
        }

    }

    /* OutputStm→'write' Exp */
    public void OutputStm(){
        match(WRITE);
        String ep = Exp();
        emit("write","","",ep);
    }
    /*  LoopStm→'while' ConditionalExp 'do' StmList 'endwh' */
    public void LoopStm(){
        Stack<Integer> tc = new Stack<>();
        Stack<Integer> fc = new Stack<>();
        match(WHILE);
        int temp1 = nxq;
        //调用布尔表达式识别函数
        ConditionalExp(tc,fc);
        match(DO);
        backpatch(tc,nxq);
        StmList();
        emit("jmp","","",temp1+"");
        backpatch(fc,nxq);
        match(ENDWH);
    }
    //实参声明
    /*ActParamList→ ε
          | Exp {',' Exp}
    */
    public void ActParamList(String functionName){
        if(lookahead.getType()==IDENTIFIER||
                lookahead.getType()==INTEGER||
                lookahead.getType()==DECIMAL||
                lookahead.getType()==LP){
            String ePlace = Exp();
            String kind = lookup(ePlace,true);
            String parmsKind = getParmsKind(functionName);
            int i=1;//记录实参个数
            if(!kind.equals(parmsKind)){
                error("实参类型和形参类型不匹配,形参类型为"+parmsKind+",实参类型为"
                +kind);
            }
            emit("push","","",ePlace);
            while (lookahead.getType()==CA){
                if(i==getParmsLength(functionName)){
                    error("实参个数大于形参个数");
                }
                match(CA);
                ePlace = Exp();
                kind = lookup(ePlace,true);
                parmsKind = getParmsKind(functionName);
                if(!kind.equals(parmsKind)){
                    error("实参类型和形参类型不匹配,形参类型为"+parmsKind+",实参类型为"
                            +kind);
                }
                i++;
                emit("push","","",ePlace);
            }
            if(i<getParmsLength(functionName)){
                int temp = getParmsLength(functionName)-i;
                error("实参个数为"+i+"个，缺少"+temp+"个实参");
            }

        }
        else if(lookahead.getType()!=RP){
            String msg="错误的实参声明,缺少右括号";
            error(msg);
        }
    }

    /* ConditionalExp→RelationExp {'or' RelationExp}  */
    public String ConditionalExp(Stack<Integer>tc,Stack<Integer>fc){
        String ePlace="";
        ePlace = RelationExp(tc,fc);
        while (lookahead.getType()==OR){
            String op = lookahead.getName();
            match(OR);
            backpatch(fc,nxq);
            String ep2 = RelationExp(tc,fc);

        }
        return ePlace;
    }

    /*RelationExp→ CompExp {'and' CompExp}*/
    public String  RelationExp(Stack<Integer>tc,Stack<Integer>fc){
        String ePlace="";
        CompExp(tc,fc);
        while (lookahead.getType()==AND){
            String op = lookahead.getName();
            match(AND);
            backpatch(tc,nxq);
            CompExp(tc,fc);
        }
        return ePlace;
    }

    /*CompExp→ Exp CmpOp Exp*/
    public void CompExp(Stack<Integer>tc,Stack<Integer>fc){
        String ePlace = Exp();
        String op = CmpOp();
        String ep2 = Exp();
        tc.push(nxq);
        emit("j" +op,ePlace,ep2,"");//无法得知
        fc.push(nxq);
        emit("jmp","","","");

    }

    //以下为条件表达式的识别
    /*          CmpOp→'<' | '<=' | '>' | '>=| '==' | '<>'        */
    public String CmpOp(){
        String ePlace = "";
        if(lookahead.getType()==LT){
            ePlace = lookahead.getName();
            match(LT);
        }
        else if(lookahead.getType()==LE){
            ePlace = lookahead.getName();
            match(LE);
        }
        else if(lookahead.getType()==GT){
            ePlace = lookahead.getName();
            match(GT);
        }
        else if(lookahead.getType()==GE){
            ePlace = lookahead.getName();
            match(GE);
        }
        else if(lookahead.getType()==EQ){
            ePlace = lookahead.getName();
            match(EQ);
        }
        else if(lookahead.getType()==NQ){
            ePlace = lookahead.getName();
            match(NQ);
        }
        else{
            if(lookahead.getType()==ERR){
                String msg = "未识别的符号"+ '"'+lookahead.getName() + '"' + ",期待输入关系运算符";
                error(msg);
            }
            else if(lookahead.getType()==EOF){
                String msg = "不完整的条件表达式";
                error(msg);
            }
            else{
                String msg = "错误的"+'"' + lookahead.getName() + '"' +",错误的关系运算符";
                error(msg);
            }
        }
        return ePlace;
    }

    //以下是四则表达式
    /*Exp→ Term {'+'|'-' Term} */
    public String Exp(){
       String ePlace = Term();
       String op = "";
        while (lookahead.getType()==PLUS||lookahead.getType()==MINUS){
            op=lookahead.getName();
            if(lookahead.getType()==PLUS){
                match(PLUS);
            }
            else if(lookahead.getType()==MINUS){
                match(MINUS);
            }
            String ep2 = Term();
            String result = "";
            int v1 = isNum(ePlace);
            int v2 = isNum(ep2);
            if(v1!=IDENTIFIER&&v2!=IDENTIFIER){
                if(v1==FLOAT||v2==FLOAT){
                    if(op.equals("+")){
                        result = Double.parseDouble(ePlace)+Double.parseDouble(ep2) + "";
                    }
                    else {
                        result = Double.parseDouble(ePlace)-Double.parseDouble(ep2) + "";
                    }
                    enter(result,"FLOAT",false);
                }else {
                    if(op.equals("+")){
                        result = Integer.parseInt(ePlace)+Integer.parseInt(ep2) + "";
                    }
                    else {
                        result = Integer.parseInt(ePlace)-Integer.parseInt(ep2) + "";
                    }
                    enter(result,"INTEGER",false);
                }
            }
            else {
                result = newTemp();
                String kind1 = lookup(ePlace,true);
                String kind2 = lookup(ep2,true);
                if(kind1.equals("FLOAT")||kind2.equals("FLOAT")){
                    enter(result,"FLOAT",false);
                }else {
                    enter(result,"INTEGER",false);
                }
            }
            emit(op,ePlace,ep2,result);
            ePlace = result;
        }
        return ePlace;
    }

    /*Term→ Factor {'*'|'/' Factor}*/
    public String Term(){
        String ePlace = Factor();
        String op = "";
        while (lookahead.getType()==MUL||lookahead.getType()==DIV){
            op = lookahead.getName();
            if(lookahead.getType()==MUL){
                match(MUL);
            }
            else if(lookahead.getType()==DIV){
                match(DIV);
            }
            String ep2 = Factor();
            String result = "";
            int v1 = isNum(ePlace);
            int v2 = isNum(ep2);
            if(v1!=IDENTIFIER&&v2!=IDENTIFIER){
                if(v1==FLOAT||v2==FLOAT){
                    if(op.equals("*")){
                        result = Double.parseDouble(ePlace)*Double.parseDouble(ep2) + "";
                    }
                    else {
                        result = Double.parseDouble(ePlace)/Double.parseDouble(ep2) + "";
                    }
                    enter(result,"FLOAT",false);
                }else {
                    if(op.equals("*")){
                        result = Integer.parseInt(ePlace)*Integer.parseInt(ep2) + "";
                    }
                    else {
                        result = Integer.parseInt(ePlace)/Integer.parseInt(ep2) + "";
                    }
                    enter(result,"INTEGER",false);
                }
            }
            else {
                result = newTemp();
                String kind1 = lookup(ePlace.trim(),true);
                String kind2 = lookup(ep2,true);
                if(kind1.equals("FLOAT")||kind2.equals("FLOAT")){
                    enter(result,"FLOAT",false);
                }else {
                    enter(result,"INTEGER",false);
                }
            }
            emit(op,ePlace,ep2,result);
            ePlace = result;
        }
        return ePlace;
    }

    /*Factor→ ID | INTC | DECI | '(' Exp ')'*/
    public String Factor(){
        String fPlace = null;
        if(lookahead.getType()==IDENTIFIER){
            String kind = lookup(lookahead.getName(),true);
            if(kind==null){//局部变量中没有这个定义
                error("未定义的标识符"+lookahead.getName());
            }
            else if(kind.equals("FLOAT")||kind.equals("INTEGER")){
                fPlace=lookahead.getName();
                match(IDENTIFIER);
            }
            else {
                error("该标识符的类型是 "+kind + "不能出现在算术表达式中");
            }
        }
        else if(lookahead.getType()==INTEGER){
            fPlace = lookahead.getDigit()+"";
            if(lookup(fPlace)==null)
                enter(fPlace,"INTEGER",false);
            match(INTEGER);
        }
        else if(lookahead.getType()==DECIMAL){
            fPlace = lookahead.getDecimal() + "";
            if(lookup(fPlace)==null)
                enter(fPlace,"FLOAT",false);
            match(DECIMAL);
        }
        else if(lookahead.getType()==LP){
            match(LP);
            fPlace = Exp();
            if(!match(RP)){
                error("缺少右括号，" + "未识别的" + lookahead.getName());
            }
        }
        return fPlace;
    }
    /*     判断字符串中是否为数字,由于标识符不能以数字开头，所以只需要看第一位                  */
    public int isNum(String str){
       if(Character.isDigit(str.charAt(0))){
           if(str.contains(".")){
               return FLOAT;
           }else {
               return INTEGER;
           }
       }else {
           return IDENTIFIER;
       }
    }

    /*错误处理*/
    public void error(String msg){
        System.out.println(lookahead.getLine() + "\t" + lookahead.getCol() +
                "\t" + msg );
        while (lookahead.getType()!=SN&&lookahead.getType()!=EOF){
            getToken();
        }
        if(lookahead.getType()==SN){
            getToken();
        }
        //System.exit(-1);
    }

    public void print(){
        for(int i=0;i<nxq;i++){
            System.out.println(i + " " +qList.get(i));
        }
    }

    /*          打印所有符号表                           */
    public void printTable(){
        Set<String> set = symTables.keySet();
        System.out.println("************打印全局变量*********************");
        for (String name:set){
            System.out.println(symTables.get(name));
        }
        System.out.println("************打印函数名和局部变量********************");
        set = functionHashMap.keySet();
        for (String name:set){
            System.out.println("函数名  "+name);
            functionHashMap.get(name).print();
        }
    }

    public static void main(String[] args) {
        Yuyi yuyi = new Yuyi();
        yuyi.program();
        yuyi.printTable();
        yuyi.print();
    }

}

class SymTable{
    String name;//符号名字
    String kind;//取值为函数名，变量两种类型
    int time=0;//使用次数
    @Override
    public String toString() {
        return name + "," + kind + " " + time;
    }
}
class Function{
    HashMap<String,SymTable> localVari = new HashMap<>();//局部变量
    Queue<SymTable> parms = new LinkedList<>();
    int length = 0;//形式参数的个数

    public void print(){
        System.out.println("局部变量信息");
        Set<String> set = localVari.keySet();
        for (String name:set){
            System.out.println(name + " ," + localVari.get(name));
        }
        System.out.println("形式参数");
        for(SymTable symTable:parms){
            System.out.println(symTable);
        }
        System.out.println(length);
    }
}
class Quad{
    private String oper;
    private String arg1;
    private String arg2;
    private String result;

    @Override
    public String toString() {
        return "("+oper+","+arg1+","+arg2+","+result +")";
    }

    public Quad(){
    }

    public Quad(String oper,String arg1,String arg2,String result){
        this.oper = oper;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}