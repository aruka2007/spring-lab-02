package org.example.springlab02;

import org.example.springlab02.aspect.CatalogException;
import org.example.springlab02.service.CatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lab4AopTests {

    @Autowired
    private CatalogService catalogService;

    @Test
    void testProxyTypeIsCglib() {
        assertTrue(AopUtils.isAopProxy(catalogService), "CatalogService must be an AOP proxy");
        assertTrue(AopUtils.isCglibProxy(catalogService), "CatalogService must be a CGLIB proxy in Spring Boot");
        assertTrue(catalogService.getClass().getName().contains("$$SpringCGLIB$$"), "Proxy class name must contain CGLIB marker");
    }

    @Test
    void testFindById() {
        String result = catalogService.findById(42);
        assertEquals("Item no. 42", result);
    }

    @Test
    void testFindAll() {
        List<String> items = catalogService.findAll(3);
        assertEquals(3, items.size());
        assertEquals("Item no. 1", items.get(0));
        assertEquals("Item no. 2", items.get(1));
        assertEquals("Item no. 3", items.get(2));
    }

    @Test
    void testRemoveSuccess() {
        String result = catalogService.remove(10);
        assertEquals("Removed item no. 10", result);
    }

    @Test
    void testRemoveExceptionTranslation() {
        // Due to ExceptionTranslationAspect (Variant 10), technical IllegalArgumentException is translated into CatalogException
        CatalogException ex = assertThrows(CatalogException.class, () -> catalogService.remove(0));
        assertEquals("INVALID_ARGUMENT", ex.getErrorCode());
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void testRemoveTwice() {
        String result = catalogService.removeTwice(5);
        assertEquals("Removed item no. 5; Removed item no. 6", result);
    }

    @Test
    void testRemoveTwiceFixed() {
        String result = catalogService.removeTwiceFixed(5);
        assertEquals("Removed item no. 5; Removed item no. 6", result);
    }
}
