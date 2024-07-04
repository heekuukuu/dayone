package dayone.dayone.excepion.impl;

import org.springframework.http.HttpStatus;

public class NoCompanyException extends  ArithmeticException{
    @Override
    public int getStatusCode(){
       return HttpStatus.BAD_REQUEST.value();

}
 @Override
    public String getMessage(){
     return "존재하지 않는 회사명입니다 ";
 }

}