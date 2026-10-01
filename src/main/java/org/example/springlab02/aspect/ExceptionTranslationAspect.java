package org.example.springlab02.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0)
public class ExceptionTranslationAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionTranslationAspect.class);

    @AfterThrowing(
            pointcut = "org.example.springlab02.aspect.Pointcuts.serviceOperation()",
            throwing = "ex"
    )
    public void captureTechnicalException(Throwable ex) {
        if (!(ex instanceof CatalogException)) {
            log.warn("[EXCEPTION-TRANSLATION] @AfterThrowing detected technical exception: {} - {}",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    @Around("org.example.springlab02.aspect.Pointcuts.serviceOperation()")
    public Object translateTechnicalExceptions(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (IllegalArgumentException ex) {
            String shortSig = pjp.getSignature().toShortString();
            log.info("[EXCEPTION-TRANSLATION] @Around translating IllegalArgumentException in {} to CatalogException(INVALID_ARGUMENT)", shortSig);
            throw new CatalogException("INVALID_ARGUMENT", "Catalog service error: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            if (ex instanceof CatalogException) {
                throw ex;
            }
            String shortSig = pjp.getSignature().toShortString();
            log.info("[EXCEPTION-TRANSLATION] @Around translating {} in {} to CatalogException(INTERNAL_ERROR)",
                    ex.getClass().getSimpleName(), shortSig);
            throw new CatalogException("INTERNAL_ERROR", "Catalog service error: " + ex.getMessage(), ex);
        }
    }
}
