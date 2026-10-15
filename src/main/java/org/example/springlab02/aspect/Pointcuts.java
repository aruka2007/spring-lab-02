package org.example.springlab02.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Pointcuts {

    @Pointcut("within(org.example.springlab02.service..*)")
    public void serviceLayer() { }

    @Pointcut("execution(public * *(..))")
    public void publicMethod() { }

    @Pointcut("serviceLayer() && publicMethod()")
    public void serviceOperation() { }
}
