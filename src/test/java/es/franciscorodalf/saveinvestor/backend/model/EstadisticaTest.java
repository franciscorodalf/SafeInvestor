package es.franciscorodalf.saveinvestor.backend.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EstadisticaTest {

    @Test
    void testConstructorVacio() {
        estadistica e = new estadistica();
        
        assertNull(e.getId());
        assertNull(e.getUsuarioId());
        assertEquals(0.0, e.getTotalIngreso());
        assertEquals(0.0, e.getTotalGasto());
    }
    
    @Test
    void testConstructorConDosParametros() {
        estadistica e = new estadistica(1500.0, 800.0);
        
        assertNull(e.getId());
        assertNull(e.getUsuarioId());
        assertEquals(1500.0, e.getTotalIngreso());
        assertEquals(800.0, e.getTotalGasto());
    }
    
    @Test
    void testConstructorConTresParametros() {
        estadistica e = new estadistica(1500.0, 800.0, 1);
        
        assertNull(e.getId());
        assertEquals(1, e.getUsuarioId());
        assertEquals(1500.0, e.getTotalIngreso());
        assertEquals(800.0, e.getTotalGasto());
    }
    
    @Test
    void testSettersAndGetters() {
        estadistica e = new estadistica();
        
        e.setId(1);
        e.setUsuarioId(2);
        e.setTotalIngreso(2000.0);
        e.setTotalGasto(1200.0);
        
        assertEquals(1, e.getId());
        assertEquals(2, e.getUsuarioId());
        assertEquals(2000.0, e.getTotalIngreso());
        assertEquals(1200.0, e.getTotalGasto());
    }
    
    @Test
    void testSetTotalIngresoNegativo() {
        estadistica e = new estadistica();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            e.setTotalIngreso(-100.0);
        });
        
        String expectedMessage = "El total de ingresos no puede ser negativo";
        String actualMessage = exception.getMessage();
        
        assertTrue(actualMessage.contains(expectedMessage));
    }
    
    @Test
    void testSetTotalGastoNegativo() {
        estadistica e = new estadistica();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            e.setTotalGasto(-50.0);
        });
        
        String expectedMessage = "El total de gastos no puede ser negativo";
        String actualMessage = exception.getMessage();
        
        assertTrue(actualMessage.contains(expectedMessage));
    }
    
    @Test
    void testCalcularBalance() {
        estadistica e = new estadistica(2000.0, 1200.0);
        
        double balance = e.calcularBalance();
        
        assertEquals(800.0, balance);
    }
    
    @Test
    void testCalcularBalanceNegativo() {
        estadistica e = new estadistica(1000.0, 1500.0);
        
        double balance = e.calcularBalance();
        
        assertEquals(-500.0, balance);
    }
    
    @Test
    void testGenerarResumen() {
        estadistica e = new estadistica(2500.0, 1700.0);
        
        String resumen = e.generarResumen();
        
        assertEquals("Ingresos: 2500.0, Gastos: 1700.0", resumen);
    }
    
    @Test
    void testToString() {
        estadistica e = new estadistica();
        e.setId(1);
        e.setUsuarioId(2);
        e.setTotalIngreso(3000.0);
        e.setTotalGasto(2000.0);
        
        String expectedString = "Estadistica{id=1, usuarioId=2, totalIngreso=3000.0, totalGasto=2000.0}";
        
        assertEquals(expectedString, e.toString());
    }
}
