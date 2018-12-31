package com.zhj.parse;

import com.zhj.scan.*;

import static com.zhj.scan.Data.*;


public class Parse extends Scanner{


    public Parse(){
        getToken();
    }

    /* 如果匹配成功返回true */
    public boolean match(int type){
        if(lookahead.getType()==type){
            getToken();
            return true;
        }
        else{
            return false;
        }
    }

    /* ProgramHead→ 'program' ID */
    public void programHead(){
        match(PROGRAM);
        match(IDENTIFIER);
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

    /***变量声明***/
    /*VarDecpart→ ε
        | 'var' VarDecList
*/
    public void VarDecpart(){
        if(lookahead.getType()==VAR){
            match(VAR);
            VarDecList();
        }
        else if(lookahead.getType()==PROCEDURE||lookahead.getType()==EOF){
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
        TypeName();
        match(IDENTIFIER);
        while (match(CA)){
            match(IDENTIFIER);
        }
        if(!match(SN )){
            error("期待输入分号");
        }
    }

    /* TypeName→'integer'
        | 'float'
*/
    public void TypeName(){
        if(lookahead.getType()==INTEGER){
            match(INTEGER);
        }
        else if(lookahead.getType()==FLOAT){
            match(FLOAT);
        }
        else {
            String msg = "错误的类型名" + '"' + lookahead.getName() + '"';
            if (lookahead.getType()!=EOF){
                error(msg);
                getToken();
            }
            else {
                msg="缺少变量名";
                error(msg);
            }
        }
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
    }

    /*  ProcDec→ 'procedure' ID '(' ParamList ')' ';' VarDecpart ProcBody  */
    public void ProcDec(){
        match(PROCEDURE);
        match(IDENTIFIER);
        match(LP);
        ParamList();
        match(RP);
        match(SN);
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

        }
    }

    /* Param→ TypeName ID {',' ID}  */
    public void Param(){
        TypeName();
        match(IDENTIFIER);
        while (lookahead.getType()==CA ){//匹配逗号
            match(CA);
            match(IDENTIFIER);
        }
    }

    //过程体
    /* ProcBody→ 'begin' StmList 'end' */
    public void ProcBody(){
        match(BEGIN);
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
            if(lookahead.getType()!=EOF)
                getToken();
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
            match(IF);
            ConditionalStm();
        }
        else if(lookahead.getType()==WHILE){
            match(WHILE);
            LoopStm();
        }
        else if(lookahead.getType()==READ){
            match(READ);
            InputStm();
        }
        else if(lookahead.getType()==WRITE){
            match(WRITE);
            OutputStm();
        }
        else if(lookahead.getType()==IDENTIFIER){
            match(IDENTIFIER);
            if(lookahead.getType()==LP){
                match(LP);
                ActParamList();
                match(RP);
            }
            else if(lookahead.getType()==ASSIGN){
                match(ASSIGN);
                Exp();
            }
        }
    }

    /*ConditionalStm→'if' ConditionalExp 'then' StmList 'else' StmList 'fi'*/
    public void ConditionalStm(){
        ConditionalExp();
        match(THEN);
        StmList();
        match(ELSE);
        StmList();
        match(FI);
    }

    /* InputStm→'read' ID */
    public void InputStm(){
        match(IDENTIFIER);
    }

    /* OutputStm→'write' Exp */
    public void OutputStm(){
        Exp();
    }
    /*  LoopStm→'while' ConditionalExp 'do' StmList 'endwh' */
    public void LoopStm(){
        match(WHILE);
        ConditionalExp();
        match(DO);
        StmList();
        match(ENDWH);
    }

    //实参声明
    /*ActParamList→ ε
          | Exp {',' Exp}
    */
    public void ActParamList(){
        if(lookahead.getType()==IDENTIFIER||
                lookahead.getType()==INTEGER||
                lookahead.getType()==DECIMAL||
                lookahead.getType()==LP){
            Exp();
            while (lookahead.getType()==CA){
                match(CA);
                Exp();
            }
        }

        else if(lookahead.getType()!=RP){
            String msg="错误的实参声明,缺少右括号";
            error(msg);
        }
    }

    //以下是四则表达式
    /*Exp→ Term {'+'|'-' Term}*/
    public void Exp(){
        Term();
        while (lookahead.getType()==PLUS||lookahead.getType()==MINUS){
            if(lookahead.getType()==PLUS){
                match(PLUS);
            }
            else if(lookahead.getType()==MINUS){
                match(MINUS);
            }
            Term();
        }
    }

    /*Term→ Factor {'*'|'/' Factor}*/
    public void Term(){
        Factor();
        while (lookahead.getType()==MUL||lookahead.getType()==DIV){
            if(lookahead.getType()==MUL){
                match(MUL);
            }
            else if(lookahead.getType()==DIV){
                match(DIV);
            }
            Factor();
        }

    }

    /*Factor→ ID | INTC | DECI | '(' Exp ')'*/
    public void Factor(){
        if(lookahead.getType()==IDENTIFIER){
            match(IDENTIFIER);
        }
        else if(lookahead.getType()==INTEGER){
            match(INTEGER);
        }
        else if(lookahead.getType()==DECIMAL){
            match(DECIMAL);
        }
        else if(lookahead.getType()==LP){
            match(LP);
            Exp();
            if(!match(RP)){
                error("缺少右括号，" + "未识别的" + lookahead.getName());
            }
        }
    }

    //以下为逻辑表达式的识别
    /*CmpOp→'<' | '<=' | '>' | '>=| '==' | '<>'*/
    public void CmpOp(){
        if(lookahead.getType()==LT){
            match(LT);
        }
        else if(lookahead.getType()==LE){
            match(LE);
        }
        else if(lookahead.getType()==GT){
            match(GT);
        }
        else if(lookahead.getType()==GE){
            match(GE);
        }
        else if(lookahead.getType()==EQ){
            match(EQ);
        }
        else if(lookahead.getType()==NQ){
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
    }


    /*CompExp→ Exp CmpOp Exp*/
    public void CompExp(){
        Exp();
        CmpOp();
        Exp();
    }

    /*RelationExp→ CompExp {'and' CompExp}*/
    public void  RelationExp(){
        CompExp();
        while (match(AND)){
            CompExp();
        }
    }

    /* ConditionalExp→RelationExp {'or' RelationExp}  */
    public void ConditionalExp(){
        RelationExp();
        while (match(OR)){
            RelationExp();
        }
    }

    /*错误处理*/
    public void error(String msg){
        System.out.println(lookahead.getLine() + "\t" + lookahead.getCol() +
        "\t" + msg );
        System.exit(-1);
    }
    public static void main(String[] args) {
        System.out.println("行\t"+"列\t"+"错误类型");
        Parse parse = new Parse();
        parse.program();
    }
}
